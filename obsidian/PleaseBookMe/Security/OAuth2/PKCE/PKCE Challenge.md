# What Problem PKCE Solves

An OAuth2 authorization-code flow has one structurally awkward step: the `code` Google issues has to cross the user's browser as a full-page redirect (`GET /callback?code=...&state=...`) before the backend ever sees it. Anything that can observe that redirect — a browser extension, a shared proxy log, a `Referer` header leaking to a third-party resource loaded on the callback page, the browser's own history — has the code in plaintext. If possessing the code were sufficient to redeem it, whoever captured it could complete the exchange themselves. This is the **Authorization Code Interception Attack**, and it's exactly what [RFC 7636](https://datatracker.ietf.org/doc/html/rfc7636) (Proof Key for Code Exchange) exists to close.

PKCE's fix: generate a secret (the **verifier**) that never leaves the server, derive a **challenge** from it (a one-way hash), send only the challenge to Google up front, and require the original verifier to be presented again at redemption time. Google checks that hashing the presented verifier reproduces the challenge it cached — so redeeming a code now requires knowing a value that was never in the redirect at all.

**Why this matters here even though this app already holds a `client_secret`.** RFC 7636 was originally written for public clients — mobile apps, SPAs — that structurally cannot keep a secret. This backend is a confidential client (see "PKCE Is Defense-in-Depth Here, Not the Only Defense" below) and authenticates to Google's token endpoint with HTTP Basic `client_id`/`client_secret` regardless of PKCE. PKCE is used anyway because the `client_secret` protects a *different* step — proving to Google "this exchange call really came from the registered backend" — while PKCE protects the step *before* that: proving "the code being redeemed hasn't been stolen off the wire between Google and this backend." Modern guidance (including OAuth 2.1) treats PKCE as mandatory for every client type for exactly this reason.

---

# The Algorithm, Precisely

Implemented in `security/oauth/google/pkce/PkceGenerator.java`, with `PkceChallenge` as the two-field carrier record.

```java
public record PkceChallenge(
    String verifier,
    String challenge
) {
    public static final String METHOD = "S256";
}
```

```java
public PkceChallenge generate() {
    byte[] bytes = new byte[VERIFIER_BYTES];   // 64
    secureRandom.nextBytes(bytes);

    String verifier = encoder.encodeToString(bytes);   // Base64 URL, no padding

    return new PkceChallenge(verifier, challengeFor(verifier));
}

private String challengeFor(String verifier) {
    byte[] digest = MessageDigest.getInstance("SHA-256")
        .digest(verifier.getBytes(StandardCharsets.US_ASCII));

    return encoder.encodeToString(digest);   // Base64 URL, no padding
}
```

Step by step:

1. **64 cryptographically random bytes** from `java.security.SecureRandom` — not `java.util.Random`, which is predictable and unsuitable for anything security-sensitive.
2. **Base64-URL-encode without padding** (`Base64.getUrlEncoder().withoutPadding()`) to get the `verifier` string. 64 bytes = 512 bits → 512/6 ≈ 86 characters. RFC 7636 requires the verifier to be **43–128 characters** from the unreserved character set `[A-Za-z0-9-._~]`; 86 sits comfortably inside that range, and Base64-URL's alphabet (`A-Za-z0-9-_`) is already a subset of the RFC's allowed set — no extra escaping needed.
3. **SHA-256 the verifier's ASCII bytes**, then Base64-URL-encode the digest (again without padding) to get the `challenge`. This is a one-way function: the challenge reveals nothing about the verifier that produced it.
4. **`METHOD` is hardcoded to `"S256"`** — never `"plain"`. See "Why S256, Never Plain" below.

Both `generate()` calls in the codebase — the standalone `PkceGenerator` bean and its use inside `DefaultGoogleConnectService` — produce a fresh, independent `PkceChallenge` every time; nothing here is deterministic or reused across requests.

---

# Why S256, Never Plain

RFC 7636 defines two challenge methods: `plain` (challenge == verifier, sent as-is) and `S256` (challenge == hash of verifier). This codebase only ever uses `S256`, and the constant is not configurable — there's no code path that could accidentally send `plain`.

The reason `plain` defeats the entire point: the challenge travels in the **same** exposed channel PKCE is trying to protect — the initial outbound redirect to Google's consent screen (browser history, `Referer` headers, proxy/access logs). If the challenge *is* the verifier, anyone who captured that first redirect now holds everything needed to redeem a code captured off the *second* redirect (the callback) — PKCE would add no protection at all. Hashing the verifier before it's sent anywhere means the value observable in the first redirect (the challenge) is cryptographically useless for reconstructing the value needed at redemption time (the verifier).

---

# Two Secrets, Not One

This flow carries two independently-generated random values that look similar (both are opaque random strings) but defend against different things:

|                           | PKCE verifier                                                                 | OAuth `state` token                                                                                                 |
| ------------------------- | ----------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------- |
| Generated by              | `PkceGenerator` (64 random bytes)                                             | `RedisOAuthStateStore.issue` (32 random bytes, separately)                                                          |
| Ever sent to the browser? | **Never.** Lives only in the `OAuthState` JSON payload, server-side in Redis. | Yes — it's a query parameter on both the outbound redirect to Google and Google's callback redirect back.           |
| What it defends against   | Authorization-code interception (this page)                                   | CSRF, replay, and is the identity lookup key — see **[[Google OAuth2 Architecture]]** "Authorization Code Exchange" |
| Consumed how              | Read out of the `OAuthState` record after the state token resolves it         | `Redis GETDEL` — single-use, atomic                                                                                 |

The verifier is not a query parameter at any point in this flow — it rides *inside* the `OAuthState` record that the state token happens to key in Redis:

```java
public record OAuthState(
    OAuthFlowMode mode,
    UUID userUid,           // null when mode is SIGN_UP_AND_CONNECT
    String codeVerifier,
    List<String> requestedScopes,
    String redirectAfter
) {}
```

This is the crux of why PKCE actually defeats interception here: even a network observer who captures **both** the `code` and the `state` off the callback redirect (the only two values that ever cross the browser) still cannot redeem the code. The verifier needed to complete the exchange was never transmitted to the browser at any point in the flow — it went straight from `PkceGenerator` into Redis, and comes back out of Redis only inside the backend process handling the callback.

---

# Lifecycle, End to End

```
DefaultGoogleConnectService.authorize(...)
        │
        ├── PkceGenerator.generate()  →  PkceChallenge { verifier, challenge }
        │
        ├── OAuthStateStore.issue(
        │       new OAuthState(mode, userUid, verifier, scopeUris, redirectAfter),
        │       Duration.ofMinutes(10)
        │   )
        │       Redis:  SETNX  oauth:state:<random-32-byte-token>  →  { ...verifier embedded... }
        │       returns the 32-byte token (NOT the verifier)
        │
        └── GoogleAuthorizationUrlBuilder.build(state, pkce, scopes)
                queryParam("state", state)                    ← the 32-byte token
                queryParam("code_challenge", pkce.challenge()) ← the HASH, never the verifier
                queryParam("code_challenge_method", "S256")
        │
        ▼
{ authorizationUrl }  →  browser navigates to Google
        │
        ▼
   [ Google caches the challenge against this authorization request ]
        │
        ▼
   [ user consents ]  →  Google redirects: GET /callback?code=...&state=<32-byte-token>
        │
        ▼
GET /api/v1/integrations/google/callback
        │
        ├── OAuthStateStore.consume(state)
        │       Redis:  GETDEL  oauth:state:<token>
        │       →  Optional<OAuthState>  — recovers codeVerifier, never sent by the browser
        │
        └── GoogleTokenClient.exchangeAuthorizationCode(code, oauthState.codeVerifier())
                POST https://oauth2.googleapis.com/token
                    Authorization: Basic <client_id:client_secret>
                    grant_type=authorization_code
                    code=<from browser>
                    code_verifier=<recovered from Redis, NEVER from the browser>
                    redirect_uri=<must match byte-for-byte>
        │
        ▼
   [ Google hashes the presented code_verifier with SHA-256 and compares it
     to the code_challenge cached against this authorization request ]
        │
        ├── match      →  200 { access_token, refresh_token, id_token, ... }
        └── mismatch   →  400 invalid_grant  →  GoogleTokenExchangeException
```

The exact HTTP form body sent at exchange time (`DefaultGoogleTokenClient.exchangeAuthorizationCode`):

```java
MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
form.add("grant_type", "authorization_code");
form.add("code", code);
form.add("code_verifier", codeVerifier);
form.add("redirect_uri", registration.getRedirectUri());
```

`client_id`/`client_secret` are not in this form body at all — they're sent as an HTTP `Authorization: Basic` header, set once when `DefaultGoogleTokenClient`'s `RestClient` is built. Google's `client-authentication-method: client_secret_basic` requires this; sending credentials in *both* the header and the form body returns `invalid_client`.

---

# PKCE Is Defense-in-Depth Here, Not the Only Defense

Worth being precise about this, because it changes what a captured code/verifier pair actually gets an attacker: this backend is a **confidential client** — it holds a `client_id`/`client_secret` and authenticates every call to Google's token endpoint with them (see the HTTP Basic header above). That means even if an attacker somehow obtained *both* the authorization `code` and the PKCE `verifier`, they still could not call Google's token endpoint successfully without also possessing this application's `client_secret` — something that never leaves the server and isn't part of this flow's threat model at all.

Contrast this with a **public client** (a mobile app or a browser-only SPA), which structurally cannot hold a `client_secret` — for those, PKCE is not defense-in-depth, it is the *entire* defense against code interception, since nothing else stands between "I have the code" and "I can redeem it." This codebase's server-side confidential client already has that separate layer of protection; PKCE here closes the one gap the `client_secret` doesn't cover — the code's exposure while it's in transit through the browser — rather than being the sole thing standing between an attacker and a stolen session.

---

# Component Boundaries

Four components each own exactly one piece of this, and none of them know about the others — `DefaultGoogleConnectService` is the only place that wires them together (see **[[Google OAuth2 Architecture]]** for its full orchestration role):

| Component | Owns | Deliberately does NOT know about |
| --- | --- | --- |
| `PkceGenerator` / `PkceChallenge` | The RFC 7636 crypto: random verifier, SHA-256 challenge. | Redis, HTTP, Google, or that a verifier will ever be stored anywhere. |
| `OAuthState` / `OAuthStateStore` (`RedisOAuthStateStore`) | Parking an opaque payload (which happens to contain the verifier, among other fields) behind a single-use, TTL'd Redis key. | What a "PKCE verifier" is, or that the string it's storing is cryptographically special — to this component it's just one field on a JSON payload. |
| `GoogleAuthorizationUrlBuilder` | Shaping the outbound URL — which fields go in the query string and which don't. | Where the challenge came from or where the verifier is being kept in the meantime. |
| `GoogleTokenClient` (`DefaultGoogleTokenClient`) | The HTTP exchange itself — POSTing the form, reading the response. | Where the `codeVerifier` argument it's handed originated (Redis, memory, wherever). |

This separation is what keeps PKCE support a small, independently-testable unit (`PkceGeneratorTest` exercises `PkceGenerator` with zero Redis, zero HTTP, zero Spring context) rather than logic smeared across the OAuth state store and the token client.

---

# Verified Properties

`PkceGeneratorTest` (`security/oauth/google/authorization/`) pins down the RFC-compliance properties directly against the implementation, with no mocks:

- **`generate_verifierIsWithinRfc7636LengthAndCharset`** — the verifier is between 43 and 128 characters, and matches `[A-Za-z0-9\-._~]+`.
- **`generate_challengeIsBase64UrlSha256OfVerifier`** — independently recomputes `Base64Url(SHA-256(verifier))` in the test and asserts it equals `challenge`; also asserts the challenge contains none of `=`, `+`, `/` (the characters standard, padded Base64 would include but URL-safe, unpadded Base64 must not).
- **`generate_isNotDeterministic`** — two calls to `generate()` produce different verifiers, guarding against an accidental fixed-seed or cached-value regression.
- **`method_isS256`** — pins `PkceChallenge.METHOD` to the literal `"S256"`, so a future edit can't silently downgrade to `"plain"` without a test failing.

---

# Failure Modes

- **Verifier/challenge mismatch at exchange time** surfaces from Google as `invalid_grant` at the token endpoint, wrapped by `DefaultGoogleTokenClient.exchangeAuthorizationCode` into `GoogleTokenExchangeException`. This is indistinguishable, from the backend's point of view, from a garbled or forged `code` — both are just "Google refused this exchange." There is no PKCE-specific error path; it collapses into the same generic exchange-failure handling described in **[[Google OAuth2 Architecture]]** ("Authorization Code Exchange").
- **A Redis compromise exposes the verifier** (it sits in plaintext inside the `OAuthState` JSON payload) — but the surrounding controls limit the blast radius: the key is single-use (`GETDEL`, consumed by the real callback the moment it arrives) and TTL'd to 10 minutes, so a compromised verifier is only useful for a narrow window and only in a race against the legitimate callback. This is a different threat model than the browser-interception attack PKCE exists to stop; it's closer to a general "protect your Redis instance" concern, and is why `RedisOAuthStateStore` treats the whole payload — not just the verifier field — as sensitive.
- **Redirect URI mismatch** — Google requires `redirect_uri` to match byte-for-byte across the authorization request, the token exchange, and the Google Cloud Console registration. This isn't a PKCE failure mode specifically, but it's checked at the exact same call (`exchangeAuthorizationCode`) and produces the same exception type, so don't assume every `GoogleTokenExchangeException` at this call site means the PKCE check failed — check the actual error body/logs.

---

# See Also

- **[[Google OAuth2 Architecture]]** — the full Delegated Authorization flow this page is one piece of; see "Authorization Code Exchange" for how the PKCE verifier's recovery fits alongside state consumption and `id_token` verification in the callback.
- **[[Google OAuth2 Flow]]** — the browser-level narration of the same flow.
- **[[PKCE OAuth2 Flow]]** — an earlier draft describing Spring Security's built-in OAuth2-login PKCE support (`DefaultOAuth2AuthorizationRequestResolver`, an `HttpSession`-held verifier). **This codebase does not use that machinery** — everything on this page (`PkceGenerator`, Redis-backed `OAuthStateStore`) is a hand-rolled equivalent, built because the application is fully stateless (`SessionCreationPolicy.STATELESS`) and has no `HttpSession` to store a verifier in. The RFC-level concept in that older note is correct; the concrete Spring classes it names are not the ones actually wired up here.
