Before anything else, one bit of vocabulary that matters more than it looks: when this page says "session," it does not mean a server-held `HttpSession` sitting in memory somewhere. This backend is fully stateless — there is no server-side session object anywhere in it. A "session," here, is just the everyday name for *the access/refresh token pair that proves someone is logged in*, plus the one database row (a `RefreshTokenEntity`) that makes the refresh half of that pair revocable. "Issuing a session" means minting that pair and persisting that row — nothing more exotic than that.

With that settled, the real question this page exists to answer: **why does One-Shot Registration need to issue a session at all?**

# Why a Session Has to Be Issued

Walk through what actually happens without one, and the answer becomes obvious in hindsight. One-Shot Registration's entire promise, from the user's side, is a single consent screen that both creates a PleaseBookMe account *and* connects Google — one click, and you're done. Suppose the backend did everything described in **[[Google OAuth Modes]]** — resolved or provisioned a user, persisted the Google connection, verified the identity — and then simply redirected the browser back to the site with no session in hand. What would the person actually experience? They'd land back on PleaseBookMe having just finished a real Google consent screen, with a real account now sitting in the database behind the scenes... and no way to prove it's theirs. There's no password to log in with — a Google-provisioned account was never given one. There's no cookie, no token, nothing distinguishing them from a stranger who just landed on the homepage. The "sign up" half of "sign up and connect" would have silently failed to deliver the one thing it promised: ending the flow already logged in.

So a session isn't an optional nicety tacked onto the end of this flow — it's the second half of what the feature is *for*. Registration without a doorway back into the account you just created isn't really registration from the user's point of view; it's just an account existing that nobody can get into yet.

# Why It Can't Be Issued Directly Inside the Callback

Granted that a session has to exist by the end of this — why not just have the callback itself set the session cookie and be done with it? Two structural facts about this system rule that out.

First, this backend and the frontend are different origins, and the callback (`GET /api/v1/integrations/google/callback`) is a request that lands on the *backend's* origin. A cookie set from there is a cookie on the backend's domain — useless to the frontend, which is what actually reads the session cookie on every subsequent page load. The callback simply cannot reach across that boundary with a `Set-Cookie` header.

Second, putting the actual tokens into the redirect's query string instead — `?accessToken=...&refreshToken=...` — would work mechanically, but it would mean handing out live, bearer credentials through a channel that's already known to be exposed: browser history, `Referer` headers, proxy and access logs, anywhere that redirect URL happens to get written down. That's precisely the same class of exposure PKCE exists to defend the authorization code against (see **[[PKCE Challenge]]**) — it would be strange to protect the code that carefully and then leak the session tokens through the exact same door two steps later.

What's needed is something that can cross the origin boundary safely, survive exactly one redirect, and be worth nothing to anyone who happens to see it. That's `SessionHandoffStore`.

# `SessionHandoffStore` — Crossing the Boundary Without Crossing the Tokens

The trick is the same one this codebase already uses for the OAuth `state` parameter (see **[[Google State]]**): don't send the thing that matters, send a disposable reference to it, and keep the thing that matters entirely server-side.

```java
public interface SessionHandoffStore {
    String issue(UUID userUid, Duration ttl);
    Optional<UUID> consume(String code);
}
```

`issue` generates a random 32-byte code, writes it into Redis under `oauth:handoff:<code>` with `SETNX` (so a freshly issued code can never silently overwrite a still-live one), and stores exactly one thing against it: the `userUid` this session is for. Not a token. Not a claim. Just enough information to say *who* this session belongs to — the actual JWT pair is minted later, by a completely different component, only once the code is redeemed.

```java
String code = encoder.encodeToString(bytes);

redisTemplate.opsForValue().setIfAbsent(KEY_PREFIX + code, userUid.toString(), ttl);

return code;
```

`consume` reads it back with `GETDEL` — get and delete in one atomic step, exactly like `OAuthStateStore` — so a code is good for exactly one redemption. A second attempt, whether it's a retry, a replay, or someone who managed to observe the code in transit, finds nothing.

The TTL here is 60 seconds, noticeably shorter than the 10 minutes an `OAuthState` gets, and that difference says something real about what each one has to survive. An `OAuthState` has to live through however long a real person takes to look at Google's consent screen and decide whether to click through — genuinely unpredictable, potentially minutes. A handoff code only has to survive one HTTP redirect followed by one immediate `POST` the frontend fires the instant it notices the `handoff` query parameter — there's no human deliberation in between at all, so there's no reason to give it a human-sized window.

# `SessionIssuer` — Where the Actual Session Gets Minted

The handoff code only ever carries a `userUid`. The component that turns that back into a real, usable session is `SessionIssuer`, and it's worth reading its own interface comment, because it explains exactly why this had to be its own thing rather than reusing whatever mints a session on an ordinary login:

```java
// Mints an access/refresh pair for a user who has already been authenticated by
// some other means, and persists the refresh token.
//
// Takes a UserEntity rather than a principal or an Authentication because the
// handoff exchange has neither: the user was authenticated by Google, minutes
// earlier, in a different request.
public interface SessionIssuer {
    LoginResponse issue(UserEntity user);
}
```

An ordinary login has a `LoginRequest` with a password to check, right there, in the same request that ends with tokens being issued. The handoff exchange (`POST /api/v1/auth/google/handoff`) has none of that — by the time it runs, all the actual authenticating happened minutes earlier, in the callback, against Google. All this request has is a code, and all that code resolves to is a bare user id. So `SessionIssuer.issue` takes the one thing that's actually available — a `UserEntity` — and does the rest of the work from scratch:

```java
AuthenticationAggregation aggregation = userIdentityLoader.loadByUsername(user.getUsername());
UserPrincipal principal = userPrincipalMapper.map(aggregation);

String accessToken = jwtEngine.issueAccessToken(principal);
String refreshToken = jwtEngine.issueRefreshToken(principal);

RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.builder()
    .secret(refreshToken)
    .owner(RefreshOwner.USER)
    .user(user)
    .deviceName(null)
    .createdAt(now)
    .expiresAt(now.plusSeconds(jwtProperties.refreshTokenLifeTime().toSeconds()))
    .build();
refreshTokenRepository.save(refreshTokenEntity);

return new LoginResponse(accessToken, refreshToken);
```

Notice it rebuilds a full `UserPrincipal` through the exact same `UserIdentityLoader` → `UserPrincipalMapper` pipeline every other authentication path uses (see **[[AuthenticatedPrincipal]]**) rather than trusting anything already known about the user from earlier in the flow. That's deliberate: whatever roles, permissions, or account status the user has *right now* — not whatever was true minutes ago when Google's consent screen was still open — is what ends up baked into the tokens. And the refresh token isn't just handed back; it's written to `auth.refresh_tokens` as a real, revocable row, the same as any other login — a refresh token in this system is only ever valid because a live database row backs it, not merely because it carries a valid signature.

The result, `LoginResponse { accessToken, refreshToken }`, is the exact same shape an ordinary `/login` call returns. From the frontend's point of view, finishing a handoff exchange and finishing a normal login are indistinguishable — both end with the same two strings to store, through the same code that stores them.

**Worth being honest about**: `AuthServiceImpl` — the component behind ordinary `/login`, `/register`, and `/refresh` — does not itself call `SessionIssuer`. It contains its own separate, near-identical block of "mint access token, mint refresh token, persist a `RefreshTokenEntity`" logic in three places. `SessionIssuer` was built specifically for the one path that has no `Authentication` or `UserPrincipal` sitting in context to reuse — a real, narrow gap in the existing pipeline — rather than as a shared extraction the other paths were refactored to use as well. The two produce the same shape of result today, but they are two separate implementations of that same idea, not one shared one.

# The Whole Handoff, End to End

```
GET /api/v1/integrations/google/callback         (mode == SIGN_UP_AND_CONNECT)
        │
        ▼
GoogleOnboardingService.finalizeOnboarding(tokens, identity)     @Transactional
        │
        ├── GoogleAccountResolver.resolve(identity)      → UserEntity (existing or newly provisioned)
        ├── GoogleConnectionWriter.persist(user, tokens, identity)
        └── SessionHandoffStore.issue(user.getUserUid(), 60s)     → Redis: oauth:handoff:<code> = userUid
        │
        ▼
302 → {frontend}{redirectAfter}?google=connected&handoff=<code>
        │
        ▼
   [ frontend sees ?handoff=<code>, immediately fires the next request ]
        │
        ▼
POST /api/v1/auth/google/handoff
        │
        ▼
GoogleOnboardingService.exchangeHandoff(code)
        │
        ├── SessionHandoffStore.consume(code)     GETDEL → userUid (or empty → InvalidSessionHandoffException, 401)
        ├── UserRepository.findByUserUid(userUid)
        └── SessionIssuer.issue(user)             → mints access/refresh, persists RefreshTokenEntity
        │
        ▼
{ accessToken, refreshToken }
```

`finalizeOnboarding` runs inside one transaction covering the user resolution, the connection write, and the handoff issuance together — if any part fails, none of it sticks, rather than leaving, say, a newly provisioned user with no way to ever log in because the handoff code never got written. The Redis write sitting inside that transaction is fine specifically because Redis is local and effectively instantaneous — if the surrounding transaction then rolled back, the exception propagates before the code ever reaches the browser, and the orphaned Redis key simply expires on its own a minute later.

# See Also

- **[[Google OAuth Modes]]** — where `finalizeOnboarding` fits inside the full `SIGN_UP_AND_CONNECT` path, and why `CONNECT` never needs any of this (a `CONNECT` request is already made by someone who's already logged in — there's no session to hand back because one was never lost).
- **[[Google State]]** — the same single-use, `GETDEL`-backed Redis pattern, used one step earlier in the flow for a different purpose (tying the callback to the request that started it, rather than crossing an origin boundary).
- **[[Google Identity Model]]** — how `GoogleAccountResolver` turns the verified Google identity into the `UserEntity` that `SessionIssuer` is ultimately handed.
- **[[AuthenticatedPrincipal]]** — the `UserPrincipal` that `SessionIssuer` rebuilds from scratch via `UserIdentityLoader`/`UserPrincipalMapper`, the same pipeline every other authentication path shares.
