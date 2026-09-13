# ISSUE-0005: Visiting the Services page force-logs the user out

Resolved by `.agents/tasks/active/TASK-0003-fix-service-page-force-logs.md`.

## Priority

**High**

Regression that fully blocks a core dashboard page for every user, every
time. Confirmed live via browser reproduction and direct API calls (see
Evidence below) — not a hypothetical.

## Summary

`GET /dashboard/services` always redirects to `/login?session=expired`, even
for a freshly-authenticated user with a fully valid session. The page's data
loader calls `GET /api/v1/services` (the list endpoint), which this session's
own earlier change — "Drop the entire getAllServices endpoint" — removed from
`ServiceController`. The now-unmapped route responds `401 Unauthorized`
(carrying an `Allow: POST` header, the fingerprint of a method-not-allowed
condition) instead of a clean `404`. The client's `withAccessToken` helper
treats any `401`/`403` as "the session is invalid," attempts a token refresh,
retries, gets `401` again (the route itself is broken, not the token), and
throws `SessionExpiredError` — which the Services page turns into a redirect
to the login screen.

This was flagged as an at-risk consequence when the list endpoint was
removed, but it was expected to surface as a visible in-page error message
("Could not load your services"), not as a forced sign-out. It is worse than
anticipated: cookies are not actually destroyed (see Evidence), but the user
experience is indistinguishable from a real logout, and repeating the same
navigation reproduces the same redirect every time — hence "must continuously
sign in again and again."

Availability (`/dashboard/availability`) and Integrations pages are
unaffected because their list-fetching gateways (`availability-gateway.ts`)
call `GET /api/v1/schedules` / `GET /api/v1/availabilities`, both of which
are real, still-mapped endpoints.

## Location

**Server (root cause of the regression):**
- `server/src/main/java/com/pleasebookme/server/core/service/controller/ServiceController.java` — `GET /api/v1/services` (list) was removed in this session; only `POST /api/v1/services`, `GET/PUT/DELETE /api/v1/services/{id}` remain
- `server/src/main/java/com/pleasebookme/server/core/service/service/BusinessServiceService.java` / `service/impl/BusinessServiceImpl.java` — `getAllServices()` removed accordingly

**Client (symptom):**
- `client/features/services/services/service-gateway.ts` — `listMyServiceCatalogOnPlatform` still calls `platformClient().get<Service[]>(SERVICE_BASE, ...)` i.e. `GET /api/v1/services`; this was flagged as an at-risk caller when the endpoint was dropped, but never updated
- `client/app/dashboard/services/page.tsx` — awaits `withAccessToken(() => listMyServiceCatalogOnPlatform(...), { allowSessionWrite: false })`; on `SessionExpiredError` it calls `redirect(SESSION_EXPIRED_REDIRECT)`
- `client/lib/authenticated-platform-request.ts` — `isAuthFailure` treats any `401`/`403` axios response as "the session is invalid" with no way to distinguish "your token is bad" from "this route doesn't accept this method"; `withAccessToken` refreshes once, retries once, then throws `SessionExpiredError` on a repeated auth-shaped failure

## Current Behavior

Verified directly, both through the browser and via `curl` against the
running dev servers (Next.js on `:3000`, Spring Boot on `:8080`):

1. **Backend**: `GET /api/v1/services` with a valid, freshly-issued Bearer
   token returns:
   ```
   HTTP/1.1 401
   Allow: POST
   Content-Length: 0
   ```
   The same token succeeds normally against `GET /api/v1/services/999999`
   (`404 SERVICE_NOT_FOUND` via `GlobalExceptionHandler`, proving the token
   itself authenticates fine) and against `GET /api/v1/profiles`,
   `GET /api/v1/users`, `GET /api/v1/booking-policies` (all `200`). The
   `401`+`Allow: POST` combination only appears on the bare `/api/v1/services`
   path, and only differs from the same request with **no** token (also
   `401`, but without the `Allow` header) by that header — indicating the
   request does reach `DispatcherServlet`'s handler mapping, correctly
   determines only `POST` is mapped at that path, and produces what should be
   a `405 Method Not Allowed`, but the response that actually leaves the
   server is `401`. The exact point where the status gets rewritten from 405
   to 401 was not root-caused in this pass — no custom
   `@ExceptionHandler(HttpRequestMethodNotSupportedException.class)` exists
   anywhere in `src/main/java` (confirmed by search), so this looks like a
   framework/security-filter interaction rather than application code, and is
   worth its own follow-up investigation.
2. **Browser, end to end**: registered a fresh test account, signed in,
   clicked "Services" in the dashboard sidebar → immediately redirected to
   `/login?session=expired` ("Your session has ended. Please sign in again.").
3. **Cookies are not actually destroyed by this path** (confirmed with a
   `curl` cookie-jar replay: login → `GET /dashboard` → `200` → `GET
   /dashboard/services` → `307` to `/login?session=expired` → `GET /dashboard`
   again → still `200`, same cookies). `listMyServiceCatalogOnPlatform` is
   called with `allowSessionWrite: false`, so `withAccessToken` never calls
   `destroySession()` on this path — the redirect is a false "session
   expired" signal, not a real logout. This distinction is invisible to the
   user: they land back on the login screen and have to re-enter credentials
   regardless of whether the cookies were technically cleared.
4. **Result**: the Services page is completely unreachable. Every attempt to
   open it produces the same redirect, which is what reads as "must
   continuously sign in again and again" — the user re-authenticates, lands
   back on the dashboard, clicks Services, and is bounced again.

## Expected Behavior

1. `GET /api/v1/services` should not silently disappear into a `401` that the
   client's generic auth-failure handling misinterprets as a dead session.
   Either restore a scoped list endpoint the client actually needs, or update
   `service-gateway.ts` to stop calling the removed route.
2. `withAccessToken`/`isAuthFailure` should not treat every `401`/`403` as
   proof the *session* is invalid when the underlying cause can also be "this
   route doesn't support this method" or any other non-session-related
   authorization failure — at minimum, a route-level failure should not be
   indistinguishable from an expired token to the end user.
3. Visiting Services with a valid session should render the catalog (or a
   scoped, owner-filtered empty state), never force a re-login.

## Suggested Follow-up

1. Decide how `listMyServiceCatalogOnPlatform` should fetch a caller's
   services now that the generic list endpoint is gone — this was flagged as
   an open decision when the endpoint was removed and was never actioned.
   Options: (a) add a scoped `GET /api/v1/services?organizationId=` or
   `/api/v1/organizations/{id}/services` endpoint on the backend, deriving
   the organization server-side the same way `createService`/`updateService`
   already do via `CurrentPrincipalProvider`; or (b) have the client resolve
   the service list some other already-scoped way. Given `SECURITY.md`'s
   standing principle of deriving ownership server-side rather than trusting
   client-supplied ids, (a) is the more consistent choice.
2. Separately, root-cause why Spring returns `401` instead of `405` for an
   authenticated request to a path whose method isn't mapped — this behavior
   will silently misfire the same way for any future endpoint removal, not
   just this one, and is worth understanding regardless of how item 1 is
   resolved.
3. Harden `authenticated-platform-request.ts` so a route-level `401`/`403`
   that isn't actually about token validity doesn't get laundered into a
   full `SessionExpiredError` redirect — at minimum this should not fire the
   user back to login on every single page visit with no way to tell that
   their session was, in fact, still fine.
