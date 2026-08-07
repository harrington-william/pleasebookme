# Google Delegated Authorization — Frontend Build Plan

Connects a signed-in user's Google account so the platform can act on their
behalf against Calendar, Sheets, and Drive (file-scoped). This is **not**
Google Sign-In — do not merge this work with `google-auth-button.tsx`. Sign-In
answers "who is this user"; this feature answers "may we read/write this
user's Google Calendar/Sheets". They share only the `GoogleTokenVerifier` on
the backend and nothing on the frontend. See `SECURITY.md` §11 vs §12 on the
server for the same distinction spelled out in detail.

This document is the plan only. No frontend code has been written yet.

---

## 1. What the backend already does

Fully implemented and compiling (`server/`). Four endpoints under
`GoogleIntegrationController` (`/api/v1/integrations/google/*`):

| Method & path | Auth | Purpose |
|---|---|---|
| `POST /connect` | Bearer | Body: `{ scopes?, redirectAfter? }`. Returns `{ authorizationUrl }` — a URL to Google's consent screen. Writes nothing to Google yet. |
| `GET /callback` | none (`permitAll`) | Google redirects the browser here directly after consent. Never returns JSON — always a `302` back to the frontend with `?google=<outcome>`. |
| `GET /connections` | Bearer | Returns the caller's own connections (owner-scoped by the query itself). |
| `DELETE /connections/{uid}` | Bearer | Revokes at Google, then marks the local row `REVOKED`. |

**The flow, end to end:**

1. Browser calls `POST /connect` with a Bearer access token. The server
   generates a PKCE verifier/challenge pair and a one-time `state` token,
   stores `{ userUid, codeVerifier, requestedScopes, redirectAfter }` in Redis
   keyed by `state` (10-minute TTL, single-use), and returns Google's
   authorization URL as **JSON** — not a redirect.
2. The frontend must perform a real top-level navigation to that URL itself
   (`window.location`, or a form submission — see §4.1). A `fetch`/`XHR`
   response body containing a URL is inert until something navigates to it.
3. The user consents (or declines) entirely on Google's UI. The frontend has
   no visibility into this step.
4. Google redirects the **browser** — not an XHR — to
   `GET {API origin}/api/v1/integrations/google/callback?code=...&state=...`
   (or `?error=access_denied` on decline). This hits the **Spring Boot
   origin directly**, not Next.js. There is no Next.js route for this URL.
5. The server exchanges the code, verifies the identity, encrypts and stores
   the tokens, then issues its own `302` to
   `{app.client.frontend-url}{redirectAfter}?google={outcome}`, where
   `redirectAfter` is exactly what was sent in step 1 (sanitized — only a
   relative, non protocol-relative path is honored; anything else silently
   falls back to `/settings/integrations`).
6. `outcome` is one of: `connected`, `denied`, `invalid_state`,
   `missing_code`, `error`. This query parameter is the **only** signal the
   frontend gets about what happened — no token, code, or state value ever
   reaches frontend code at any point in this flow.

**Consequence for the frontend:** step 4–5 happens entirely outside Next.js.
There is nothing to build for the callback itself — no route, no page logic —
other than making sure a real page exists at whatever path `redirectAfter`
points to, and that that page knows how to read `?google=`.

---

## 2. Wire contracts to mirror

Following the existing convention in `features/auth/types/auth.ts` (types
mirror server DTOs 1:1, cite the source file, drift is a bug):

```ts
// Source: server/src/main/java/com/pleasebookme/server/security/oauth/google/authorization/GoogleScope.java
export const GOOGLE_SCOPES = ["CALENDAR", "SHEETS", "DRIVE_FILE"] as const;
export type GoogleScope = (typeof GOOGLE_SCOPES)[number];

// Source: server/.../service/integration/dto/GoogleConnectRequest.java
export interface GoogleConnectRequest {
  scopes?: GoogleScope[];
  redirectAfter?: string;
}

// Source: server/.../service/integration/dto/GoogleConnectResponse.java
export interface GoogleConnectResponse {
  authorizationUrl: string;
}

// Source: server/.../integration/enums/OAuthProvider.java
export type OAuthProvider = "GOOGLE";

// Source: server/.../integration/enums/OAuthConnectionStatus.java
export const OAUTH_CONNECTION_STATUSES = [
  "ACTIVE", "REVOKED", "EXPIRED", "ERROR",
] as const;
export type OAuthConnectionStatus = (typeof OAUTH_CONNECTION_STATUSES)[number];

// Source: server/.../service/integration/dto/OAuthConnectionSummaryResponse.java
export interface OAuthConnectionSummary {
  oauthConnectionId: string;   // BigInteger serializes as a JSON string on this platform
  oauthConnectionUid: string;  // UUID
  provider: OAuthProvider;
  providerEmail: string;
  scopes: string[];            // raw granted scope URIs, not GoogleScope enum names
  status: OAuthConnectionStatus;
  tokenExpiresAt: string;      // ISO instant
  connectedAt: string;
  lastRefreshedAt: string;
  lastUsedAt: string | null;
}

// The five outcomes DefaultGoogleConnectService.complete() can redirect with.
export const GOOGLE_CONNECT_OUTCOMES = [
  "connected", "denied", "invalid_state", "missing_code", "error",
] as const;
export type GoogleConnectOutcome = (typeof GOOGLE_CONNECT_OUTCOMES)[number];
```

Two things worth flagging explicitly:

- **`scopes` on the response is a list of raw granted scope URIs**
  (`https://www.googleapis.com/auth/calendar`, ...), not `GoogleScope` enum
  names — Google echoes back what it actually granted, as free-form scope
  strings. Don't try to type it as `GoogleScope[]`; it isn't. Map URI → a
  human label for display (a small lookup, the inverse of the server's
  `GoogleScope.uri()`).
- **`BigInteger` fields serialize as JSON strings**, not numbers, under this
  project's Jackson setup (same reason `LoginResponse`/entity IDs elsewhere
  in the codebase are typed `string`, not `number`, in the TS mirrors) —
  confirm this against an actual response during manual testing before
  committing to the type, but treat `oauthConnectionId` as `string` by
  default per the rest of this codebase's convention.

---

## 3. Where this lives

New feature folder, **sibling to** `features/auth/`, not inside it:

```
features/integrations/google/
  types/
    google-connection.ts        # contracts above
  services/
    google-connect-gateway.ts   # SERVER ONLY — talks to Spring Boot directly
    google-connect-api.ts       # browser-side — talks to our own BFF routes
  components/
    connect-google-button.tsx
    google-connection-outcome-banner.tsx
    google-connection-list.tsx
    disconnect-google-connection-button.tsx
    google-scope-badge.tsx
```

Reasoning: `features/auth/` owns the platform *session* (login, register,
JWT, sign-in with Google). This feature owns a *post-login* integration
concept that maps directly onto the backend's own `integration` bounded
context — a different domain even though both involve Google OAuth
machinery. Keeping them separate now avoids `features/auth/` becoming a
dumping ground as more integrations (Google Sheets export, future
non-Google providers) get added later. This markdown file stays at its
current path since that's where you asked for it, but the code it describes
should not.

Page route:

```
app/dashboard/settings/integrations/page.tsx
```

**This exact path matters, not just as a nice URL.** `proxy.ts` only session-
gates the `/dashboard` prefix (`PROTECTED_PREFIXES = ["/dashboard"]`). The
backend's `DEFAULT_REDIRECT_AFTER` is `/settings/integrations` — no
`/dashboard` prefix — which would land outside proxy's protection and outside
any real page you build. **Never rely on the backend default.** Every call to
`initiate()`/`POST /connect` from this frontend must explicitly pass
`redirectAfter: "/dashboard/settings/integrations"` in the request body. This
is worth a code comment at the call site when you build it, because the
failure mode if you forget — landing on a 404 or an unprotected route after a
real Google consent — won't show up until someone actually completes the
consent screen in a manual test, not at compile time.

---

## 4. New shared infrastructure this feature exposes (build this first)

Every existing server call in this codebase so far — `register`, `login`,
`refresh` — is `permitAll()` on the backend. **This is the first feature that
needs a Bearer-authenticated call from a Route Handler or Server Component.**
That's new territory, and it's infrastructure every future authenticated
feature (bookings, resources, customers dashboards) will also need, so it
belongs in `lib/`, not inside `features/integrations/google/`.

### 4.1 The navigation problem

`POST /connect` returns JSON, not a redirect (backend does this deliberately
— see §12 of `SECURITY.md`, a `fetch` would otherwise try to load Google's
consent screen as a cross-origin XHR response and fail CORS). Two ways to
turn that JSON URL into an actual browser navigation:

- **BFF route + client component**, matching every existing pattern in this
  codebase (`auth-api.ts` → `bffClient` → `/api/auth/*` → `auth-gateway.ts`):
  a client component calls a BFF route handler, gets `{ authorizationUrl }`
  back as JSON, then does `window.location.assign(authorizationUrl)` itself.
  **This is the recommended approach** — it's the only one consistent with
  how `login-form.tsx`/`register-form.tsx` already work, so anyone reading
  this feature after reading `features/auth/` won't hit a different paradigm.
- Alternative worth knowing about but not recommended here: a Next.js Server
  Action bound to a `<form>`, calling `redirect(authorizationUrl)` (which
  Next.js turns into a real HTTP redirect the browser's form submission
  follows natively, sidestepping the fetch/CORS problem a different way).
  Skip this unless you're deliberately introducing Server Actions as a new
  pattern project-wide — doing it only here creates two different ways of
  doing the same kind of thing in the codebase.

### 4.2 The token-refresh problem

The access token cookie lives 15 minutes (`ACCESS_TOKEN_MAX_AGE` in
`lib/auth-cookies.ts`). Nothing prevents a user from sitting on the
integrations settings page for 20 minutes before clicking "Connect" or
"Disconnect" — at which point the stored access token is already expired,
and a naive Bearer call fails with 401 even though the user has a perfectly
valid, renewable session (the refresh token cookie, 30 days). Every
`platformClient()` call this feature makes needs to handle that.

Build a helper, e.g. `lib/authenticated-platform-request.ts`, exposing
something with this shape:

```ts
// Reads the access-token cookie, runs `call` with it. On a 401/403 from the
// platform, attempts exactly one refresh (POST /api/v1/auth/refresh) and
// retries `call` once with the rotated token, persisting the new pair via
// createSession(). If the access token is missing outright, or refresh also
// fails, throws a typed error the caller maps to "redirect to /login" /
// destroySession().
export async function withAccessToken<T>(
  call: (accessToken: string) => Promise<T>
): Promise<T>;
```

This is the single most important piece of new infrastructure in this plan —
build and manually verify it (force an access-token expiry and confirm the
retry actually happens) **before** writing any Google-specific gateway
function on top of it. Every gateway function in §5 below is a thin wrapper
around one `withAccessToken(...)` call; if this helper is wrong, every
route handler built on it silently inherits the same bug, and it's the kind
of bug that only reproduces 15+ minutes into a manual test.

---

## 5. Service layer (mirrors `auth-gateway.ts` / `auth-api.ts` exactly)

**`features/integrations/google/services/google-connect-gateway.ts`**
(server-only, imports `platformClient`, `bearer`, never imported by a Client
Component — same header comment style as `auth-gateway.ts`):

```ts
const GOOGLE_BASE = "/api/v1/integrations/google";

// POST /connect
export async function initiateGoogleConnectOnPlatform(
  accessToken: string,
  request: GoogleConnectRequest
): Promise<GoogleConnectResponse>;

// GET /connections
export async function listGoogleConnectionsOnPlatform(
  accessToken: string
): Promise<OAuthConnectionSummary[]>;

// DELETE /connections/{uid}
export async function disconnectGoogleConnectionOnPlatform(
  accessToken: string,
  oauthConnectionUid: string
): Promise<void>;
```

Every function attaches `bearer(accessToken)` as a header — the one thing
`auth-gateway.ts` never needed, since all three of its calls are
`permitAll()`.

**BFF route handlers** (`app/api/integrations/google/`):

```
connect/route.ts               POST  — withAccessToken → initiateGoogleConnectOnPlatform
connections/[uid]/route.ts     DELETE — withAccessToken → disconnectGoogleConnectionOnPlatform
```

Note **no `GET /connections` route handler** is listed. The list is rendered
by a Server Component fetching directly via the gateway function (see §6) —
exactly how `app/dashboard/page.tsx` already reads `getSessionActor()`
directly rather than round-tripping through `/api/auth/me`. Add a `GET`
route later only if something client-side ends up needing to refetch the
list without a full Server Component refresh; don't build it speculatively.

**`features/integrations/google/services/google-connect-api.ts`**
(browser-side, mirrors `auth-api.ts` — calls the BFF routes above, never
`platformClient()` directly, throws a normalized error the components can
`catch`):

```ts
export async function startGoogleConnect(
  scopes?: GoogleScope[]
): Promise<GoogleConnectResponse>;
// Always sends redirectAfter: "/dashboard/settings/integrations" — see §3.

export async function disconnectGoogleConnection(
  oauthConnectionUid: string
): Promise<void>;
```

**Error normalization:** `features/auth/services/auth-errors.ts` isn't
actually auth-specific — it normalizes the platform's generic
`ApiErrorResponse` shape. Reusing `normalizeAuthError` here works today but
reads oddly from a feature that isn't auth. Worth a small refactor —
extract the shape-agnostic parts into `lib/api-error.ts` and re-export from
`auth-errors.ts` for backward compatibility — but that's a nice-to-have, not
a blocker. Don't let it hold up this feature; import from `auth-errors.ts`
directly if you'd rather ship first and refactor later.

---

## 6. Page and components

**`app/dashboard/settings/integrations/page.tsx`** — Server Component, same
shape as `app/dashboard/page.tsx`:

1. Reads the session server-side (`getSessionActor()` or equivalent guard —
   proxy.ts already gates `/dashboard`, but per the existing dashboard page's
   own comment, route-level interception alone isn't sufficient; re-check
   close to the data).
2. Calls `listGoogleConnectionsOnPlatform` through `withAccessToken(...)`
   directly — no BFF hop for the initial render.
3. Renders `<GoogleConnectionOutcomeBanner />`, `<ConnectGoogleButton />`,
   and `<GoogleConnectionList connections={...} />`.

**`connect-google-button.tsx`** (Client Component) — the entry point.
`onClick`: call `startGoogleConnect(selectedScopes)`, then
`window.location.assign(response.authorizationUrl)`. Needs a pending state
(disable the button once clicked — the whole point is the browser is about
to navigate away, but a slow network call to get the URL first is still a
real wait) and an error path for when the *initiate* call itself fails
(network error, expired session) — that failure happens before Google is
ever involved, so it should render as a normal inline error
(`AuthFormAlert`-style), not a `?google=` outcome.

If scope selection matters for v1 (e.g. Calendar now, Sheets/Drive as
separate opt-ins later), this button needs a way to know which
`GoogleScope[]` to request — either hardcode `["CALENDAR"]` for now and add
picker UI later, or build the picker immediately. Decide this before writing
the button; it changes its prop shape.

**`google-connection-outcome-banner.tsx`** (Client Component — needs
`useSearchParams`). On mount, reads `?google=`, maps each of the five
outcomes to a message and visual severity, and replaces the URL (via
`router.replace`, stripping the query param) so a page refresh doesn't
re-show a stale banner. On the `connected` outcome specifically, also call
`router.refresh()` — the Server Component list fetched in step 2 above ran
*before* the redirect landed, so it won't include the just-created
connection without an explicit refresh.

Suggested outcome → message mapping (adjust wording, keep the severity
distinction — `denied` is a neutral user choice, not a failure):

| Outcome | Severity | Message intent |
|---|---|---|
| `connected` | success | Google account connected. |
| `denied` | neutral | You declined the Google consent screen — nothing changed. |
| `invalid_state` | warning | This connection attempt expired or was already used. Try again. |
| `missing_code` | warning | Google didn't return the expected response. Try again. |
| `error` | destructive | Something went wrong connecting your Google account. |

**`google-connection-list.tsx`** — presentational, takes
`connections: OAuthConnectionSummary[]` as a prop, no fetching of its own.
Empty state ("No Google account connected yet") when the array is empty.
Per-row: `providerEmail`, a status indicator (`google-scope-badge.tsx`-style
treatment per `AuthFormAlert`'s "10% tint of the semantic color" convention
— `ACTIVE` = success tint, `REVOKED`/`EXPIRED`/`ERROR` = muted/destructive
tint), the granted scopes (map each URI back to a human label), relative
`connectedAt`/`lastUsedAt` timestamps, and a `<DisconnectGoogleConnectionButton />`.

**`disconnect-google-connection-button.tsx`** (Client Component) — same
shape as `sign-out-button.tsx`: `onClick` → `disconnectGoogleConnection(uid)`
→ `router.refresh()` on success (re-runs the Server Component, row
disappears) → pending state while in flight. Consider a confirm step
(browser `confirm()` is banned project-wide per the Chrome-automation
guidance in this environment, but a plain "click again to confirm" or a
small inline confirm affordance is fine) since disconnecting is
consequential — it revokes at Google immediately, not just locally.

---

## 7. Build sequence

Ordered so nothing is built against a foundation that doesn't exist yet, and
so the riskiest/least-tested piece (token refresh) is validated earliest,
before three separate features get built on top of it:

1. **`lib/authenticated-platform-request.ts`** (`withAccessToken` helper,
   §4.2). Manually verify the retry path — this is the one piece of this
   plan with no precedent elsewhere in the codebase to copy from.
2. **`features/integrations/google/types/google-connection.ts`** — the
   contracts in §2. Cheap, unblocks everything else, easy to get exactly
   right by re-reading the four DTO files listed if anything's ambiguous.
3. **`google-connect-gateway.ts`** — server-only functions, built on step 1.
4. **BFF route handlers** — `connect/route.ts`,
   `connections/[uid]/route.ts`, built on step 3.
5. **`google-connect-api.ts`** — browser-side wrapper around step 4.
6. **`connect-google-button.tsx`** — first component with a real payoff:
   at this point you can click a button and land on Google's real consent
   screen. Good place to pause and manually verify steps 1–6 against the
   actual Google OAuth client before building the rest.
7. **`app/dashboard/settings/integrations/page.tsx`** (skeleton — session
   guard + the button, no list yet) + confirm the callback round-trip
   actually lands back on this exact route with a `?google=` param.
8. **`google-connection-outcome-banner.tsx`** — now the round trip is
   visibly complete for the happy path.
9. **`google-connection-list.tsx`** + wire the Server Component's list fetch
   into the page.
10. **`disconnect-google-connection-button.tsx`** — closes the loop.
11. Deliberately induce each of the four non-happy outcomes (`denied` by
    clicking "Cancel" on Google's screen; `invalid_state` by reusing an old
    callback URL; `missing_code`/`error` are harder to trigger organically —
    reading `DefaultGoogleConnectService.complete()` again for exactly which
    conditions produce them is enough to confirm the banner's copy covers
    them all) and confirm the banner renders sensibly for each.

---

## 8. Environment / deployment wiring (not code, but blocks step 6)

This flow spans two origins in a way nothing else in the frontend does yet,
so double-check before testing:

- **Google Cloud Console → Authorized redirect URIs** must contain the
  **Spring Boot** origin's callback path (e.g.
  `http://localhost:8080/api/v1/integrations/google/callback` locally), not
  a Next.js URL. This is a different Console field from the "Authorized
  JavaScript origins" the separate Sign-In flow uses.
- **`server/src/main/resources/application.yaml`** → `app.client.frontend-url`
  must point at wherever the Next.js app actually runs (`http://localhost:3000`
  is already the local default — confirm it still matches your dev port).
- **CORS is irrelevant to this flow** — every authenticated call goes
  browser → Next.js BFF (same-origin) → Spring Boot (server-to-server), and
  the one cross-origin hop (browser → Google → Spring Boot) is a top-level
  navigation, not a fetch, so CORS headers never enter into it. If you find
  yourself trying to "fix CORS" to make this work, something upstream of
  that is wrong — most likely the button attempting a `fetch` against
  Google's URL instead of navigating to it.
- `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` are already wired
  server-side (`application.yaml` lines 62-67) — nothing Google-secret-shaped
  belongs in any Next.js env var for this flow. The frontend never handles a
  client secret, an authorization code, or a token, at any point.

---

## 9. Things that will bite if skipped

- Forgetting `redirectAfter` on the `/connect` call (§3) — silently lands
  outside `/dashboard`, outside proxy protection, on a route that may not
  exist.
- Treating `POST /connect`'s response as something to `fetch`-follow instead
  of navigating to (§4.1) — fails with a CORS error that has nothing to do
  with actual CORS configuration.
- Skipping the refresh-retry helper (§4.2) and calling `platformClient()`
  directly from a route handler — works in every quick manual test (tokens
  are fresh right after login) and then fails for real users ~15 minutes
  into a session, which is exactly the kind of bug that survives a
  fast demo and reaches production.
- Assuming `scopes` on `OAuthConnectionSummary` is typed like the request's
  `GoogleScope[]` (§2) — it's raw granted-scope URI strings from Google, a
  different shape than what was requested.
- Building a callback page/route in Next.js at all — there isn't one. The
  callback is entirely a Spring Boot concern; the frontend only ever sees
  its aftermath as a `?google=` query parameter on a page it already owns.
