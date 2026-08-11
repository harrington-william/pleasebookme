# Google One-Shot Registration — Frontend Build Plan

Sign up **and** grant Calendar/Sheets/Drive in a single Google round trip. The
user picks an account, sees one consent screen, and lands on the dashboard
signed in with an active integration.

**The backend is built and its tests pass.** This document is the frontend plan
only — no frontend code has been written. Server-side detail lives in
`docs/services/auth/GOOGLE_ONE_SHOT_BACKEND.md`; the architectural narrative is
`SECURITY.md` §14.

> **One prerequisite is still open.** `UserProvisioningService` writes no
> `tenant.tenants` row (`SERVER_AGENTS.md` → "Workspace provisioning"). This
> flow provisions accounts through that same component, so until it is fixed a
> successful consent still produces a tenant-less account. That is a backend
> task and does not block building any of the below — but do not read a green
> end-to-end run as proof the workspace is complete.

---

## 1. Scope: registration only

| Path | Flow | Changing? |
|---|---|---|
| **Register** page | one-shot (new) | **yes** — swap the Google button |
| **Login** page | GIS ID token (existing) | no |
| Settings → Integrations | connect flow (existing) | no |

Login deliberately stays on Google Identity Services. The one-shot sends
`prompt=consent`, and forcing a consent screen on every sign-in is hostile. A
returning user still sees an account chooser but no consent screen, because the
registration grant already covers `openid email profile` for the same client ID.

**Declining and unticking are different outcomes**, and the landing page has to
tell them apart:

| User action | Callback carries | Reality |
|---|---|---|
| Unticks Calendar, continues | `?google=connected&handoff=…` | account **created**, session issued, narrow scopes |
| Cancels or closes the window | `?google=denied` | **nothing created** — not registered at all |

---

## 2. The backend contract, as actually built

Two new endpoints, both `permitAll`, both on the existing platform origin.

```
POST /api/v1/auth/google/authorize
     body    { redirectAfter?: string }        // optional
     200     { authorizationUrl: string }

POST /api/v1/auth/google/handoff
     body    { code: string }
     200     { accessToken: string, refreshToken: string }
     401     InvalidSessionHandoffException — unknown, expired, or already used
```

The consent callback lands on **Spring**, not Next.js, and redirects back to:

```
{frontend}{redirectAfter}?google=<outcome>[&handoff=<code>]
```

| `?google=` | Meaning | What to show |
|---|---|---|
| `connected` | account + session ready, `handoff` present | exchange, then go to `/dashboard` |
| `denied` | user cancelled at Google | neutral message, back to `/register` |
| `invalid_state` | state forged, expired (10 min), or replayed | "link expired", back to `/register` |
| `missing_code` | malformed callback | same as `invalid_state` |
| `error` | exchange or provisioning failed server-side | "something went wrong", back to `/register` |

`redirectAfter` defaults server-side to **`/google/complete`**. Passing it from
the client is optional and currently unnecessary — see §4.3.

### Two properties worth internalising

**The handoff code is not a token.** Redis stores only a user reference; the JWT
pair is minted when the code is exchanged. So the value sitting in the URL bar
is worthless the instant it is used — but it is also **single-use with a
60-second TTL**, which drives two hard requirements in §5.2.

**`?google=connected` does not mean Calendar was granted.** Users can untick
scopes and the connection row is still written `ACTIVE`. Any "is this workspace
calendar-connected?" question must read the connection's `scopes` array (raw
Google URI strings, not enum names) — never `status`. That check belongs to the
dashboard, not to this flow.

---

## 3. Where the code goes

This is an **auth** feature, not an integrations one. It produces a session; the
Google connection is a side effect. So it extends `features/auth/` rather than
`features/integrations/google/`, mirroring how the backend put both endpoints on
`AuthController`.

**New files**

```
app/api/auth/google/authorize/route.ts        POST → platform, no auth
app/api/auth/google/handoff/route.ts          POST → platform, then createSession()
app/(auth)/google/complete/page.tsx           public landing
features/auth/components/google-onboarding-button.tsx
features/auth/components/google-handoff-exchange.tsx
```

**Modified files**

```
features/auth/types/auth.ts                   + 3 types
features/auth/services/auth-gateway.ts        + 2 server functions
features/auth/services/auth-api.ts            + 2 browser functions
features/auth/components/register-form.tsx    swap the Google button
```

Extending the existing `auth-gateway.ts` / `auth-api.ts` rather than adding
parallel service modules is deliberate: both endpoints live under
`/api/v1/auth/**`, which is exactly what those two files already describe
themselves as covering.

Neither BFF route uses any access-token helper — both are unauthenticated by
nature, which is the whole point of the flow.

---

## 4. Implementation notes per file

### 4.1 `features/auth/types/auth.ts`

```ts
export type GoogleAuthorizeRequest  = { redirectAfter?: string };
export type GoogleAuthorizeResponse = { authorizationUrl: string };
export type GoogleHandoffRequest    = { code: string };
// The handoff response is the existing AuthTokens — reuse it, do not redeclare.
```

### 4.2 `auth-gateway.ts` (server only)

Two thin passthroughs in the established style — return the platform response
verbatim, let axios errors propagate for the route handler to normalise:

```ts
authorizeGoogleOnboardingOnPlatform(payload) → GoogleAuthorizeResponse
exchangeGoogleHandoffOnPlatform(code)        → AuthTokens
```

### 4.3 `app/api/auth/google/authorize/route.ts`

POST, no body validation beyond "optional string". Forward to the platform and
return `{ authorizationUrl }` unchanged.

**Do not send a `redirectAfter`.** The server default is already
`/google/complete`, and hardcoding the same path on both sides invites them to
drift. The field exists as the extension point for later (e.g. preserving a
`?next=` destination through signup) — reach for it then, not now.

Normalise errors with `normalizeApiError` from `lib/api-error`, matching
`app/api/auth/login/route.ts`.

### 4.4 `app/api/auth/google/handoff/route.ts`

The only route here that touches cookies.

```
parse body → { code }               400 if missing
exchangeGoogleHandoffOnPlatform     401 propagates as-is
createSession(tokens)               httpOnly, exactly like /api/auth/login
decodeJwt(accessToken)
return { subject, actorType, tenantUid }    // no tokens in the body, ever
```

Mirror `app/api/auth/login/route.ts` almost line for line — same
`createSession`, same `decodeJwt` claim extraction, same `SessionSummary` shape.
A `tenantUid` of `null` is expected and is not an error (`SECURITY.md` §5).

Give 401 its own message: "That sign-in link has expired. Please try again."
The platform's own wording is correct but internal.

### 4.5 `app/(auth)/google/complete/page.tsx`

A Server Component that reads `searchParams` and branches. Sits in the `(auth)`
route group so it inherits the chrome-less auth shell.

```
?google=connected + handoff  → <GoogleHandoffExchange code={handoff} />
?google=denied               → neutral copy + "Back to sign up"
anything else / no handoff   → "link expired" copy + "Back to sign up"
```

Handle `connected` **without** a `handoff` as a failure, not a success. It should
not occur from the onboarding path, but a connect-flow callback misconfigured to
land here would produce exactly that, and silently rendering a spinner forever is
the worst outcome.

### 4.6 `features/auth/components/google-handoff-exchange.tsx`

Client component. Fires the exchange on mount, shows a spinner, then:

```
success → router.push("/dashboard"); router.refresh();
failure → inline AuthFormAlert + "Back to sign up"
```

`router.refresh()` matters — Server Components must re-read the new cookie. Same
pattern as `sign-out-button.tsx`.

### 4.7 `features/auth/components/google-onboarding-button.tsx`

Model it on `connect-google-button.tsx`, minus the scope checkboxes — onboarding
always requests everything, since this is the user's single consent screen.

```
POST /api/auth/google/authorize
window.location.assign(authorizationUrl)   // NEVER fetch (see §6)
// leave `pending` true — the page is navigating away
```

Gate on `clientEnv.googleOAuthEnabled` the same way `GoogleAuthButton` does.

Label it **"Continue with Google"**, not "Sign up with Google". Resolution step 1
means an existing user clicking it is simply signed in, and the copy should not
claim otherwise.

### 4.8 `register-form.tsx`

Swap `<GoogleAuthButton label="Sign up with Google" />` for
`<GoogleOnboardingButton />`. Nothing else changes; `login-form.tsx` is untouched.

---

## 5. The two things that will actually bite

### 5.1 The landing route must not be gated — verify before building anything else

The handoff redirect arrives **before any cookie exists**. That is the entire
point of the flow.

`proxy.ts` gates `PROTECTED_PREFIXES = ["/dashboard"]` on the refresh cookie, so
a landing route under `/dashboard` would bounce to `/login` and **discard the
handoff code silently, after a real consent**. `GUEST_ONLY_ROUTES` is exact-
matched on `/login` and `/register`, so it does not catch this path either.

`/google/complete` currently satisfies both constraints with **no change to
`proxy.ts`**. Confirm that by loading it in a fresh incognito window before
writing the button — this failure only reproduces after a genuine consent round
trip, which is an expensive way to discover it.

### 5.2 React StrictMode will consume the code twice

In development, React runs effects twice. The second call hits a `GETDEL`'d key
and reports failure **on a flow that actually succeeded** — a session was
created, and the user is told it was not.

Latch with a `useRef`, not a state flag: state resets across the double-invoke,
a ref does not.

```ts
const started = useRef(false);
useEffect(() => {
  if (started.current) return;
  started.current = true;
  void exchange();
}, []);
```

While you are there, scrub the code from the URL on mount:

```ts
window.history.replaceState(null, "", "/google/complete");
```

It is single-use and expires in 60 seconds, so this is hygiene rather than a
control — but it keeps a credential-shaped string out of browser history and out
of any screen-share.

---

## 6. Smaller traps

- **Never `fetch` the authorization URL.** `fetch` follows redirects
  transparently, so the browser would try to *fetch* Google's consent page
  cross-origin. It presents as a CORS error and is not one. It must be a
  top-level `window.location.assign`.
- **CORS is irrelevant to the callback.** It is a top-level navigation, not an
  XHR. If you find yourself adding CORS config to fix this flow, the diagnosis
  is wrong.
- **The Google Console redirect URI is the *backend* origin**
  (`…:8080/api/v1/integrations/google/callback`), not the Next.js one, and must
  match byte for byte. Only `app.client.frontend-url` on the server needs to
  know where the frontend lives.
- **60 seconds is short.** A slow landing page — heavy Server Component, cold
  start — can burn a meaningful fraction of it. Keep `/google/complete` light and
  fire the exchange immediately, not behind a transition or a suspense boundary
  that waits on something else.

---

## 7. Build order

1. **Verify `/google/complete` is reachable with no cookies.** A placeholder page
   and an incognito window. §5.1 — do this first, it is the cheapest possible
   moment to find that problem.
2. Types + the two `auth-gateway.ts` functions.
3. `app/api/auth/google/authorize/route.ts`. Curl it; confirm you get a real
   Google URL back with `prompt=consent` and `access_type=offline` in it.
4. `app/api/auth/google/handoff/route.ts`. Cannot be tested standalone — a valid
   code only exists mid-flow. Build it, then verify in step 6.
5. `google-onboarding-button.tsx` on the register page. Clicking it should now
   reach a real Google consent screen listing Calendar, Sheets and Drive.
6. `page.tsx` + `google-handoff-exchange.tsx`. First full round trip.
7. Walk **every** outcome deliberately:
   - full consent → dashboard, signed in
   - Calendar unticked → still signed in, connection has narrow scopes
   - cancel at Google → `/register`, no account created
   - refresh the landing page after success → spent code rejected cleanly
   - wait 60s before exchanging → same rejection, same wording
   - existing Google account → signs in rather than erroring
