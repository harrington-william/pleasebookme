Before getting into the two modes themselves, one clarification worth making up front, because the name of this page invites the wrong assumption: **these are not modes of "Google Sign-In."** Signing in with Google — proving who you are by handing over a Google ID token — is a separate, much simpler mechanism (`POST /api/v1/auth/google`) that never touches anything on this page at all: no PKCE, no authorization-code exchange, no `OAuthState`, no `mode` field. That mechanism only ever does one thing, so it has no need for a mode.

The two modes described here belong to the other Google mechanism entirely — Delegated Authorization, the flow where the backend asks Google for permission to act on a user's behalf against Calendar, Sheets, and Drive (see **[[Google OAuth2 Architecture]]** for how the two mechanisms relate). That flow has exactly one callback endpoint serving two genuinely different situations, and `OAuthFlowMode` is the flag that tells the callback which one it's looking at:

```java
public enum OAuthFlowMode {
    CONNECT,
    SIGN_UP_AND_CONNECT
}
```

# Why One Callback Needs to Know Which Mode It's In

Google's redirect URI has to match, byte for byte, whatever was registered in Google Cloud Console — there's no room for two different callback addresses depending on context. So both situations described below end up at the exact same `GET /api/v1/integrations/google/callback`, arriving minutes after two very different requests kicked them off. The only way that one endpoint can behave correctly in both cases is if something travels alongside the request telling it which case it's in — and since the callback has no session, no cookie, nothing else to go on, that something is the `mode` field sitting inside the `OAuthState` payload recovered from Redis (see **[[Google State]]** for how that payload gets there and back).

# `CONNECT` — An Existing User Adds a Google Connection

This is the ordinary case: someone already has a PleaseBookMe account, is already logged in, and goes to their dashboard settings to connect (or reconnect, or grant an additional scope to) their Google account.

```java
@Override
public GoogleConnectResponse initiate(
	UserPrincipal principal,
	GoogleConnectRequest request
	) {
    return new GoogleConnectResponse(authorize(
        OAuthFlowMode.CONNECT,
        principal.subject(),
        request == null ? null : request.scopes(),
        safeRedirectAfter(request == null ? null : request.redirectAfter(), DEFAULT_REDIRECT_AFTER)
    ));
}
```

The entry point is `POST /api/v1/integrations/google/connect`, and it requires a Bearer token — `CurrentPrincipalProvider.requireUser()` is what supplies `principal` here, which is precisely why this mode is allowed to already know `userUid`. There's a real, logged-in person making this request, so the `OAuthState` written to Redis carries their `userUid` from the very first moment, and the caller is even allowed to say which specific scopes they want and which page to return to afterward (`GoogleConnectRequest.scopes()` / `redirectAfter()` — both optional; omitting either falls back to requesting every scope and returning to the dashboard's integrations settings page).

When the callback sees `mode == CONNECT`, its job is simply look up the user this state already named, hand the freshly exchanged tokens to `GoogleConnectionWriter` (the exact same writer both modes share), and redirect back to the settings page with `?google=connected`. No new session is issued, because the person already had one — they never stopped being logged in while they were off on Google's consent screen.

# `SIGN_UP_AND_CONNECT` — Registering and Connecting in One Motion

This is the more interesting case, and the reason `OAuthFlowMode` exists as more than a single value: a brand-new visitor, who has never had a PleaseBookMe account at all, registers *and* grants Calendar/Sheets/Drive access in a single Google consent screen — one click, one form, done — rather than the ordinary two-step "register, then separately go connect Google later."

```java
@Override
public GoogleConnectResponse initiateOnboarding(String redirectAfter) {
    return new GoogleConnectResponse(authorize(
        OAuthFlowMode.SIGN_UP_AND_CONNECT,
        null,   // nobody is logged in yet
        null,
        safeRedirectAfter(redirectAfter, DEFAULT_ONBOARDING_REDIRECT_AFTER)
    ));
}
```

The entry point is `POST /api/v1/auth/google/authorize`, and — notice the difference — it's `permitAll`, because by definition nobody could possibly be logged in yet. That single fact ripples through everything else about this mode:

- **`userUid` is `null`** at the moment the authorization request goes out, because there is no user. The callback will have to *create* one before it can do anything with this field.
- **Scopes are never a choice here** — the request always asks for every `GoogleScope` value (`CALENDAR`, `SHEETS`, `DRIVE_FILE`), since there's no settings UI mid-signup for a brand-new visitor to pick from. The point of this mode is maximal capability granted in the one consent screen the person's already looking at.
- **The default landing page is public, not the dashboard** — `/google/complete` rather than a dashboard route. This isn't a stylistic choice; a dashboard route requires an authenticated session, and at the exact moment the callback runs, no session exists yet. Sending the browser to a route that expects a cookie which hasn't been issued yet would just bounce the person to a login screen, discarding everything that just happened.

When the callback sees `mode == SIGN_UP_AND_CONNECT`, it takes a materially different path than `CONNECT` does:

```java
if (oauthState.mode() == OAuthFlowMode.SIGN_UP_AND_CONNECT) {
    String handoff = googleOnboardingService.finalizeOnboarding(tokens, identity);
    return redirect(oauthState.redirectAfter(), "connected", handoff);
}
```

`finalizeOnboarding` does three things inside one transaction: it resolves — or, for a genuinely new person, provisions — a user from the [[Google Identity Model]], it persists the Google connection through the very same `GoogleConnectionWriter` the `CONNECT` mode uses, and it mints a short-lived, single-use handoff code. That code — not a session, not a token — is what rides in the redirect back to the frontend: `?google=connected&handoff=<code>`. The frontend then makes one more call, `POST /api/v1/auth/google/handoff`, trading that code for the actual JWT pair. The extra round trip exists purely because the callback lands on the backend and can't set a cookie on the frontend's origin — the handoff code is the one thing allowed to cross that boundary, and it's worthless to anyone who intercepts it, since it unlocks nothing by itself except a lookup that's already been spent by the time a legitimate handoff completes.

# The One Subtlety in How the Callback Branches

It would be natural to write the branch the other way around — `if (mode == CONNECT) { ... } else { /* must be sign-up */ }` — and it would even look right, since there are only two values. It's written the opposite way on purpose: the callback checks specifically for `mode == SIGN_UP_AND_CONNECT`, and falls through to the ordinary `CONNECT` handling otherwise.

The reason is a detail about the field's own history: `mode` was added to `OAuthState` after this flow already existed with only one behavior. Any state that had already been issued — sitting in Redis, mid-flight, from a consent screen a real person was looking at right as the deploy happened — deserializes with `mode = null`, simply because that field didn't exist yet when it was written. Checking explicitly for the new value and treating everything else (including `null`) as the original behavior means every one of those in-flight consents still completes correctly across the deploy boundary. Checking for the old value instead, and treating everything else as the new behavior, would have done the opposite — it would have silently misrouted every consent that was already in progress. Neither branch is more "correct" in isolation; the difference only shows up at the exact moment new code meets old data, which is exactly the moment worth designing for.

# See Also

- **[[Google OAuth2 Architecture]]** — how these two modes fit inside the full Delegated Authorization flow, alongside the token exchange and the connection it produces.
- **[[Google State]]** — the `OAuthState` payload that actually carries `mode` from the initiating request to the callback.
- **[[Google Identity Model]]** — how `SIGN_UP_AND_CONNECT` resolves a Google identity into a platform user during `finalizeOnboarding`.
- **[[PKCE Challenge]]** — the verifier both modes generate and carry through the same `OAuthState` payload.
