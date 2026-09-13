# What is JWT Claim?

`JwtClaims` is the one claim shape used for both access and refresh tokens, and for both actor types this platform issues tokens to (User, Widget). There is no separate claim record per token type or per actor — one shape, reused, which is exactly what lets `JwtEngine`, `JwtAuthenticationFilter`, and everything else in `security/token/` stay generic instead of branching per actor. See **[[Access & Refresh Architecture]]** for how this fits into the full issue/verify/refresh lifecycle — this page is only about the claims themselves.

Package: `com.pleasebookme.server.security.token.jwt.claims`.

```java
public record JwtClaims(
    AuthenticatedActorType actorType,

    UUID subject,
    UUID tenant,

    UUID tokenId,
    JwtTokenType tokenType,

    Instant issuedAt,
    Instant expiresAt
) {}
```

# Fields

| Field | Meaning | Populated from |
| --- | --- | --- |
| `actorType` | Which kind of actor this token belongs to (`USER`, `WIDGET`, ...). | `principal.actorType()` — see **[[AuthenticatedPrincipal]]**. |
| `subject` | The actor's own identifier. | `principal.subject()`. |
| `tenant` | The tenant this actor is scoped to, if any. | `principal.tenantUid()` — always `null` for a `UserPrincipal`, always populated for a `WidgetPrincipal`. See **[[AuthenticatedPrincipal]]** for why. |
| `tokenId` | A fresh, random identifier for *this specific token*, not the actor. | `UUID.randomUUID()`, generated new every time a token is issued — access and refresh tokens issued in the same call get two different `tokenId`s. |
| `tokenType` | `ACCESS` or `REFRESH`. | Fixed by which `JwtClaimsFactory` method built this claims object. |
| `issuedAt` / `expiresAt` | The token's validity window. | `Instant.now()` and `now + lifetime`, where the lifetime is `accessTokenLifeTime` or `refreshTokenLifeTime` from `JwtProperties`, depending on `tokenType`. |

`DefaultJwtClaimsFactory` builds this record straight off an `AuthenticatedPrincipal`, with zero branching per actor type:

```java
public JwtClaims accessClaims(AuthenticatedPrincipal principal) {
    Instant now = Instant.now();
    return new JwtClaims(
        principal.actorType(), principal.subject(), principal.tenantUid(),
        UUID.randomUUID(), JwtTokenType.ACCESS,
        now, now.plus(jwtProperties.accessTokenLifeTime())
    );
}
```

`refreshClaims(...)` is identical except for `JwtTokenType.REFRESH` and `refreshTokenLifeTime` — the two factory methods differ in exactly two places, everything else about the actor is read the same way.

# How These Fields Become an Actual JWT

`DefaultJwtGenerator` maps every field above onto either a standard registered JWT claim or a custom one:

| `JwtClaims` field | JWT claim | Registered or custom |
| --- | --- | --- |
| `subject` | `sub` | Registered — `.toString()`'d from the `UUID`. |
| `tokenId` | `jti` | Registered (`.id(...)`). |
| `issuedAt` / `expiresAt` | `iat` / `exp` | Registered. |
| `tenant` | `tenant` | Custom. Passed as `null` when `principal.tenantUid()` is `null` — jjwt's `.claim(name, null)` drops the claim entirely rather than writing a null value, so a `UserPrincipal`'s token simply has no `tenant` claim at all, not a present-but-null one. |
| `actorType` | `actor_type` | Custom — the enum's `.name()`, a plain string. |
| `tokenType` | `token_type` | Custom — same treatment, and the one claim verification actually branches on. |

Two more registered claims are set from configuration rather than from `JwtClaims` at all: `iss` (`JwtProperties.issuer()`) and `aud` (`JwtProperties.audience()`) — fixed, platform-wide values, the same on every token regardless of actor.

Signing is `HS256` against the single key `JwtKeyProvider` returns — see **[[Access & Refresh Architecture]]** for what that means for key rotation.

# Verification Reconstructs the Same Record — With One Hard Gate

`DefaultJwtVerifier` does the reverse mapping, pulling the same claims back out to rebuild a `JwtClaims`, but only after three checks jjwt performs during parsing (signature, `requireIssuer`, `requireAudience` — an exact match against the configured issuer/audience, not merely "a value is present") and one manual check afterward — expiry, re-thrown as this codebase's own `TokenExpiredException` rather than left as jjwt's own exception type.

The verification-time detail that matters most for the access/refresh split lives here:

```java
private void validateTokenType(Claims claims) {
    String tokenType = claims.get("token_type", String.class);
    if (!"ACCESS".equals(tokenType)) {
        throw new JwtException("Invalid token type");
    }
}
```

`DefaultJwtVerifier` — and therefore `JwtEngine.verify(...)`, and therefore `JwtAuthenticationFilter` — will **only ever accept a token whose `token_type` claim is `ACCESS`**. A structurally valid, correctly signed, unexpired refresh token still fails this check and is rejected. This is not an oversight; it's the entire reason a refresh token needs its own, completely separate verification path (`RefreshTokenVerifier`, a database lookup, not a JWT parse at all) instead of reusing this one — see **[[Access & Refresh Architecture]]** for the full reasoning behind that split, and why it turns out to matter less than it might sound like for signing-key rotation.

`tenantClaim` is read back with the same null-aware treatment it was written with: absent claim → `null` on the reconstructed record, present claim → parsed back to a `UUID`.

# See Also

- **[[Access & Refresh Architecture]]** — the full issue/verify/refresh lifecycle this claim shape is embedded in, including why access and refresh tokens are verified through two entirely different mechanisms.
- **[[AuthenticatedPrincipal]]** — the sealed interface every `JwtClaims` is ultimately derived from and reconstructed back into.
