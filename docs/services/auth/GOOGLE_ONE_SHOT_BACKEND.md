# Google One-Shot Registration — Backend Implementation Plan

Merge Google **identity** and Google **delegated access** into a single consent
round trip, **on registration only**. Login keeps the existing GIS ID-token flow
and is not touched.

Backend scope only. The frontend is an orchestration layer and is planned
separately in `client/features/auth/GOOGLE_ONE_SHOT_FLOW.md`.

> **Hard prerequisite.** This flow provisions accounts from inside the OAuth
> callback, so it inherits every gap in `UserProvisioningService` — including
> the missing `tenant.tenants` row and the four `NOT NULL` blockers listed in
> `SERVER_AGENTS.md` → "Workspace provisioning — INCOMPLETE". Those are runtime
> failures. Discovered from inside a callback they present as a `?google=error`
> redirect plus a stack trace, not as a clear error at the point of the mistake.
> Fix provisioning first and verify it through the existing password register
> endpoint.

---

## 1. Target behaviour

| Entry point | Mechanism | Consent screen | Produces |
|---|---|---|---|
| **Register** | authorization code + PKCE | yes, full scope list | account + session + `oauth_connections` row |
| **Login** | GIS ID token (unchanged) | no¹ | session |
| Settings → Integrations | authorization code (unchanged) | yes | `oauth_connections` row |

¹ Google still shows an account chooser. It skips the *consent* screen because
the registration grant already covers `openid email profile` for this client ID.
A user who registered with a password and later signs in with Google sees one
first-grant screen for basic profile info — that is Google's, not ours.

**Declining scopes ≠ declining consent.** These are different callbacks and must
end differently:

| User action | Google sends | Outcome |
|---|---|---|
| Unticks Calendar, continues | `code`, narrow `scope` | account + session + connection with narrow scopes |
| Cancels / closes the window | `error=access_denied` | **nothing** — no account, no session |

---

## 2. Request flow

```
POST /api/v1/auth/google/authorize          permitAll, no principal
  └─ mode = SIGN_UP_AND_CONNECT, userUid = null
     PKCE + state → Redis, TTL 10m
     → { authorizationUrl }

     [ browser navigates to Google, user consents ]

GET  /api/v1/integrations/google/callback   permitAll, existing endpoint
  ├─ consume state (GETDEL)                       ← moved above the error check
  ├─ error present?        → 302 ?google=denied
  ├─ exchange code                                  HTTP → Google
  ├─ verify id_token                                HTTP → Google JWKS (cached)
  └─ mode == SIGN_UP_AND_CONNECT
       └─ finalizeOnboarding(tokens, identity)      @Transactional
            resolve-or-provision user
            link auth.accounts
            persist oauth_connection
            issue handoff code → Redis, TTL 60s
       → 302 {frontend}{redirectAfter}?google=connected&handoff=<code>

POST /api/v1/auth/google/handoff            permitAll
  └─ GETDEL handoff → userUid
     mint access + refresh JWT, persist refresh token
     → { accessToken, refreshToken }
```

### Why the handoff exists

The callback lands on Spring (`:8080`) and redirects to Next.js (`:3000`).
Spring cannot set a cookie for the Next.js origin, and the frontend's security
model is httpOnly cookies written by its own BFF. Putting tokens in the redirect
query string would leak them into browser history, `Referer`, and every proxy
log in between.

**Store only the user reference in Redis, not the tokens.** The code that
transits the URL bar then becomes a lookup key that is worthless the instant it
is used, rather than a bearer credential that is worthless only once its TTL
elapses. The JWT pair is minted at exchange time, so the 15-minute access token
also starts its life when the session actually begins.

---

## 3. Unchanged

No edits at all. Listed because "does this need touching?" is the expensive
question during the build.

**OAuth infrastructure**
- `security/oauth/google/pkce/` — `PkceGenerator`, `PkceChallenge`
- `security/oauth/google/authorization/GoogleAuthorizationUrlBuilder` — `prompt=consent` stays hardcoded. Registration is the only caller of the code flow, and it *must* force consent to guarantee a refresh token. There is nothing to parameterise.
- `security/oauth/google/authorization/GoogleScope`
- `security/oauth/google/client/` — `GoogleTokenClient`, `DefaultGoogleTokenClient`, `dto/GoogleTokenResponse`
- `security/oauth/google/verifier/` — both files
- `security/oauth/google/config/GoogleOAuthConfig`
- `security/oauth/google/identity/GoogleIdentity`
- `security/crypto/` — `TokenCipher`, `AesGcmTokenCipher`

**Security wiring**
- `security/config/SecurityConfig` — **deliberately unchanged.** Both new endpoints sit under `/api/v1/auth/**`, which is already `permitAll`. Putting them anywhere else would force a new matcher; this is the reason for the URL choice, not a coincidence.
- `security/token/jwt/**`, `security/identity/**`

**Persistence**
- `integration/oauthstate/store/` — `OAuthStateStore`, `RedisOAuthStateStore`. Generic over `OAuthState`; the new field rides along in the existing JSON.
- `integration/oauthconnection/` — entity and repository
- **No Flyway migration.** Nothing in this flow changes the schema.

**Auth**
- `service/auth/service/GoogleSignInService` (interface) — signature stays `signIn(String idToken)`
- `service/auth/service/AuthService` / `AuthServiceImpl` — see §6 for why the `SessionIssuer` migration is deliberately excluded here
- `service/auth/service/UserProvisioningService` — unchanged *by this flow*, but blocked on the tenant work above

---

## 4. New components

### 4.1 `integration/oauthstate/model/OAuthFlowMode`

```java
public enum OAuthFlowMode {
    CONNECT,
    SIGN_UP_AND_CONNECT
}
```

Lives beside `OAuthState`, **not** in `integration/enums/`. Every enum in that
package is a native Postgres type mapped with `@JdbcTypeCode(NAMED_ENUM)`; this
one is a pure application value that only ever exists in Redis JSON. Filing it
with the schema enums would misrepresent it.

### 4.2 `auth/handoff/store/SessionHandoffStore` + `RedisSessionHandoffStore`

```java
public interface SessionHandoffStore {
    String issue(UUID userUid, Duration ttl);
    Optional<UUID> consume(String code);
}
```

Structurally a copy of `RedisOAuthStateStore`: 32 `SecureRandom` bytes, url-safe
Base64 without padding, `setIfAbsent` with TTL on issue, `getAndDelete` on
consume. Key prefix `oauth:handoff:`.

Two deliberate differences:
- The payload is a bare UUID string — no Jackson, no record. Nothing else is
  stored, by design (§2).
- TTL is **60 seconds**, not 10 minutes. The code does transit the URL bar; keep
  the window in which it means anything as small as the round trip needs.

Mirrors `integration/oauthstate/`'s layout under the `auth` bounded context.
Neither has a table — both are Redis-backed, so `GETDEL` gives single-use
consumption in one atomic command and TTL expiry removes the need for a cleanup
job. Do not add a migration.

### 4.3 `service/auth/service/GoogleAccountResolver` + `impl/DefaultGoogleAccountResolver`

```java
public interface GoogleAccountResolver {
    UserEntity resolve(GoogleIdentity identity);
}
```

Owns the complete three-step resolution, moved **verbatim** out of
`DefaultGoogleSignInService`:

1. `auth.accounts` by `(GOOGLE, sub)` → returning user
2. `auth.users` by email, gated on `emailVerified` → link
3. `UserProvisioningService` → new workspace

This extraction is the single most important part of the plan. Login and
registration are now two independent paths that can both provision a user; if
they do not share this component they will drift, and the thing that drifts is
the `emailVerified` gate. That gate is a real account-takeover control — without
it, anyone able to create a Google account bearing a victim's address could
claim their platform account. Move the code; do not reimplement it.

Carries across unchanged: `resolveOrProvisionUser`, `linkGoogleAccount`,
`provisionAndLinkNewUser`, `resolveAvailableUsername`, and the
`GOOGLE_PROVIDER` constant.

### 4.4 `service/auth/service/SessionIssuer` + `impl/DefaultSessionIssuer`

```java
public interface SessionIssuer {
    LoginResponse issue(UserEntity user);
}
```

`UserIdentityLoader` → `UserPrincipalMapper` → `JwtEngine` → persist
`RefreshTokenEntity`. This block is currently duplicated three times
(`AuthServiceImpl.login`, `AuthServiceImpl.register`,
`DefaultGoogleSignInService.signIn`).

Needed as a standalone component because the handoff exchange mints a session
for a user it never interactively authenticated — there is no `Authentication`
and no `PrincipalUserDetails` to start from, only a `UserEntity`.

### 4.5 `service/integration/service/GoogleConnectionWriter` + `impl/DefaultGoogleConnectionWriter`

```java
public interface GoogleConnectionWriter {
    void persist(UserEntity user, GoogleTokenResponse tokens, GoogleIdentity identity);
}
```

`DefaultGoogleConnectService.persist()` resolves the user from
`oauthState.userUid()` — which the onboarding branch does not have. Split the
lookup from the write: this takes a resolved `UserEntity`.

Move `mergedScopes` and `expiresAt` across with it.

**Add one guard that does not exist today.** The current `persist()` skips
writing `refresh_token` when Google omits it, which is correct for a
*re-consent* against an existing row. On a brand-new row there is no stored
token to fall back on, and the column is `NOT NULL` — so the insert fails, the
transaction rolls back, and the user is never provisioned:

```java
boolean isNewConnection = connection.getOauthConnectionId() == null;

if (isNewConnection
        && (tokens.refreshToken() == null || tokens.refreshToken().isBlank())) {
    log.warn("Google returned no refresh token for a new connection; "
           + "skipping connection write for user {}", user.getUserUid());
    return;
}
```

`access_type=offline` plus `prompt=consent` should always yield one, so this is
belt-and-braces — but the failure it prevents is "user cannot register at all",
which is far worse than "user registers without a calendar connection".

### 4.6 `service/auth/service/GoogleOnboardingService` + `impl/DefaultGoogleOnboardingService`

The single auth-facing entry point for this flow.

```java
public interface GoogleOnboardingService {
    GoogleAuthorizeResponse authorize(String redirectAfter);
    String finalizeOnboarding(GoogleTokenResponse tokens, GoogleIdentity identity);
    LoginResponse exchangeHandoff(String code);
}
```

**`authorize`** delegates to `GoogleConnectService.initiateOnboarding` (§5.2).
It does not rebuild PKCE/state/scope assembly — that logic stays in one file.

**`finalizeOnboarding`** is `@Transactional` and returns the handoff code:

```java
@Transactional
public String finalizeOnboarding(GoogleTokenResponse tokens, GoogleIdentity identity) {
    UserEntity user = googleAccountResolver.resolve(identity);
    googleConnectionWriter.persist(user, tokens, identity);
    return sessionHandoffStore.issue(user.getUserUid(), HANDOFF_TTL);
}
```

Three things about this method are load-bearing:

- **It is a separate bean, not a private method on `DefaultGoogleConnectService`.**
  Spring does not proxy self-invocation; a private method would silently run
  with no transaction and a provisioning failure would strand a user with a
  Google connection and no workspace.
- **The Redis write sits inside the transaction, and that is fine.** Contrast
  with the Google HTTP call, which is deliberately outside it — Redis is local
  and sub-millisecond, so the connection is not held meaningfully longer. If the
  commit fails after `issue()`, the exception propagates, the code never reaches
  the browser, and the orphaned key expires in 60 seconds. Harmless.
- **No JWTs here.** They are minted at exchange time (§2).

**`exchangeHandoff`**:

```java
@Transactional
public LoginResponse exchangeHandoff(String code) {
    UUID userUid = sessionHandoffStore.consume(code)
        .orElseThrow(() -> new InvalidSessionHandoffException(
            "Session handoff code is invalid or has already been used"
        ));

    UserEntity user = userRepository.findByUserUid(userUid)
        .orElseThrow(() -> new UserNotFoundException("User not found: " + userUid));

    return sessionIssuer.issue(user);
}
```

Expired, replayed, and forged codes are indistinguishable from the outside and
must all fail identically — same reasoning as `invalid_state`.

### 4.7 DTOs — `service/auth/dto/`

```java
public record GoogleAuthorizeRequest(String redirectAfter) {}
public record GoogleAuthorizeResponse(String authorizationUrl) {}

public record GoogleHandoffRequest(
    @NotBlank(message = "Missing handoff code")
    String code
) {}
```

`GoogleAuthorizeResponse` duplicates `GoogleConnectResponse`'s single field on
purpose — the auth controller's contract stays inside `service/auth/dto/`
rather than reaching across into the integration package for a one-field record.

`redirectAfter` carries no `@NotBlank`: null is valid and means "use the
default". It is validated by `safeRedirectAfter` in the service, not by
annotation.

### 4.8 `service/auth/exception/InvalidSessionHandoffException`

Plain `RuntimeException`, single `String message` constructor. Add an
`@ExceptionHandler` in `GlobalExceptionHandler` → **401 Unauthorized** (the
caller holds no valid session and must restart the flow).

### 4.9 Tests

| File | Covers |
|---|---|
| `auth/handoff/store/RedisSessionHandoffStoreTest` | issue/consume round trip, single-use `GETDEL`, unknown code → empty, null/blank code → empty. Mirror `RedisOAuthStateStoreTest`. |
| `service/auth/service/DefaultGoogleAccountResolverTest` | all three resolution branches + the `emailVerified` rejection. Largely liftable from the existing `DefaultGoogleSignInServiceTest`. |
| `service/auth/service/DefaultGoogleOnboardingServiceTest` | `finalizeOnboarding` calls resolver → writer → store in order; `exchangeHandoff` on an unknown code throws and issues no session |

---

## 5. Modified components

### 5.1 `integration/oauthstate/model/OAuthState`

```java
public record OAuthState(
    OAuthFlowMode mode,
    UUID userUid,           // null when SIGN_UP_AND_CONNECT — nobody is logged in yet
    String codeVerifier,
    List<String> requestedScopes,
    String redirectAfter
) {}
```

**Branch on `mode == SIGN_UP_AND_CONNECT`, never on `mode == CONNECT`.** States
written before the deploy deserialise with `mode = null` and still carry their
`userUid`; branching this way sends them down the connect path and nothing
in flight breaks across the rollout. Branching the other way would fail every
consent started in the preceding 10 minutes.

### 5.2 `service/integration/service/GoogleConnectService` (interface)

Add one method:

```java
GoogleConnectResponse initiateOnboarding(String redirectAfter);
```

Deliberately kept here rather than duplicated into the auth package: this is
`initiate()` with `mode = SIGN_UP_AND_CONNECT`, `userUid = null`, and every
`GoogleScope` value. Keeping all authorization-URL construction — scope
assembly, PKCE, state TTL, `safeRedirectAfter` — in one file is worth the
package dependency it creates.

> **Trade-off, stated plainly.** `service.auth` and `service.integration` now
> reference each other: `DefaultGoogleOnboardingService` → `GoogleConnectService`,
> and `DefaultGoogleConnectService` → `GoogleOnboardingService`. There is no
> *bean* cycle (the call graph is a DAG), but there is a package cycle. The
> alternative is duplicating four collaborators and the scope/PKCE logic into
> the auth package. Duplication of security-relevant assembly is the worse of
> the two.

### 5.3 `service/integration/service/impl/DefaultGoogleConnectService`

Four changes.

**a. Fix `DEFAULT_REDIRECT_AFTER`.**

```java
private static final String DEFAULT_REDIRECT_AFTER = "/dashboard/settings/integrations";
```

The current value, `/settings/integrations`, is not a route in the Next.js app
(the real page is `app/dashboard/settings/integrations/page.tsx`). This is
already mildly broken for the connect flow; the one-shot makes it a 404 on a new
user's first contact.

**b. Consume the state before the error check.**

```java
Optional<OAuthState> resolved = oauthStateStore.consume(state);

if (error != null && !error.isBlank()) {
    log.info("Google consent was not granted: {}", error);
    return redirect(
        resolved.map(OAuthState::redirectAfter).orElse(DEFAULT_REDIRECT_AFTER),
        "denied"
    );
}

if (resolved.isEmpty()) { /* invalid_state, unchanged */ }
```

Google sends `state` on error callbacks too. Returning early discards the only
thing that knows where the user came from — so a new user who cancels consent
gets bounced to the integrations page instead of back to `/register`. Consuming
first also keeps the single-use guarantee honest: a state should be spent
whether or not consent succeeded.

**c. Branch the success path.**

```java
GoogleIdentity identity = googleTokenVerifier.verify(tokens.idToken());

if (oauthState.mode() == OAuthFlowMode.SIGN_UP_AND_CONNECT) {
    String handoff = googleOnboardingService.finalizeOnboarding(tokens, identity);
    return redirect(oauthState.redirectAfter(), "connected", handoff);
}

UserEntity user = userRepository.findByUserUid(oauthState.userUid())
    .orElseThrow(() -> new UserNotFoundException("User not found: " + oauthState.userUid()));
googleConnectionWriter.persist(user, tokens, identity);

return redirect(oauthState.redirectAfter(), "connected");
```

The existing `catch (RuntimeException)` → `?google=error` wrapper stays and now
also contains provisioning failures. That containment is why a broken
`UserProvisioningService` shows up as an opaque error redirect rather than a
readable stack trace at the call site — hence the prerequisite in the header.

**d. Add a `redirect` overload** carrying `handoff`, and add
`initiateOnboarding`. Delete `persist`, `mergedScopes`, `expiresAt` (moved to
§4.5). Keep `safeRedirectAfter` — it is the open-redirect guard and both flows
need it.

### 5.4 `service/auth/service/impl/DefaultGoogleSignInService`

Shrinks to orchestration:

```java
@Transactional
public LoginResponse signIn(String idToken) {
    GoogleIdentity identity = googleTokenVerifier.verify(idToken);
    UserEntity user = googleAccountResolver.resolve(identity);
    return sessionIssuer.issue(user);
}
```

Drops seven injected collaborators (`AccountRepository`, `UserRepository`,
`UserProvisioningService`, `UserIdentityLoader`, `UserPrincipalMapper`,
`JwtEngine`, `JwtProperties`, `RefreshTokenRepository`) for two. All four private
methods move to `DefaultGoogleAccountResolver`.

**Behaviour must not change.** This is a pure refactor of the login path, which
is the one thing in this plan that is already in production use.

### 5.5 `service/auth/controller/AuthController`

Inject `GoogleOnboardingService`; add two endpoints. `POST /google` stays
exactly as it is.

```java
@PostMapping("/google/authorize")
public GoogleAuthorizeResponse authorizeGoogleOnboarding(
    @RequestBody(required = false) GoogleAuthorizeRequest request
) {
    return googleOnboardingService.authorize(
        request == null ? null : request.redirectAfter()
    );
}

@PostMapping("/google/handoff")
public LoginResponse exchangeGoogleHandoff(
    @Valid @RequestBody GoogleHandoffRequest request
) {
    return googleOnboardingService.exchangeHandoff(request.code());
}
```

`required = false` on the authorize body matches how
`GoogleIntegrationController` already treats `/connect`.

### 5.6 Tests that will fail to compile

- **`DefaultGoogleConnectServiceTest`** — every `new OAuthState(...)` breaks on
  arity. Six call sites (lines ~189, ~237, ~264 plus the `initiate` captors).
  Add `OAuthFlowMode.CONNECT` as the first argument. The `initiate_*` assertions
  should also assert `mode == CONNECT`, and the denied-path test needs its
  expected default path updated per §5.3a.
- **`DefaultGoogleSignInServiceTest`** — the mock set changes completely. Split
  it: resolution assertions (including `emailVerified`) move to
  `DefaultGoogleAccountResolverTest`; what remains is a thin
  verifier → resolver → issuer delegation test.

---

## 6. Explicitly out of scope

**Migrating `AuthServiceImpl` to `SessionIssuer`.** `login()` and `register()`
each contain the same JWT-plus-refresh-token block that `SessionIssuer` now
encapsulates, and folding them in would remove the last of the duplication. It
is left out because this plan already refactors the Google login path, and
touching password login/register as well means a regression there cannot be
attributed to either change. Do it as a follow-up, on its own.

**Scope enforcement at call time.** Granted scopes are persisted but nothing
checks them before a Calendar or Sheets call — the existing gap recorded in
`SECURITY.md`. Until a consumer exists, "did the user actually grant Calendar?"
is the dashboard's question to ask, and it must be asked against the `scopes`
array, not against `status`: `persist()` sets `ACTIVE` unconditionally, so a
user who unticked every sensitive scope still has an ACTIVE connection that can
do nothing.

**Concurrency guard on simultaneous consents.** Unchanged from today: the unique
constraint on `(user_id, provider, provider_account_id)` prevents a duplicate
row, but the loser surfaces as a 500.

---

## 7. Build sequence

Ordered so each step is independently verifiable and the risky work lands on top
of already-proven parts.

1. **Tenant provisioning creates a `tenant.tenants` row.** Verify through
   `POST /api/v1/auth/register` — a new user must come out with a tenant on the
   FREE plan and all five `max_*` limits populated. Everything below mints
   broken accounts until this is true.
2. **Extract `GoogleAccountResolver`.** Pure refactor. `./gradlew test` — the
   existing sign-in tests should pass with only mock rewiring.
3. **Extract `SessionIssuer`**, wire into `DefaultGoogleSignInService`. Still a
   pure refactor; login behaviour must be identical.
4. **Extract `GoogleConnectionWriter`**, including the new-connection refresh
   token guard (§4.5). `DefaultGoogleConnectServiceTest` should still pass.
5. **`SessionHandoffStore` + `RedisSessionHandoffStoreTest`.** Standalone, no
   dependants yet.
6. **`OAuthFlowMode` + `OAuthState` field**, then the `complete()` changes
   (§5.3 a–d). Fix the broken test call sites here.
7. **`GoogleOnboardingService`**, `initiateOnboarding`, the two DTOs, the
   exception and its handler.
8. **Two controller endpoints.** Boot the app — `./gradlew bootRun` — and
   confirm the context loads every new bean before testing behaviour.
9. **Walk the real flow against a live Google client.** This is the first time
   any of it has run end to end; per `SECURITY.md`, `GoogleTokenResponse` has
   never deserialised an actual Google payload.

Outcomes to exercise deliberately in step 9, not just the happy path:

- consent granted in full → account, session, connection with three scopes
- Calendar unticked → account, session, connection with narrow scopes
- consent cancelled → `?google=denied`, **no user row written**
- handoff replayed → second attempt rejected, no second session
- handoff expired (wait 60s) → rejected identically to a forged code
- an existing Google account hitting `/authorize` → resolution step 1 signs them
  in rather than erroring
