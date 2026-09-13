# Authentication Identity & Principal Architecture

## Overview

The Authentication Identity architecture defines how authenticated identities are represented throughout the platform while maintaining a strict separation between the business domain and the underlying security framework.

The primary architectural objective is to ensure that the Authentication domain owns all business concepts related to identity, while Spring Security remains an infrastructure concern responsible only for integrating with the application framework.

This design prevents framework-specific abstractions from leaking into business logic and allows the platform to evolve independently of Spring Security.

## Motivation

Spring Security requires authenticated identities to implement framework-specific contracts such as UserDetails and Authentication.

Although these abstractions are useful for integrating with the framework, they should not become part of the domain model because they tightly couple business logic to a specific security implementation.

Instead, the platform introduces a canonical business identity called AuthenticatedPrincipal.

Every authentication mechanism—including Username/Password, JWT, OAuth, API Keys, Widgets, and future Service Accounts—ultimately produces the same authenticated identity model.

Business authorization decisions are always performed using AuthenticatedPrincipal rather than Spring Security interfaces.

## Architectural Principles

The identity architecture follows several core principles.

Framework Isolation

Business code must never depend directly on Spring Security classes.

Only the infrastructure layer is aware of:

UserDetails
Authentication
GrantedAuthority
SecurityContextHolder

The business layer only understands:

AuthenticatedPrincipal
AuthenticationAggregate
Authorization Context
Canonical Identity Model

The platform defines exactly one authenticated identity representation.

AuthenticatedPrincipal

This object represents the authenticated actor regardless of how authentication occurred.

Examples include:

Username & Password
JWT
Google OAuth
API Key
Widget Token
Internal Service Account

Every authentication mechanism ultimately produces the same identity representation.

In practice, AuthenticatedPrincipal is a sealed interface, and each actor type is a concrete record implementing it — UserPrincipal, WidgetPrincipal, and future additions such as ApiKeyPrincipal. The sealed permits list is what enforces the "exactly one representation" guarantee at compile time: every actor must implement the interface's common contract (actorType, subject, tenantUid), and any code that switches over AuthenticatedPrincipal is checked exhaustively by the compiler, so introducing a new actor type surfaces every call site that needs to handle it. Every implementation is guaranteed to expose a tenantUid field, but not every implementation guarantees it is non-null — see Tenant Optionality below.

Not every actor carries the same amount of identity data. UserPrincipal is rich — username, roles, permissions, organization, membership, profile — because users participate in RBAC and organizational structure. WidgetPrincipal is deliberately minimal — actor type, widget identifier, tenant, status — because a widget has no roles layer, just a fixed, narrow capability set bound to a single tenant. The sealed interface only guarantees the fields every actor needs (identity, tenant scoping); anything beyond that is specific to the concrete type.

The first seven components run during authentication, building the principal and handing it to the framework. CurrentPrincipalProvider runs afterwards, on every subsequent request, and is the only sanctioned way for code outside the security package to ask "who is calling?" — see SecurityContext Integration.

## Authentication Pipeline

The complete authentication pipeline is illustrated below.

```bash
Database
        │
        ▼
IdentityLoader
        │
        ▼
AuthenticationAggregate
        │
        ▼
PrincipalMapper
        │
        ▼
AuthenticatedPrincipal
        │
        ├──────────────► Authorization Engine
        │
        ▼
GrantedAuthorityAdapter
        │
        ▼
UserDetailsAdapter
        │
        ▼
PrincipalUserDetails
        │
        ▼
Authentication
        │
        ▼
SecurityContextHolder

Notice that the business identity is created before interacting with Spring Security.

The framework only receives an adapter around the business identity.
```

This is the full pipeline for actors whose identity requires joining multiple tables — currently only User (user, membership, organization, tenant, profile, roles, permissions). Not every actor needs every stage. Widget authentication uses a shortened pipeline instead:

```bash
Database
        │
        ▼
WidgetIdentityLoader
        │
        ▼
WidgetPrincipal
        │
        ▼
AuthenticationTokenFactory
        │
        ▼
SecurityContextHolder
```

WidgetIdentityLoader queries a single entity (WidgetEntity) and constructs WidgetPrincipal directly — there is no AuthenticationAggregate or PrincipalMapper stage, because there is no multi-repository join to justify a separate read-model/transformation split. This is a deliberate simplification, not an incomplete implementation: the AuthenticationAggregate/PrincipalMapper stages exist to keep a complex, multi-entity assembly out of the loader, and a single-entity actor has nothing for them to do. Future low-complexity actors (e.g. API Keys, if their identity resolves from one row) should follow the same shortened shape rather than introducing an aggregate/mapper pair with nothing to aggregate or map.

## Architectural Benefits

This architecture provides several long-term advantages.

Framework Independence

Business logic remains isolated from Spring Security.

Changing authentication frameworks requires modifications only within the adapter layer.

Single Responsibility

Each component performs one well-defined responsibility.

Loading, mapping, adaptation, and authorization remain independent concerns.

Extensibility

Future authentication mechanisms—including OAuth, API Keys, and Service Accounts—can reuse the same architecture by introducing a new IdentityLoader (and, only if the actor's identity requires joining multiple tables, an AuthenticationAggregate/PrincipalMapper pair) while continuing to produce the same AuthenticatedPrincipal contract. Widget Tokens are already implemented this way — see Widget Authentication.

Testability

Business authorization can be unit tested using AuthenticatedPrincipal without constructing SecurityContextHolder or mocking Spring Security.

Maintainability

Framework code remains localized within infrastructure.

Business code operates exclusively on domain concepts, reducing coupling and simplifying long-term evolution.

##  Widget Authentication

Widget authentication is the second concrete authentication mechanism built on this architecture, after User, and the first to deliberately diverge from the full pipeline described above.

A widget authenticates using a public_key/secret_key pair issued at widget registration, rather than a username and password. Unlike a user, a widget has no roles, no permissions, and no organizational membership — it is a single-purpose actor with a fixed, narrow capability set, bound to exactly one tenant. WidgetPrincipal reflects this: it carries only actor type, widget identifier, tenant, and status, with no roles/permissions fields.

Origin Validation

Each widget is registered against exactly one client-owned website domain (e.g. barbershop.com) in widget_origins. This is a defense against a stolen or leaked public key being replayed from a domain the widget was never embedded on — browsers set the Origin header on outbound requests in a way page JavaScript cannot override, so comparing it against the widget's registered origin is a meaningful check against unauthorized cross-origin use of a public key, even though it does not defend against a server-side attacker who can set arbitrary headers.

This check happens inside WidgetIdentityLoader.loadByPublicKey(publicKey, origin), before a WidgetPrincipal is ever constructed: if the widget has origin validation enabled, the loader looks up whether the request's origin is registered for that widget and throws WidgetOriginMismatchException (mapped to 401) on a mismatch. Origin is not a field on WidgetPrincipal — it is a one-time admission check performed by the loader, not an ongoing identity property carried forward on the principal, since nothing downstream of authentication needs to know what origin a request arrived on.

Shortened Pipeline

WidgetIdentityLoader returns WidgetPrincipal directly — there is no WidgetAggregation and no WidgetPrincipalMapper. Both existed briefly during development and were removed once it became clear they added a layer of indirection around a single WidgetEntity lookup with no actual aggregation or transformation work to do. See the Authentication Pipeline and Component Responsibilities sections above for the general principle this establishes for future low-complexity actors.

SecurityContext Integration

WidgetPrincipal never passes through UserDetailsAdapter or PrincipalUserDetails. AuthenticationTokenFactory constructs a PreAuthenticatedAuthenticationToken directly around the WidgetPrincipal instead, since Widget has no password/account-lock concepts for UserDetails to represent. See SecurityContext Integration above for how AuthenticationTokenFactory picks between the two paths.

JWT

JwtClaims and JwtClaimsFactory are actor-agnostic (actorType, subject, tenant, tokenId, tokenType, timestamps), so the same JWT shape and issuance/verification code serves both User and Widget. JwtAuthenticationFilter reads claims.actorType() on every request to decide whether to resolve the subject through UserIdentityLoader or WidgetIdentityLoader.

Not Yet Implemented

WidgetPrincipal currently has no scopes or authority set — a widget authenticates successfully but AuthenticationTokenFactory grants it an empty GrantedAuthority collection. A capability model (e.g. reusing the auth.permissions slug vocabulary as a fixed, non-RBAC scope set per widget) is expected but not yet built. The widget bootstrap flow does exist at `POST /api/v1/auth/widget/bootstrap`: it exchanges the public/secret key pair and request origin for a JWT through `WidgetIdentityLoader.loadByPublicKey`.

## Google Sign-In (Authentication)

Google Sign-In is the third authentication mechanism, after Username/Password and Widget. It answers "who is this actor?" and produces the same canonical UserPrincipal every other mechanism produces. It grants the platform no access to any Google API — that is a separate concern, described in section 12.

Flow Shape: ID Token, Not Authorization Code

The client obtains a signed ID token from Google Identity Services in the browser and posts it to the server. There is no redirect, no authorization code, no client secret, and no token exchange. The ID token is verified once, converted to an internal identity, and discarded — it is never stored.

This is deliberately the simpler of the two Google flows. Sign-in needs only a trustworthy assertion of who the user is; it does not need ongoing access to anything.

```bash
Browser (Google Identity Services)
        │  id_token
        ▼
POST /api/v1/auth/google
        │
        ▼
GoogleTokenVerifier
        │  GoogleIdentity(sub, email, emailVerified, name, pictureUrl)
        ▼
GoogleAccountResolver  ── three-way account resolution
        │
        ▼
SessionIssuer  ── UserIdentityLoader → UserPrincipalMapper → JwtEngine
        │
        ▼
LoginResponse(accessToken, refreshToken)
```

DefaultGoogleSignInService is only the orchestration of those three steps — verify, resolve, issue. Both collaborators are shared with the one-shot registration flow in section 14, which is the point: two entry points can each provision a user, and they must not drift.

Token Verification

DefaultGoogleTokenVerifier wraps a NimbusJwtDecoder configured in GoogleOAuthConfig against Google's JWKS endpoint. Four validations run on every token: RS256 signature against the key matching the token's `kid`, timestamp (`exp`/`nbf`), issuer, and audience.

Two details are easy to get wrong and are handled explicitly:

- Google's `iss` claim is sometimes the schemeless string `accounts.google.com` and sometimes `https://accounts.google.com`. Spring's default claim converter tries to parse `iss` as a `java.net.URL` and throws on the schemeless form. GoogleOAuthConfig overrides the converter for that one claim to keep it a raw String, and accepts both spellings.
- The audience must equal the configured `client-id`. Without this check, an ID token minted for any other Google application would be accepted, letting an attacker authenticate as any user of the platform using a token issued to an unrelated app.

Account Resolution

GoogleAccountResolver (`service/auth/`) resolves the Google identity to a platform user in three ordered steps:

1. Look up `auth.accounts` by (provider = GOOGLE, provider_account_id = sub). A hit is a returning user; nothing is written.
2. Otherwise look up `auth.users` by email. A hit links the Google account to the existing user by inserting an `auth.accounts` row — but only if `email_verified` is true.
3. Otherwise provision a new user through UserProvisioningService (the same component `register()` uses), then link.

Step 2's `email_verified` gate is a critical control, not a formality. Without it, anyone able to create a Google account bearing a victim's email address — including via a domain they control that Google has not verified — could take over that platform account by signing in with Google. A false value produces GoogleAccountEmailNotVerifiedException (409) and writes nothing.

Note the gate guards *linking to an account that already exists*, not account creation. An unverified email with no matching user still provisions normally — there is nothing to take over.

This component is deliberately shared with the one-shot registration flow rather than reimplemented there. Two independent copies of a three-step resolution would eventually diverge, and the thing that would diverge is the gate above.

Google never returns a phone number, so `auth.users.phone` was made nullable (V124) to support this path.

After resolution, the flow rejoins the standard pipeline unchanged: UserIdentityLoader, UserPrincipalMapper, JwtEngine, and a persisted refresh token. Google-ness ends at account resolution.

## Google Delegated Authorization (OAuth2 Authorization Code Flow)

This is a fundamentally different concern from section 11 and the distinction should not be blurred. Sign-In answers "who is this user?" Delegated authorization answers "may this platform act on the user's behalf against Google Calendar, Sheets, and Drive, and for how long?" It produces no principal, issues no platform JWT, and touches no part of the identity pipeline. Its output is a long-lived, encrypted credential stored in `integration.oauth_connections`.

A user may be signed in with a password and still connect Google. The two are orthogonal.

Why the Backend Owns the Redirect

The registered `redirect_uri` points at the backend (`/api/v1/integrations/google/callback`), not the dashboard. The authorization code transits the browser either way — that is inherent to the flow — but a backend-owned callback removes one hop where the code could land in browser history, a frontend server log, or an error-reporting breadcrumb. It also lets exchange, verification, encryption, and persistence happen in one place, and keeps CORS out of the callback entirely, since a top-level browser navigation is not a cross-origin XHR.

Note this requires an Authorized **redirect URI** in Google Cloud Console. That is a different setting from the Authorized **JavaScript origin** that Google Sign-In needs; both exist on the same OAuth client.

Components

```text
security/oauth/google/
  pkce/            PkceGenerator, PkceChallenge
  authorization/   GoogleScope, GoogleAuthorizationUrlBuilder
  client/          GoogleTokenClient, DefaultGoogleTokenClient, dto/GoogleTokenResponse
  verifier/        GoogleTokenVerifier          (shared with Sign-In)
integration/oauthstate/
  model/OAuthState, model/OAuthFlowMode
  store/OAuthStateStore, store/RedisOAuthStateStore
service/integration/
  GoogleConnectService / DefaultGoogleConnectService
  GoogleConnectionWriter / DefaultGoogleConnectionWriter   (shared with one-shot)
  GoogleAccessTokenProvider / DefaultGoogleAccessTokenProvider
  GoogleIntegrationController
```

`integration/oauthstate/` has no entity, no repository and no table — it is Redis-backed, chosen over a Postgres table because `GETDEL` gives single-use consumption in one atomic command and TTL expiry removes the need for a cleanup job. The same is true of `auth/handoff/` in section 14. Neither takes a migration.

Phase 1 — Initiate

`POST /api/v1/integrations/google/connect`, authenticated, carrying a Bearer token.

CurrentPrincipalProvider.requireUser() identifies the caller — this is the only point in the whole flow where the platform learns whose Google account is being connected. The service then generates a 256-bit opaque state token and a PKCE verifier/challenge pair, writes `{userUid, codeVerifier, requestedScopes, redirectAfter}` into Redis under `oauth:state:<token>` with a 10-minute TTL and `NX`, and returns the Google authorization URL as JSON.

The endpoint returns a URL rather than a 302 on purpose. The initiating call is an XHR carrying an Authorization header; `fetch` follows redirects transparently, so a 302 would make the browser attempt to *fetch* Google's consent page cross-origin, which fails CORS and the user never sees the consent screen. The navigation has to be a deliberate `window.location` assignment by the client.

Three authorization parameters carry non-obvious weight:

- `access_type=offline` — without it Google returns no refresh token and the integration dies in one hour.
- `prompt=consent` — forces a refresh token on *every* consent, not only the first for a given account. Without it, a user reconnecting receives no refresh token and the NOT NULL column has nothing to write.
- `include_granted_scopes=true` — incremental authorization, so connecting Sheets later yields a token valid for Calendar and Sheets both.

Scopes are requested as GoogleScope enum values (CALENDAR, SHEETS, DRIVE_FILE), never as raw URIs supplied by the client. A caller therefore cannot ask Google for a scope the platform has not deliberately allow-listed. `openid`, `email`, and `profile` are always appended, because `openid` is what makes Google return the `id_token` the callback needs to identify the granting account.

DRIVE_FILE is `drive.file`, not full `drive`, by deliberate choice: full Drive access triggers Google's annual CASA third-party security assessment, a real cost and delay, while `drive.file` covers files the application itself creates.

Phase 2 — Consent

Entirely between the browser and Google. The consent screen lets the user untick individual scopes, so what is granted is not necessarily what was requested. Everything downstream treats the granted set as authoritative.

Phase 3 — Callback

`GET /api/v1/integrations/google/callback`, permitAll, reached by top-level browser navigation and therefore carrying no Authorization header. Identity comes from `state` alone.

```text
1. Redis GETDEL oauth:state:<state>
2. error=access_denied            → 302 …?google=denied            [stop]
     empty state                  → 302 …?google=invalid_state     [stop]
3. POST Google token endpoint     (outside any transaction)
     code, code_verifier, redirect_uri, client credentials in Basic header
4. GoogleTokenVerifier.verify(id_token) → sub, email
5. mode = SIGN_UP_AND_CONNECT     → section 14                     [stop]
6. TokenCipher.encrypt(access_token), TokenCipher.encrypt(refresh_token)
7. upsert integration.oauth_connections on (user_id, GOOGLE, sub)
8. 302 → {frontend}{redirectAfter}?google=connected
```

Step 1 is the security core of the flow, and `GETDEL` does three jobs in one atomic command. It is the CSRF defence: an attacker cannot forge a state they never received, so a callback bearing an unknown state is rejected. It is the replay defence: the key is deleted as it is read, so a second callback with the same state finds nothing. And it is the identity lookup, since the state is what binds this callback to the user who initiated it. Forged, expired, and replayed states are indistinguishable from the outside and all rejected identically.

**The state is consumed before the error branch, not after.** Google sends `state` on error callbacks too. Returning early on `access_denied` discarded the only record of where the user came from, and left a live state replayable for the remainder of its TTL — both fixed. This mattered little while every consent came from an authenticated settings page; it matters a great deal once a declined consent is a stranger's first contact with the product.

PKCE binds the authorization code to the initiating request. Even if a code were intercepted, it cannot be redeemed without the `code_verifier`, which never left the server.

Every failure path ends in a 302 back to the dashboard with a `?google=` outcome, never a JSON error body. A browser sitting on a JSON 500 is a dead end for a human user, so `complete()` catches everything and converts it to a redirect.

`redirectAfter` is client-supplied and feeds that 302, which makes it an open-redirect vector. Only relative, non protocol-relative paths are honoured; `https://evil.com` and `//evil.com` both fall back to the default.

Three persistence details are easy to get wrong:

- Google returns granted scopes as a single space-delimited **string**, not an array. It is split before being written to the `TEXT[]` column, and merged with any previously granted scopes so a narrow response cannot silently revoke capability.
- Google omits `refresh_token` on some re-consents. The column is NOT NULL and the stored token remains valid, so it is only overwritten when a new one actually arrives.
- `expires_in` is relative seconds, not a timestamp.

There is deliberately no explicit `@Transactional` around the callback. The write is a single row, and `SimpleJpaRepository.save()` is already transactional, so the Google network round-trip sits outside any transaction by construction rather than by careful annotation placement — no database connection is held open across a call to a third party.

Phase 4 — Using and Refreshing

GoogleAccessTokenProvider is the only component permitted to return a decrypted Google token. Every future consumer — calendar sync, sheets export — goes through it, so expiry, refresh, and revocation are handled in exactly one place.

```text
load connection → reject unless status = ACTIVE
token_expires_at > now + 60s ?
   yes → decrypt(access_token, token_key_version) → return
   no  → refresh via refresh_token
           200            → re-encrypt, update expiry + last_refreshed_at
           invalid_grant  → status = REVOKED, revoked_at = now, throw
stamp last_used_at
```

The 60-second skew is not padding: a token with three seconds of validity left is already expired by the time the API call it was fetched for actually lands.

`invalid_grant` is treated as terminal and distinguished from every other failure. It means the user revoked access, changed their password, or the grant expired — re-consent is required and retrying is futile. A transient 5xx from Google must *not* mark the connection revoked, so GoogleTokenRefreshException carries an `invalidGrant` flag rather than collapsing both into one error.

Disconnect

Revocation is sent to Google first, then the local row is marked REVOKED. Deleting only the local row would leave a live grant sitting in the user's Google account with no way for the platform to reach it again. Revocation failure is logged but non-fatal, since an already-revoked token answers 400 and the local state must still be updated.

`disconnect` returns the same exception for "this connection is not yours" as for "this connection does not exist", so the endpoint cannot be used to probe which connection UIDs are real.

Who May Write a Connection

`integration.oauth_connections` rows hold live Google credentials, so the consent flow is the only sanctioned writer. The generic CRUD surface at `/api/v1/oauth-connections` had its POST, PUT, and list-all endpoints removed, along with the request DTO that carried client-suppliable `accessToken`/`refreshToken` values. Left in place, POST would have allowed any authenticated user to inject forged credential rows, and the unscoped list-all would have exposed every user's connections. Only an owner-scoped read and a delete remain.

## Token Encryption at Rest

`integration.oauth_connections.access_token` and `refresh_token` are ciphertext. A Google refresh token is effectively a long-lived password to the user's calendar and files; unlike a platform JWT it cannot be rotated by expiry, and it is valuable in a database dump, a backup, or a WAL archive long after any breach.

TokenCipher / AesGcmTokenCipher (`security/crypto/`) implement AES-256-GCM. A fresh 12-byte IV is generated per encryption and prepended to the ciphertext — GCM fails catastrophically on IV reuse, so this is not optional. The 128-bit authentication tag means tampering is detected on decrypt rather than yielding garbage plaintext.

Keys are configured as a version-to-key map with a designated current version, and validated at startup: a key that is not valid Base64 or not exactly 32 bytes fails the boot rather than the first request.

`decrypt` takes the key version as a parameter rather than reading it from the payload, which is what makes the `token_key_version` column do real work. Rotation is a configuration change — add key 2, set current to 2 — after which new writes use the new key while existing rows keep decrypting with the version recorded against them. No re-encryption sweep is required, and no downtime.

TokenEncryptionException is deliberately not mapped in GlobalExceptionHandler. A decryption failure is a server fault, and surfacing cipher details to a client is an information leak, so it falls through to a generic 500.

## Google One-Shot Registration

Registration and delegated consent in a single Google round trip. This is not a third mechanism — it is section 12's authorization-code flow with section 11's identity half of the *same* token response put to use instead of discarded. One exchange already returns `id_token` alongside `access_token`/`refresh_token`; previously the identity was used only to key the connection row.

It applies to **registration only**. Login stays on section 11's GIS ID-token flow, because the one-shot must send `prompt=consent` to guarantee a refresh token and forcing a consent screen on every sign-in is hostile. A returning user who signed in via GIS but holds no ACTIVE connection is nudged on the dashboard, not blocked.

Flow Shape

```text
POST /api/v1/auth/google/authorize      permitAll, no principal
     OAuthState{ mode = SIGN_UP_AND_CONNECT, userUid = null, … } → Redis, 10m
     → { authorizationUrl }

     [ one consent screen ]

GET  /api/v1/integrations/google/callback     the same callback both flows use
     … steps 1-4 of section 12 …
     GoogleOnboardingService.finalizeOnboarding(tokens, identity)   @Transactional
         GoogleAccountResolver.resolve(identity)     ← section 11's three steps
         GoogleConnectionWriter.persist(…)           ← section 12's upsert
         SessionHandoffStore.issue(userUid, 60s)
     → 302 {frontend}{redirectAfter}?google=connected&handoff=<code>

POST /api/v1/auth/google/handoff        permitAll
     GETDEL oauth:handoff:<code> → userUid
     SessionIssuer.issue(user)
     → { accessToken, refreshToken }
```

Why a Handoff Code

The callback lands on the backend and redirects to the frontend, which owns the httpOnly session cookies. The backend cannot set a cookie for another origin, so the session has to cross that boundary somehow. Three options, only one acceptable:

- Tokens in the redirect query string — **no**. They leak into browser history, `Referer`, and any intermediary log.
- Backend sets the cookie on its own domain — **no**. Cross-origin to the BFF, and it abandons the httpOnly model wholesale.
- A one-time handoff code — **yes**, mirroring the OAuthStateStore pattern already proven here.

**Only the user reference is stored, never the tokens.** The JWT pair is minted when the code is exchanged. This is the difference between a lookup key and a bearer credential: the code does transit the URL bar, so what it is worth if observed matters more than how long it lives. It is worth nothing after first use. As a secondary benefit, the 15-minute access token starts its life when the session actually begins rather than at callback time, so a user who stalls on the landing page does not receive a pre-aged token.

TTL is 60 seconds — it only has to survive one redirect. `GETDEL` gives single-use consumption in one atomic command, and expired, replayed and forged codes all fail identically as InvalidSessionHandoffException (401), for the same reason `invalid_state` does.

Transaction Boundary

`finalizeOnboarding` is `@Transactional` and lives on its own bean. Both facts are load-bearing.

Transactional, because this path writes a user, a role assignment, an organization, a membership, a profile, an account link and a connection row. A failure partway through must roll all of it back rather than strand a user with a Google credential and no workspace.

Its own bean, because Spring does not proxy self-invocation — a private method on DefaultGoogleConnectService would silently run with no transaction at all, and the failure would be invisible until the day something actually failed mid-way.

The boundary starts *after* the Google network calls, not around them, preserving section 12's property that no database connection is ever held open across a third-party HTTP round trip. The Redis handoff write does sit inside it, which is fine: Redis is local and sub-millisecond, and if the commit then fails the exception propagates, the code never reaches the browser, and the orphaned key expires in a minute.

Declining Is Not Unticking

Two outcomes that look similar and must end differently:

| User action | Google sends | Result |
|---|---|---|
| Unticks Calendar, continues | `code`, narrow `scope` | account, session, connection with narrow scopes |
| Cancels or closes the window | `error=access_denied` | **nothing** — no account, no session |

Blocking signup on a scope the user just declined is hostile, and they can grant it later through section 12's connect flow, which upserts the same row and merges scopes. So a narrow grant still produces a working account.

The consequence is that **an ACTIVE connection does not imply Calendar access**. `persist` sets ACTIVE unconditionally. Any "is this workspace calendar-connected?" decision must read the `scopes` array — which holds raw Google URI strings, not GoogleScope enum names — never `status`.

One further edge: on a *new* connection with no `refresh_token` in the response, the row is skipped rather than written. The column is NOT NULL with nothing to fall back on, so writing would abort the transaction that is also provisioning the account. Losing a calendar connection is recoverable; losing the registration is not.

Landing Route

`redirectAfter` defaults to a **public** route for this flow, separately from the connect flow's default. The handoff redirect necessarily arrives before any session cookie exists — that is the entire point — so a default under the frontend's authenticated area would be bounced by its route guard and the handoff code silently discarded, after a real consent. The open-redirect guard from section 12 still applies to the client-supplied value.

State Compatibility

OAuthState gained a `mode` field, so the callback branches on `mode == SIGN_UP_AND_CONNECT` rather than on `mode == CONNECT`. States written before the field existed deserialise with a null mode but still carry their `userUid`; branching this way lets every in-flight consent finish normally across a deploy, where the inverse would fail all of them.

## Future Evolution

This identity architecture serves as the foundation for all future authentication mechanisms within the platform.

JWT-based authentication, Widget authentication, and Google Sign-In are all now implemented on top of it, per the sections above.

JWT did not replace AuthenticatedPrincipal — it is a mechanism for reconstructing the same canonical authenticated identity on every request, verified once per request by JwtAuthenticationFilter and handed to the same AuthenticationTokenFactory/SecurityContextHolder integration regardless of actor type.

Google Sign-In likewise did not introduce a new actor type. It produces the same UserPrincipal that Username/Password produces and rejoins the standard pipeline at UserIdentityLoader — which is why it required no change to AuthenticatedPrincipal's permits list, no new IdentityLoader, and no new branch in AuthenticationTokenFactory. A new *credential type* is not the same thing as a new *actor type*, and only the latter costs anything architecturally.

The one-shot registration flow in section 14 is a further illustration: it introduced no actor type, no principal, no IdentityLoader, and no schema change whatsoever. It reuses section 11's resolver and section 12's writer behind one transaction, and adds a single new piece — the session handoff — which exists solely to cross an origin boundary, not to authenticate anything.

Remaining future work follows the same shape: API Keys and Service Accounts are added by introducing a new IdentityLoader (and only an AuthenticationAggregate/PrincipalMapper pair if the actor's identity genuinely requires a multi-table join), adding the new concrete type to AuthenticatedPrincipal's permits list, and adding its branch to AuthenticationTokenFactory's switch — the compiler enforces that the last two steps aren't skipped. Regardless of whether authentication originates from Username/Password, JWT, OAuth, API Keys, or Widget Tokens, the remainder of the platform continues to operate exclusively on AuthenticatedPrincipal.

Known gaps in the Google delegated-authorization flow, in priority order:

- **Workspace provisioning is still incomplete, and registration depends on it.** `UserProvisioningService` now creates the `tenant.tenants` row on `FREE`/`GENERAL` (V139–V141), but it still creates no schedule, availability, or notification preferences — see `SERVER_AGENTS.md`. Section 14 routes through the same component rather than duplicating it, so provisioning failures roll back either registration path.
- **Unverified end to end.** Every component is unit tested against mocks, and the application boots with the full wiring, but no real consent round-trip has been performed. The likeliest failure point is GoogleTokenResponse deserialisation — Jackson 3 databind is paired with 2.x annotations (`com.fasterxml.jackson.annotation`) on this classpath, which is confirmed, but an actual Google payload has never been parsed. This now gates signup, not just calendar sync.
- **No scope enforcement at call time.** Granted scopes are persisted, but nothing yet checks them before a Calendar or Sheets call. That check belongs in the consumer, once one exists. Until then, "did this user actually grant Calendar?" is the frontend's question to ask, against the `scopes` array rather than `status` — see section 14.
- **No concurrency guard on connect.** Two simultaneous consents for the same Google account would both attempt the upsert; the unique constraint on (user_id, provider, provider_account_id) prevents a duplicate row, but the loser currently surfaces as a raw 500 rather than being retried. On the onboarding path the same race would also mean two provisioning attempts, arbitrated by the unique constraints on `auth.users`.
- **No proactive refresh.** Tokens refresh lazily when GoogleAccessTokenProvider is called. A connection unused past its refresh-token lifetime will simply fail on next use rather than being kept warm.
- **Handoff codes are not bound to a browser.** The code is single-use and lives 60 seconds, but nothing ties it to the session that started the flow — an attacker who observed one within that window could exchange it first. Binding it to a nonce cookie set at `/authorize` time would close this; the current mitigation is the TTL and single-use consumption alone.
