# Purpose

This page is the entry point into how PleaseBookMe issues, verifies, and rotates its own login tokens — read this first, before opening `security/token/jwt/` or `security/token/refresh/`. It covers the access/refresh pair every login, registration, and Google flow ends with, and the two very different ways each half of that pair is actually checked. The claim shape both tokens share has its own dedicated page: **[[JWT Claims]]**.

This is deliberately a different subject from **[[Authorization Engine]]**. That engine answers "is this actor allowed to do this?" once a caller is already known. This page is about how the caller becomes known at all, on every single request, without a database round trip — and the one place a database round trip *is* required regardless.

# The Single Most Important Fact About This Design

Access tokens and refresh tokens are structurally the same kind of object — both are JWTs, both signed by the same key, both built by the same `JwtGenerator` — but they are **checked in two completely different ways**, and neither can be substituted for the other:

- An **access token** is verified statelessly: signature, issuer, audience, expiry, and its own `token_type` claim, all checked in memory with zero database access. This is what makes it cheap enough to check on every single request.
- A **refresh token** is verified by a **database row lookup** (`RefreshTokenRepository.findBySecret(...)`), not by re-parsing or re-verifying its JWT signature at all. `DefaultRefreshTokenVerifier` never calls anything from `security/token/jwt/` — it just looks the literal token string up in `auth.refresh_tokens` and checks `revokedAt`/`expiresAt` on the row it finds.

This is worth sitting with, because it's easy to assume "it's a JWT" means "it's verified as a JWT everywhere it's used." A refresh token's actual security here rests on being an unguessable, exact-match string that only this backend could have issued and persisted — not on anyone re-checking its cryptographic signature at redemption time. Re-checking the signature would add nothing the database lookup doesn't already guarantee, since only a legitimately issued token has a matching row at all; the database is the real source of truth for a refresh token's validity, revocation, and expiry, in a way the JWT payload itself never could be (a JWT can't be revoked by editing its own bytes — that's exactly why revocation needs a row).

One direct consequence: an access token can never be redeemed as a refresh token (it was never written to `auth.refresh_tokens`, so the lookup simply fails), and a refresh token can never be accepted as an access token (`DefaultJwtVerifier` hard-rejects anything whose `token_type` claim isn't `ACCESS` — see **[[JWT Claims]]**). The two are cross-rejected by two entirely different mechanisms, not by one shared check.

# End-to-End Flow

**Issuing a pair** — `/login`, `/register`, and the Google one-shot handoff (**[[Google OAuth2 Session]]**) all converge on the same shape:

```
UserPrincipal (already authenticated by some means)
        │
        ├── JwtEngine.issueAccessToken(principal)   → signed JWT, token_type = ACCESS
        └── JwtEngine.issueRefreshToken(principal)  → signed JWT, token_type = REFRESH
        │
        ▼
persist a RefreshTokenEntity row  (secret = the refresh JWT string, owner = USER, expiresAt, revokedAt = null)
        │
        ▼
LoginResponse { accessToken, refreshToken }
```

**Every subsequent request** — the access token only, never touching the database:

```
Authorization: Bearer <accessToken>
        │
        ▼
JwtAuthenticationFilter
        │
        ▼
JwtEngine.verify(token)
        │
        └── DefaultJwtVerifier: signature ✓, issuer ✓, audience ✓, not expired ✓, token_type == ACCESS ✓
        │
        ▼
JwtClaims  →  branch on actorType  →  UserIdentityLoader / WidgetIdentityLoader  →  AuthenticatedPrincipal
        │
        ▼
SecurityContextHolder
```

**Refreshing** — `POST /api/v1/auth/refresh`, the one path that does touch the database:

```
{ refreshToken }
        │
        ▼
AuthController.refresh → AuthService.refreshToken → TokenRefresher.refresh(token)
        │
        ├── RefreshTokenVerifier.verify(token)     Repository.findBySecret(...) — NOT a JWT parse
        │       ├── not found      → RefreshTokenNotFoundException
        │       ├── revokedAt set  → RefreshTokenRevokedException
        │       └── expired        → RefreshTokenExpiredException
        │
        ├── reject if owner != USER   (widget/tenant refresh is not supported yet — see below)
        ├── token.setRevokedAt(now)                  ← the OLD refresh token dies here
        ├── reload the principal fresh from the DB   (not whatever was true at login time)
        ├── JwtEngine.issueAccessToken / issueRefreshToken     ← a brand NEW pair
        └── persist a brand new RefreshTokenEntity row
        │
        ▼
RefreshResponse { accessToken, refreshToken }
```

**Widget bootstrap is asymmetric** — `POST /api/v1/auth/widget/bootstrap` issues an access token *only*:

```java
WidgetPrincipal principal = widgetIdentityLoader.loadByPublicKey(publicKey, secretKey, origin);
String accessToken = jwtEngine.issueAccessToken(principal);
return new WidgetBootstrapResponse(accessToken);
```

No refresh token, no `RefreshTokenEntity` row. `RefreshOwner` (the enum on that entity) already has `WIDGET` and `TENANT` values sitting unused — seeded ahead of the feature, the same forward-scaffolding pattern documented elsewhere (`AGENTS.md`) — but `TokenRefresher.refresh(...)` explicitly rejects any owner other than `USER` today, and `refresh_tokens.user_id` is still `NOT NULL`, so a widget-owned row couldn't even be persisted yet if something tried.

# Component Map

| Component                        | Package                   | Role                                                                                                                                    |
| -------------------------------- | ------------------------- | --------------------------------------------------------------------------------------------------------------------------------------- |
| **[[JWT Claims]]**               | `token/jwt/claims/`       | The actor-agnostic claim shape both token types share.                                                                                  |
| `JwtTokenType`                   | `token/jwt/enums/`        | `ACCESS` \| `REFRESH` — the one claim that tells the two apart.                                                                         |
| `JwtProperties`                  | `token/jwt/config/`       | `issuer`, `audience`, `algorithm`, `secret`, `accessTokenLifeTime`, `refreshTokenLifeTime` — bound from `security.jwt.*`.               |
| `JwtKeyProvider`                 | `token/jwt/crypto/`       | Decodes the single Base64 `security.jwt.secret` into one HMAC `SecretKey`. See "Signing Key Rotation" below.                            |
| `JwtGenerator`                   | `token/jwt/generator/`    | Builds and signs the actual JWT string from a `JwtClaims`.                                                                              |
| `JwtVerifier`                    | `token/jwt/verifier/`     | Parses, verifies, and rejects anything not `token_type = ACCESS`.                                                                       |
| `JwtClaimsFactory`               | `token/jwt/factory/`      | Builds a `JwtClaims` from an `AuthenticatedPrincipal` — one method for access, one for refresh.                                         |
| `JwtEngine` / `DefaultJwtEngine` | `token/jwt/engine/`       | The facade everything else calls: `issueAccessToken`, `issueRefreshToken`, `verify`.                                                    |
| `JwtAuthenticationFilter`        | `token/filter/`           | The only consumer of `JwtEngine.verify(...)` on the request path — see **[[AuthenticatedPrincipal]]** for what it does with the result. |
| `RefreshTokenVerifier`           | `token/refresh/verifier/` | The database-backed check described above — never touches `JwtVerifier`.                                                                |
| `TokenRefresher`                 | `token/refresh/`          | Orchestrates one refresh call: verify, revoke the old row, reload the principal, issue and persist a new pair.                          |

# `AuthController` Endpoints, by What They Actually Return

| Endpoint | Issues | Notes |
| --- | --- | --- |
| `POST /login` | access + refresh | Standard username/password login. |
| `POST /register` | access + refresh | Registration is a complete authentication event — no separate login call needed afterward. |
| `POST /refresh` | access + refresh (**new pair**) | The only endpoint that consumes a refresh token; see "Refresh Token Rotation" below. |
| `GET /me` | — | Reads the already-authenticated principal back out via `CurrentPrincipalProvider`; issues nothing. |
| `POST /widget/bootstrap` | access **only** | See "Widget bootstrap is asymmetric" above. |
| `POST /google` | access + refresh | Sign-In; rejoins the same pipeline as `/login` once the Google identity resolves to a user. |
| `POST /google/authorize` | — | Returns a consent URL; issues nothing yet. |
| `POST /google/handoff` | access + refresh | The one-shot registration exchange — see **[[Google OAuth2 Session]]**. |

# Refresh Token Rotation

This is the rotation that's actually implemented, and it's a **single-use, revoke-then-reissue** scheme: every time a refresh token is successfully redeemed, `TokenRefresher` immediately marks that exact row `revokedAt = now` and writes a brand new row with a brand new secret — the token just spent can never be spent again. A stolen refresh token is therefore only useful up until the next time the legitimate client refreshes; after that, both the thief's and the legitimate client's copies of that specific token are equally dead, and only the new one issued at that moment works.

One thing worth being precise about, because "rotation" in other systems sometimes implies more: this codebase's rotation is a plain use-once-then-die scheme, not a reuse-detection scheme. If a **revoked** token is presented again — which would happen if a stolen token were used *after* the legitimate client had already refreshed — the request is simply rejected (`RefreshTokenRevokedException`). Nothing here treats that specific event as a compromise signal or automatically revokes the user's other active sessions in response. That escalation (a real, common hardening step in OAuth security best practice — "if a revoked token is ever replayed, assume the whole token family is compromised and kill it") is not built here; today a replayed-revoked-token attempt just fails, quietly, like any other invalid request.

`TokenRefresher` also reloads the principal from scratch on every refresh (`UserIdentityLoader.loadByUsername(...)` → `UserPrincipalMapper.map(...)`) rather than trusting anything about the user that was true when the *old* token was issued — so a refresh always reflects the user's current roles, permissions, and account status, not whatever was true minutes or days earlier.

# Signing Key Rotation

This is the concept worth being honest about rather than assuming it exists just because the codebase already has a working example of it elsewhere (see `TokenCipher` in **[[Security Architecture]]** §9, and Google's own JWKS-based key rotation that `DefaultGoogleTokenVerifier` already relies on for ID tokens). **Neither pattern is present here.** `JwtKeyProvider` holds exactly one `SecretKey`, derived from exactly one config value (`security.jwt.secret`) — no key id (`kid`) header, no version claim, no registry of "current key plus still-valid-for-a-while old keys." Rotating the secret is a single config change with no transition window: from the moment the new secret is deployed, `jwtKeyProvider.signingKey()` only ever returns it, and `DefaultJwtVerifier` only ever verifies against it.

What that concretely means is worth walking through, because the blast radius is smaller than "no key rotation" might first suggest:

- **Every currently-outstanding access token stops verifying immediately.** The next request each of those tokens is used on fails at `DefaultJwtVerifier` and gets a 401.
- **Every currently-outstanding refresh token keeps working exactly as before.** `RefreshTokenVerifier` never checks a JWT signature at all — it only looks the token up by its literal string value in `auth.refresh_tokens`. The signing key rotating changes nothing about whether that row exists or is still unrevoked.
- **So the practical effect of a secret rotation is: every logged-in client makes one extra, otherwise-invisible `/refresh` call** the next time its access token is rejected, gets a fresh pair signed with the new secret, and continues on as if nothing happened. This is the same recovery path a client already takes on ordinary access-token expiry — rotating the secret just forces it to happen for everyone at once, rather than staggered across each token's normal lifetime.
- **The actual security payoff still lands where it matters**: if the secret was rotated *because it leaked*, an attacker holding the old secret can no longer forge new access tokens that `DefaultJwtVerifier` will accept, from the moment of rotation onward. What's genuinely missing, compared to a versioned scheme like `TokenCipher`'s, is a graceful multi-key transition window — there's no way to keep accepting old-key-signed tokens for a grace period while phasing in a new one. Given how cheaply legitimate clients recover (one silent refresh call), that missing graceful window is a real gap, but a low-severity one specifically *because* refresh-token validity was already decoupled from the signing key in the first place.

If this ever needs to be built properly, the pattern to follow already exists twice in this codebase: `TokenCipher`'s `{current-version, keys}` map plus a `token_key_version` column recorded per row (see **[[Security Architecture]]** §9), or Google's own `kid`-keyed JWKS approach this backend already consumes as a client. Neither has been ported to this platform's own JWT signing yet.

# See Also

- **[[JWT Claims]]** — the actor-agnostic claim shape both token types share, field by field, and exactly how `token_type` gates access-only verification.
- **[[AuthenticatedPrincipal]]** — what `JwtAuthenticationFilter` reconstructs from a verified access token's claims.
- **[[Google OAuth2 Session]]** — the one-shot registration handoff, which mints its access/refresh pair through this exact same `SessionIssuer` → `JwtEngine` path.
- **[[Security Architecture]]** §7 — this token layer's place inside the platform's overall security architecture.
