Every OAuth2 authorization-code flow has an awkward gap in it: the request that kicks the flow off (our backend redirecting the browser to Google) and the request that finishes it (Google redirecting the browser back to us) are two completely separate HTTP requests, minutes apart, with nothing in between but the user staring at a Google consent screen. Something has to tie those two requests together — to prove the callback we're about to act on is really the continuation of a flow *we* started, for *this* user, and not a forged or replayed request pointed at our callback URL. That something is the OAuth **`state`** parameter.

# What `state` Is For, Conceptually

The OAuth2 spec's own answer to this is almost embarrassingly simple: the client makes up an opaque value, tucks it into the authorization request as a query parameter named `state`, and Google is obligated to hand that exact same value back — unchanged — on the callback. Google never looks at it, never interprets it, never does anything with it except echo it. Whatever meaning `state` carries is entirely up to the client that generated it.

Most OAuth explanations stop there and treat `state` as a single random string used only to prove "yes, I generated this." PleaseBookMe leans on it for more than that: because the backend runs completely stateless (no `HttpSession`, no server-side login session sitting between the two requests — see the `SecurityConfig` note in **[[Google OAuth2 Architecture]]**), the `state` value is also the *only* thread connecting the callback back to everything the initiating request knew: which user asked for this, what they asked to connect, where to send them afterward, and the secret PKCE verifier the callback will need to actually redeem the code. None of that survives anywhere else between the two requests. `state` is where it lives.

# The Two Things Called "State" — Don't Conflate Them

This is the single easiest thing to get confused, so it's worth separating up front. There are two different objects in play, and only one of them is ever visible outside the backend:

- **The token.** A short, random, opaque string — 32 bytes of `SecureRandom`, Base64-URL encoded. This is the only thing that actually travels as the `state` query parameter, in both directions: out to Google on the authorization redirect, and back from Google on the callback. To anyone watching the network, browser history, or a proxy log, this string means nothing — it isn't the user's identity, isn't a session id, isn't anything decodable.
- **The payload.** Everything the token is really standing in for — captured in the `OAuthState` record below. This never leaves the backend. It's written to Redis, keyed by the token, and read back out of Redis when the callback arrives holding that same token. The browser, and anyone watching it, never sees this payload at all.

So when Google redirects back with `?code=...&state=abc123`, `abc123` is meaningless on its own. What makes it useful is that the backend can use it as a key to look up everything it actually needs.

# What `OAuthState` Actually Carries

```java
public record OAuthState(
    OAuthFlowMode mode,
    UUID userUid,           // null when mode is SIGN_UP_AND_CONNECT
    String codeVerifier,
    List<String> requestedScopes,
    String redirectAfter
) {}
```

Reading it field by field tells the whole story of what the callback needs to know that it has no other way of finding out:

- **`mode`** says which of the two things this authorization request actually is — a returning, already-logged-in user connecting their Google account (`CONNECT`), or a brand-new visitor registering and connecting in one motion (`SIGN_UP_AND_CONNECT`). The callback is one shared endpoint for both flows, so this field is how it tells them apart. The distinction itself — what each mode actually does differently — has its own page: **[[Google OAuth Modes]]**.
- **`userUid`** is who to attach the resulting connection to — except when it can't be, because nobody's logged in yet. A brand-new signup obviously has no user yet at the moment the authorization request goes out, so this field is `null` in exactly that one case, and the callback has to provision a user before it can do anything with it.
- **`codeVerifier`** is the [[PKCE Challenge]] secret generated alongside this same authorization request. It has to survive somewhere between "generate it" and "hand it back to Google at token-exchange time," and this is that somewhere. The mechanism this protects against is its own subject.
- **`requestedScopes`** remembers which Google permissions were actually asked for, so the callback can hand the same list forward when it eventually persists the connection.
- **`redirectAfter`** is where to send the user's browser once everything is done — the dashboard settings page for a normal connect, a public landing page for a fresh signup (since no session cookie exists yet to gate a dashboard route). It travels here because the callback, running minutes later as a completely different request, has no other way of knowing what page the user was on when they clicked "Connect."

None of this is a business record in the usual sense — there's no entity, no table, no repository. It's a receipt for one specific authorization attempt, meant to be read exactly once and then thrown away.

# Where It Actually Lives: Redis, Not the Browser, Not the Database

`RedisOAuthStateStore` is the component that owns the token/payload split described above. Issuing a state looks like this:

```java
byte[] bytes = new byte[STATE_BYTES];       // 32
secureRandom.nextBytes(bytes);
String token = encoder.encodeToString(bytes);

redisTemplate.opsForValue().setIfAbsent(
    "oauth:state:" + token,
    objectMapper.writeValueAsString(state),
    ttl                                      // 10 minutes
);

return token;
```

A fresh random token is generated, the entire `OAuthState` payload is serialized to JSON and written into Redis under a key built from that token, and only the bare token — never the payload — is handed back to the caller to embed in the authorization URL. Ten minutes is the whole allotted lifetime of an in-progress consent: enough time for a real human to look at Google's consent screen and click through it, short enough that an abandoned flow doesn't sit around as a lingering resource.

`setIfAbsent` (Redis's `SETNX`) is used instead of a plain write, which is a small piece of belt-and-suspenders correctness rather than a defense against a realistic attack — two independently generated 32-byte random tokens colliding is not something that happens in practice. What `SETNX` buys is a hard, loud failure instead of a silent one *if* it ever somehow did happen: a second issued state can never accidentally overwrite a still-live one sitting under the same key.

Consuming a state, on the callback side, is the interesting half:

```java
String payload = redisTemplate.opsForValue().getAndDelete("oauth:state:" + state);
```

`GETDEL` reads the value and deletes the key in one atomic round trip. That single operation is doing three separate jobs at once, and it's worth naming all three, because none of them would hold if the read and the delete were two separate steps:

1. **It proves the callback is legitimate.** Only a token this backend itself issued has a matching Redis key at all — nobody could forge a `state` value out of thin air and have it resolve to anything.
2. **It makes replay impossible.** The instant the legitimate callback reads the key, it's gone. A second request — a network retry, a malicious replay, a user double-clicking a browser back button and resubmitting — finds nothing, because there's nothing left to find.
3. **It's the identity lookup.** The whole reason the callback can say "ah, this is user X, connecting with verifier Y, wanting to end up back on page Z" is that all of that was sitting in the payload this one read just recovered.

If any of these three were split into separate steps — check-then-delete, for instance — there'd be a window between the check and the delete where two requests could both see the key still present and both proceed, which is exactly the race an atomic primitive exists to close.

# When Consumption Comes Back Empty

`consume(...)` returns an `Optional<OAuthState>`, and an empty result can mean any of three genuinely different things: the token never existed (forged), the token existed once but was already used (replayed), or the token existed but its ten minutes ran out (expired). The callback does not — and should not — try to tell these apart. All three collapse into the exact same outcome: reject the callback, redirect the user back with an `invalid_state` marker, and log a warning. Distinguishing them would only hand an attacker useful information about *why* their probe failed; treating them identically gives away nothing.

# Why Redis, and Not a Signed Token or the Database

It's worth being explicit about the alternative this design didn't take, because it's the obvious first idea: why not make `state` a self-contained signed value (a JWT, say) instead of a bare opaque lookup key? A signed token would avoid the Redis round trip entirely — the callback could verify it locally with no external lookup.

The reason that doesn't work here is replay. A signed token, once issued, remains valid and verifiable for as long as its expiry allows — there's nothing to delete, because there's nothing external for it to point at. Reusing the same `state` value twice would look exactly as legitimate the second time as the first. Making it single-use would require bolting on some separate "have I seen this token before" store anyway — at which point the Redis-backed opaque token is that store, just without the redundant signing step on top. The same reasoning is why the session-handoff mechanism elsewhere in this codebase (`SessionHandoffStore`, used by the one-shot registration flow) takes the identical shape: a random token, a Redis key, single-use consumption via `GETDEL`. It's the same pattern reused for the same reason, not a coincidence.

# See Also

- **[[Google OAuth2 Architecture]]** — where this state mechanism fits into the full connect/callback flow, alongside the token exchange and the encrypted connection it produces.
- **[[PKCE Challenge]]** — the `codeVerifier` field carried inside this payload, and the interception attack it defends against.
- **[[Google OAuth Modes]]** — what `CONNECT` and `SIGN_UP_AND_CONNECT` actually do differently once the callback branches on `mode`.
