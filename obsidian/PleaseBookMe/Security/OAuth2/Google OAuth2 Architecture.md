# Purpose

This page is the entry point into PleaseBookMe's Google OAuth2 architecture — read this first, before opening any controller or service class. It gives the end-to-end shape of every flow and names the component responsible for each step; the low-level pages linked throughout (**[[PKCE OAuth2 Flow]]**) and the source itself are where to go once you know which piece you're looking for.

Everything here lives under `security/oauth/google/` (the Google-specific mechanics: PKCE, URL building, token exchange, ID-token verification), `integration/oauthconnection/` + `integration/oauthstate/` (the persisted connection and the in-flight Redis state), `service/integration/` (orchestration: connect, disconnect, token refresh), and `service/auth/` (the one-shot registration variant). `security/crypto/` (`TokenCipher`) is shared infrastructure, not Google-specific.

---

# Two Mechanisms, Not One

The single biggest source of confusion in this area is treating "Google OAuth" as one thing. It is two, and they share almost no code:

|                   | Sign-In                         | Delegated Authorization                                   |
| ----------------- | ------------------------------- | --------------------------------------------------------- |
| Question answered | Who is this user?               | May we act on their behalf against Calendar/Sheets/Drive? |
| Flow              | ID token, posted by the browser | Authorization code + redirect, handled server-side        |
| Entry point       | `POST /api/v1/auth/google`      | `POST /api/v1/integrations/google/connect`                |
| Produces          | A platform JWT pair             | An encrypted row in `integration.oauth_connections`       |
| Token stored      | None — verified once, discarded | Access + refresh, AES-256-GCM at rest                     |

**This page is about the second one** — Delegated Authorization — since that's where PKCE, the authorization-code exchange, the URL builder, and the Calendar/Sheets connection all live. Sign-In is documented for contrast only; see `SECURITY.md` §11 for its own detail.

A third entry point, **One-Shot Registration**, is not a third mechanism — it's Delegated Authorization with the `id_token` that already comes back in the same token response put to use, instead of discarded. See "One-Shot Registration" below.

---

# End-to-End Flow (Delegated Authorization)

```
Dashboard "Connect Google"
        │
        ▼
POST /api/v1/integrations/google/connect        (Bearer token — CurrentPrincipalProvider.requireUser())
        │
        ▼
DefaultGoogleConnectService.initiate()
        │
        ├── PkceGenerator.generate()             → { verifier, challenge }
        ├── OAuthStateStore.issue(OAuthState, 10m) → Redis, opaque state token
        └── GoogleAuthorizationUrlBuilder.build() → Google's consent URL
        │
        ▼
{ authorizationUrl }  ──►  browser redirects
        │
        ▼
   [ Google consent screen — user picks scopes ]
        │
        ▼
GET /api/v1/integrations/google/callback        (permitAll — top-level browser navigation, no Bearer token)
        │
        ├── OAuthStateStore.consume(state)        Redis GETDEL — CSRF + replay defence + identity lookup, one atomic op
        ├── GoogleTokenClient.exchangeAuthorizationCode(code, verifier)  → GoogleTokenResponse
        ├── GoogleTokenVerifier.verify(id_token)  → GoogleIdentity (sub, email, emailVerified)
        └── GoogleConnectionWriter.persist(user, tokens, identity)
                  └── TokenCipher.encrypt(accessToken / refreshToken)  → integration.oauth_connections
        │
        ▼
302 → {frontend}{redirectAfter}?google=connected
```

---

# Component Map

| Component                                                        | Package                                        | Role                                                                                                                                                                                                                  |
| ---------------------------------------------------------------- | ---------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `PkceGenerator` / `PkceChallenge`                                | `security/oauth/google/pkce/`                  | Generates a random verifier + its SHA-256 challenge. See **[[PKCE OAuth2 Flow]]** — but note the caveat below, this codebase does **not** use Spring's built-in OAuth2 login machinery.                               |
| `GoogleScope`                                                    | `security/oauth/google/authorization/`         | The allow-listed scope vocabulary (`CALENDAR`, `SHEETS`, `DRIVE_FILE`) plus the always-appended base scopes (`openid`, `email`, `profile`). A caller can only ever request from this enum — never a raw scope string. |
| `GoogleAuthorizationUrlBuilder`                                  | `security/oauth/google/authorization/`         | Builds Google's consent-screen URL from a `ClientRegistration`, the PKCE challenge, the state, and the resolved scope list.                                                                                           |
| `OAuthState` / `OAuthFlowMode`                                   | `integration/oauthstate/model/`                | The in-flight record parked in Redis between "initiate" and "callback": `mode`, `userUid` (null for a not-yet-registered signup), `codeVerifier`, `scopes`, `redirectAfter`.                                          |
| `OAuthStateStore` / `RedisOAuthStateStore`                       | `integration/oauthstate/store/`                | `issue(state, ttl)` → opaque token; `consume(token)` → `GETDEL`, single-use, atomic. No entity, no table — Redis-backed by design.                                                                                    |
| `GoogleTokenClient` / `DefaultGoogleTokenClient`                 | `security/oauth/google/client/`                | The only component that talks to Google's token endpoint: authorization-code exchange, refresh, and revoke.                                                                                                           |
| `GoogleTokenVerifier`                                            | `security/oauth/google/verifier/`              | Verifies the `id_token` that comes back alongside the access/refresh tokens — shared with the Sign-In mechanism.                                                                                                      |
| `GoogleConnectService` / `DefaultGoogleConnectService`           | `service/integration/`                         | Orchestrates initiate → callback → list → disconnect. The class in the diagram above.                                                                                                                                 |
| `GoogleConnectionWriter` / `DefaultGoogleConnectionWriter`       | `service/integration/`                         | The single upsert into `oauth_connections` — encrypts both tokens, merges granted scopes, shared by the Connect flow and One-Shot Registration.                                                                       |
| `TokenCipher` / `AesGcmTokenCipher`                              | `security/crypto/`                             | AES-256-GCM encrypt/decrypt with a versioned key. Not Google-specific — anything storing a long-lived third-party credential goes through this.                                                                       |
| `GoogleAccessTokenProvider` / `DefaultGoogleAccessTokenProvider` | `service/integration/`                         | The **only** sanctioned way to get back a decrypted, guaranteed-fresh access token — refreshes lazily, marks the connection `REVOKED` on a terminal `invalid_grant`.                                                  |
| `OAuthConnectionEntity`                                          | `integration/oauthconnection/`                 | The persisted row: provider, `providerAccountId`, encrypted `accessToken`/`refreshToken`, `tokenKeyVersion`, `scopes[]`, `status`.                                                                                    |
| `DestinationCalendarEntity` / `DestinationSheetsEntity`          | `integration/calendar/`, `integration/sheets/` | Not the OAuth connection itself — see "Calendar & Sheets Connection" below.                                                                                                                                           |
| `GoogleOnboardingService` / `DefaultGoogleOnboardingService`     | `service/auth/`                                | The One-Shot Registration variant of the callback — see below.                                                                                                                                                        |

---

# Authorization Code Exchange

The callback (`GET /api/v1/integrations/google/callback`) is deliberately the only place a code is ever exchanged, and it runs in a strict order:

1. **Consume the state first, before even looking at `error`.** `OAuthStateStore.consume` is a Redis `GETDEL` — it deletes as it reads, so it does three jobs in one atomic call: CSRF defence (an attacker can't forge a state they never received), replay defence (a second callback with the same state finds nothing), and identity lookup (the state is what says who initiated this). Google sends `state` on a declined-consent callback too, so consuming it before branching on `error` is what lets a decline still discard the flow cleanly instead of leaving a live, replayable state sitting in Redis.
2. **Exchange the code for tokens** via `GoogleTokenClient`, sending the `code_verifier` alongside the `code` — this is what makes the code useless to anyone who merely observed it in transit. Only the party holding the original verifier can complete the exchange.
3. **Verify the `id_token`** that comes back in the same response, via `GoogleTokenVerifier` — this is how the callback learns *whose* Google account just granted consent, without a second round trip.
4. **Persist**, via `GoogleConnectionWriter` — encrypt both tokens, merge the granted scopes with whatever was already stored (a narrower re-consent must never silently revoke a previously granted scope), mark the connection `ACTIVE`.

There is deliberately no `@Transactional` around this method. The database write is one row (`SimpleJpaRepository.save()` is already transactional on its own), so the Google network round-trip sits outside any transaction by construction — nothing holds a Hikari connection open across a third-party HTTP call.

Every failure path — declined consent, expired/forged/replayed state, a runtime exception mid-exchange — ends in a `302` back to the frontend with a `?google=<outcome>` query parameter, never a JSON error body. A browser sitting on a raw 500 is a dead end for a human mid-consent.

---

# PKCE

`PkceGenerator` produces a 64-byte random verifier and its SHA-256 challenge; `GoogleAuthorizationUrlBuilder` sends the challenge to Google up front, and the callback later sends the original verifier back during the token exchange. Google only issues tokens if hashing the verifier reproduces the challenge it cached — so a code intercepted in the browser is useless without the verifier, which never leaves the server.

**Read the existing [[PKCE OAuth2 Flow]] note with one correction in mind**: it describes Spring Security's built-in OAuth2 login machinery (`DefaultOAuth2AuthorizationRequestResolver`, `HttpSessionOAuth2AuthorizationRequestRepository`, an `HttpSession`-backed verifier). **That is not what this codebase does.** There is no Spring Security OAuth2 client auto-configuration in use here at all — PKCE generation is the hand-rolled `PkceGenerator` above, and the verifier is parked in **Redis** via `OAuthStateStore` (10-minute TTL, single-use `GETDEL`), not in an `HttpSession`, because the application is fully stateless (`SecurityConfig` → `SessionCreationPolicy.STATELESS`). The *concept* the note explains (challenge sent up front, verifier redeemed at exchange time) is correct and worth keeping; the concrete Spring classes it names are not the ones actually wired up.

Three non-obvious query parameters ride alongside the PKCE pair on every authorization URL: `access_type=offline` (without it, no refresh token comes back — the connection dies in an hour), `prompt=consent` (forces a refresh token on *every* consent, not just the first — without it, a reconnecting user gets nothing to write into the `NOT NULL refresh_token` column), and `include_granted_scopes=true` (incremental authorization, so connecting Sheets later doesn't silently drop a previously granted Calendar scope).

---

# Token Encryption at Rest

A Google refresh token is an effectively-permanent credential to a user's Calendar, Sheets, and Drive — unlike a platform JWT, it doesn't expire on its own, which makes it valuable in a database dump or backup long after any breach. `TokenCipher` (`AesGcmTokenCipher`) encrypts both `accessToken` and `refreshToken` with AES-256-GCM before either ever reaches `oauth_connections` — fresh IV per encryption, 128-bit auth tag, versioned keys so rotation is a config change rather than a re-encryption sweep. This is genuinely shared infrastructure, not something built for Google specifically — see **[[Security Architecture]]** §9 for the full mechanism; it's summarized here only because the Connect flow is its primary consumer today.

`GoogleAccessTokenProvider` is the only component allowed to hand back a *decrypted* token. It checks the connection is `ACTIVE`, returns the cached access token if it has more than 60 seconds of validity left (a token expiring in the next few seconds is already dead by the time an API call lands), and otherwise refreshes via `GoogleTokenClient` — re-encrypting the result and updating `tokenExpiresAt`/`lastRefreshedAt`. A refresh that fails with `invalid_grant` is terminal (the user revoked access, changed their password, or the grant lapsed) and marks the connection `REVOKED`; any other failure is transient and does not touch `status`. Nothing besides this provider is expected to call `TokenCipher.decrypt(...)` directly on an OAuth connection's tokens.

---

# Calendar & Sheets Connection

**`OAuthConnectionEntity` is not the same thing as "this service syncs to this calendar."** The connection is *one row per (user, provider)* — proof that this user granted this app some set of Google scopes, and the encrypted material to act on it. `DestinationCalendarEntity` (`integration.destination_calendars`) and `DestinationSheetsEntity` (`integration.destination_sheets`) are the separate, much smaller link that says *which specific external calendar or spreadsheet a given `Service` writes to* — each row is just `(user_id, service_id, integration_type, external_id)`, both FKing into `auth.users` and `core.services`.

The relationship: a `Service` optionally points at a `DestinationCalendarEntity`/`DestinationSheetsEntity` (`ServiceEntity.destinationCalendarId`/`destinationSheetsId`), and *that* row exists only meaningfully once the owning user also holds an `ACTIVE` `OAuthConnectionEntity` with the right scope granted. The destination tables answer "where does this service's data go"; the connection table answers "are we actually allowed to write there right now." Checking the latter is always a `scopes` array lookup on the connection (raw Google URI strings, e.g. `https://www.googleapis.com/auth/calendar`, not `GoogleScope` enum names) — **never** a check of `status == ACTIVE` alone, since `persist()` sets `ACTIVE` unconditionally even when the user unticked every non-base scope on the consent screen.

Nothing yet actually calls `GoogleAccessTokenProvider` to perform a real Calendar or Sheets write — that consumer (the sync job that reads a `DestinationCalendarEntity`, fetches a token, and calls the Google Calendar API) is Platform v2.0.0 work per the roadmap in `CLAUDE.md`, not yet built. The plumbing above — the connection, the encryption, the token provider — is deliberately in place ahead of that consumer, the same forward-scaffolding pattern the codebase already follows elsewhere (see `AGENTS.md`).

---

# One-Shot Registration

`POST /api/v1/auth/google/authorize` runs the exact same `authorize(...)` path as `/connect` above, with two differences: `mode = SIGN_UP_AND_CONNECT` instead of `CONNECT`, and `userUid = null` (nobody is logged in yet — this endpoint is `permitAll`). The callback is the **same** `GET /api/v1/integrations/google/callback` both flows share; it branches on `oauthState.mode()`:

```
mode == SIGN_UP_AND_CONNECT
        │
        ▼
GoogleOnboardingService.finalizeOnboarding(tokens, identity)     @Transactional
        │
        ├── GoogleAccountResolver.resolve(identity)     — resolve-or-provision a user (shared w/ Sign-In)
        ├── GoogleConnectionWriter.persist(...)          — the SAME writer the Connect flow uses
        └── SessionHandoffStore.issue(userUid, 60s)      — Redis, single-use
        │
        ▼
302 → {frontend}{redirectAfter}?google=connected&handoff=<code>
        │
        ▼
POST /api/v1/auth/google/handoff
        │
        ▼
GoogleOnboardingService.exchangeHandoff(code)  — GETDEL → userUid → SessionIssuer.issue(user)
        │
        ▼
{ accessToken, refreshToken }
```

The handoff code exists because the callback lands on Spring and redirects to the Next.js frontend, which owns the httpOnly session cookies — Spring can't set a cookie on another origin, and tokens riding in the redirect's query string would leak into browser history and logs. Only the `userUid` crosses that boundary through Redis; the JWT pair itself is minted only when `/handoff` is called, so the value sitting in the URL bar is a dead lookup key, not a bearer credential.

This flow reuses `GoogleConnectionWriter` and `GoogleAccountResolver` rather than duplicating them — the one genuinely new piece is the handoff. Full detail: `SECURITY.md` §14, `docs/services/auth/GOOGLE_ONE_SHOT_BACKEND.md`.

---

# See Also

- **[[PKCE OAuth2 Flow]]** — the PKCE mechanism in isolation; read alongside the correction above.
- **[[Security Architecture]]** §9 — `TokenCipher` in the context of the platform's broader security architecture, not just Google.
- **[[AuthenticatedPrincipal]]** — `UserPrincipal` is what `CurrentPrincipalProvider.requireUser()` returns to gate `/connect`; Sign-In and One-Shot Registration both produce it via the shared `GoogleAccountResolver` → `SessionIssuer` path.
- `SECURITY.md` §§11–14 — the authoritative prose narrative this page is distilled from, including the known gaps (unverified end-to-end against a real Google payload, no proactive refresh, handoff codes not bound to a browser).
