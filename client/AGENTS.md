<!-- BEGIN:nextjs-agent-rules -->
# This is NOT the Next.js you know

This version has breaking changes — APIs, conventions, and file structure may all differ from your training data. Read the relevant guide in `node_modules/next/dist/docs/` before writing any code. Heed deprecation notices.
<!-- END:nextjs-agent-rules -->

---

# PleaseBookMe — Next.js Client

Frontend for the centralized reservation platform. This document records the
architecture and conventions established while building the auth feature (the
first real feature in this app) — every subsequent feature should follow them.

## Governing principle

**The Spring Boot server is the authoritative entity. The frontend is built
around it, never the other way round.**

Concretely:

- Wire types mirror the server's DTOs exactly. When the server changes, this
  client follows — do not invent fields, rename them for convenience, or add
  client-side concepts the platform does not model.
- If the design shows a capability the backend does not implement, **do not
  fake it**. Render it as visibly reserved/disabled and document why. See
  "Reserved features" below.
- Client-side validation is a UX guardrail, never a security control. The
  platform re-validates and re-authorizes everything.

## Version gotchas (Next.js 16)

- **`middleware.ts` is now `proxy.ts`.** Root-level file, must export a function
  named `proxy`. Defaults to the Node.js runtime; setting `runtime` throws.
- **`cookies()` is async.** `const store = await cookies()`. `.set()`/`.delete()`
  only work inside a Route Handler or Server Function — never during Server
  Component render.
- **zod is v4**, not v3. Validators are top-level: `z.email()`, `z.url()`, not
  `z.string().email()`. Custom messages use `{ error: "..." }`, not
  `{ message: "..." }`.
- **Tailwind is v4** — config lives in `app/globals.css` via `@theme`, there is
  no `tailwind.config.js`.
- **shadcn style is `base-nova`**, built on `@base-ui/react`, not Radix. Base UI
  form controls are not native inputs: `Checkbox` uses
  `checked`/`onCheckedChange`/`inputRef` and therefore needs a react-hook-form
  `<Controller>`, not `register()`.

## Classname composition

Every dynamic className goes through `cn()` in `lib/utils.ts` — never a
template literal or manual ternary. `cn()` is `classnames` (conditional/array/
object composition) piped through `tailwind-merge` (resolves conflicting
Tailwind utilities, e.g. `p-2` + `p-4` → `p-4`). `clsx` was removed; `classnames`
is the standard.

A static, non-conditional className stays a plain string prop — `cn()` adds
nothing to a string with no branches.

## Architecture: the BFF hop

The browser **never** calls Spring Boot directly.

```
browser ──bffClient──▶ Next.js Route Handler ──platformClient──▶ Spring Boot
         (same-origin)      app/api/auth/*                    API_BASE_URL
```

Why:

1. **httpOnly cookies.** Platform JWTs are written as httpOnly cookies by the
   route handlers, so browser JavaScript can never read them. This is the main
   reason for the indirection.
2. **No CORS to negotiate.** The server now *does* configure CORS
   (`security/config/WebConfig.java`, allowing `localhost:3000/3001/3002`), but
   the browser still only ever talks to its own origin, so preflights never
   enter the picture for this app.
3. **One error shape.** Route handlers normalize the platform's inconsistent
   error responses before they reach components.

Rules:

- `lib/axios.ts` exports **two** instances. `bffClient` (browser → `/api`) and
  `platformClient()` (server → Spring). `platformClient` is a lazy function, not
  a const, because it reads server-only env and would otherwise crash any client
  bundle that transitively imports the module.
- Never import `lib/env.ts`'s `serverEnv()`, `lib/session.ts`, or any
  `*-gateway.ts` into a Client Component.
- Route handlers must not return tokens in their response body. Return identity
  only.

## Session & auth

- Tokens live in httpOnly cookies `pbm_access_token` / `pbm_refresh_token`
  (`lib/auth-cookies.ts`). Lifetimes mirror the server's `PT15M` / `P30D`.
- Tokens are stored **as issued** — not re-encrypted or re-signed. Spring signed
  them and is the only party that can verify them; a second Next-managed
  signature would add a key to manage and no security.
- `lib/auth-cookies.ts` is deliberately dependency-free (no `next/headers`, no
  env) because both `lib/session.ts` and `proxy.ts` import it, and the Next docs
  warn against proxy pulling in shared modules.
- **`lib/jwt.ts` does not verify signatures.** It decodes only. Use it for
  optimistic render decisions, never authorization.

### Three layers of protection, deliberately

1. `proxy.ts` — optimistic. Checks only that the *refresh* cookie exists (gating
   on the 15-minute access token would bounce users every 15 minutes). A forged
   cookie passes; that is expected and fine.
2. Page/Route Handler — re-reads the session server-side. Catches forgeries the
   proxy waves through.
3. The platform — verifies the JWT on every call via `JwtAuthenticationFilter`.
   This is the only real enforcement.

Never treat layer 1 as security. Per the Next.js data-security guide, checks
belong close to the data.

### ⚠ The redirect-loop invariant

**Every redirect to `/login` must also clear the session cookies, or it bounces
straight back.** `proxy.ts` sends a signed-in visitor away from `/login`, so a
guard that says "no session" and redirects there while the refresh cookie
survives hands the proxy a cookie that says "signed in" — and the two redirect
at each other until the browser gives up with `ERR_TOO_MANY_REDIRECTS`.

A Server Component **cannot** clear a cookie — `cookies().delete()` is illegal
during render — so it redirects to `SESSION_EXPIRED_REDIRECT`
(`/login?session=expired`, `lib/auth-cookies.ts`) and `proxy.ts` does the
clearing on its behalf. Never write a bare `redirect("/login")` in a page or
layout. The marker is a UX signal, not a credential: anyone can type it, and all
it achieves is signing the typist out of their own browser.

This shipped broken once and reproduced on **every** visit made more than 15
minutes after signing in — long enough after a working first login to look
intermittent, which is why "it works, then it doesn't" was the reported symptom.

### The access token is not the session signal

The refresh cookie is, at *both* layers. The access cookie lives 15 minutes and
the refresh cookie 30 days, so for almost the whole life of a session there is
no access cookie at all — reading its absence as "signed out" is simply wrong.

- `getSessionActor()` reads the access token, then falls back to the refresh
  token. Both are JWTs carrying the same identity claims (`sub`/`actor_type`/
  `tenant` — see `DefaultJwtGenerator` on the platform), differing only in
  `token_type` and lifetime, so identity survives either way.
- `withAccessToken` already had this right: missing refresh token is fatal,
  missing access token is routine.

### `SessionExpiredError` means the platform said no

It must **not** be thrown for a network error, a timeout or a 5xx. Callers treat
it as grounds to sign the user out, so widening it to every failure means a
backend restart silently ends everyone's session. `withAccessToken` rethrows
non-auth failures unchanged; pages surface those as a load error and keep the
session.

## Design system

`design/client/DESIGN.md` is the branding authority. The `design/client/stitch/`
mocks supply **structure and layout only** — their palettes are stale.

> **DESIGN.md contradicts itself.** Its YAML frontmatter says
> `background: #101419` / `primary: #adc6ff`; its prose "Colors" section says
> `#0B0D10` / `#3B82F6`. **The prose wins** — it is the brand narrative, and the
> `create_account_dark` mock is generated against it. Frontmatter is used only
> for tokens the prose does not specify (emerald/amber/red, type scale, spacing).

- **Dark only.** Obsidian Dark is currently the only theme. `<html>` hard-codes
  `class="dark"`. Do not add light-theme overrides without a design decision.
- Tokens live in `app/globals.css`. Use the semantic utilities
  (`bg-surface`, `text-muted-foreground`, `border-border`), not raw hex.
- Type scale: `text-display`, `text-headline-lg`, `text-headline-lg-mobile`,
  `text-headline-md`, `text-body-lg`, `text-body-md`, `text-label-md`,
  `text-mono-label`.
- Spacing follows the strict 4px grid: `base` 4, `xs` 8, `sm` 12, `md` 16,
  `lg` 24, `xl` 32, `2xl` 48. Use `p-lg`, `gap-md` etc.
- Radius: `--radius` is 10px and `rounded-lg` resolves to it. That is the
  universal control radius.
- Depth comes from tonal layering and borders, **not** heavy shadows. Panels get
  `border-border`; only Level-2 surfaces may use the documented ambient shadow
  `0 20px 40px rgba(0,0,0,0.4)`.
- The canvas colour is on `html`, **not** `body`. `GridBackground` is a
  `fixed -z-10` layer inside `body`; a background on `body` would cover it.

### `GridBackground`

`components/background/grid-background.tsx`, mounted once in the root layout so
it sits behind every route. Do not mount it per-page.

Built with CSS gradients rather than `<svg><pattern id>` on purpose: an SVG
pattern needs a document-unique id, so a globally reused component would emit
duplicate ids and every `url(#id)` would bind to the first match. It also masks
the grid to a radial fade — a flat edge-to-edge grid reads as wallpaper.

## Folder conventions

```
app/
  (public)/layout.tsx                Public shell — owns MarketingHeader. See
                                     "The public route group" below
  (public)/page.tsx                  Landing page: section composition only
  (public)/(auth)/{login,register}/page.tsx
                                     Route files: metadata only, no logic
  (public)/(auth)/google/complete/page.tsx
                                     PUBLIC landing for one-shot registration.
                                     Must stay outside /dashboard — see Flow 3
  api/auth/*/route.ts                BFF boundary — the only caller of gateways
  dashboard/page.tsx                 PLACEHOLDER, replace with the real one
components/
  ui/                                shadcn-managed. Do not hand-edit; the CLI
                                     overwrites this directory.
  background/                        Our own shared visual primitives
  marketing/                         Public header/footer + landing sections
features/<domain>/
  components/                        UI, "use client" where interactive
  hooks/                             client-only hooks
  schemas/                           zod schemas + form→wire mappers
  services/                          <domain>-api.ts     browser → BFF
                                     <domain>-gateway.ts server → Spring
                                     <domain>-errors.ts  error normalization
  types/                             wire contract mirroring server DTOs
lib/                                 cross-cutting: axios, env, session, jwt,
                                     api-error, authenticated-platform-request
```

Nested feature domains are allowed where the backend has one:
`features/integrations/google/` mirrors the server's `integration` bounded
context and is deliberately NOT inside `features/auth/`, which owns the platform
session only.

- Feature types live in `features/<domain>/types/`, not a root `types/`.
- Pages stay thin so behaviour is reusable and testable independently of routing.
- Put your own components in a new `components/<purpose>/` folder rather than
  `components/ui/`, which the shadcn CLI owns.

### The public route group

`app/(public)/` exists so one header can cover every public route while
`/dashboard` keeps its own chrome. A child layout cannot *remove* a parent's
header, so putting `MarketingHeader` in the root layout would leak it into the
dashboard — grouping the public routes is the only way to scope it.

- **Route groups do not change URLs.** `/`, `/login`, `/register` and
  `/google/complete` are all unaffected, so `proxy.ts`'s `PROTECTED_PREFIXES`
  and `GUEST_ONLY_ROUTES` need no update. Verify that if the group ever moves.
- `(auth)` is nested inside `(public)` and still owns the centered-card `<main>`.
- `PublicLayout` returns a fragment, not a wrapper `<div>`, so its children stay
  direct flex items of the `body` column — a wrapper would break `flex-grow` on
  the pages' own `<main>`.
- Only the header is shared. The footer stays on the landing page; auth screens
  are deliberately chrome-light.
- ⚠ `MarketingHeader` now renders on auth routes too, so its name is narrower
  than its job. Rename to a neutral `site-header` if that starts to mislead.

## Platform contract & known gaps

Auth endpoints are under `/api/v1/auth` and are the only `permitAll()` paths.

| Endpoint    | Notes                                                        |
| ----------- | ------------------------------------------------------------ |
| `/register` | 201, returns tokens directly — registration signs you in      |
| `/login`    | 200, authenticates by **username**, not email                 |
| `/refresh`  | 200, **rotates** the pair — persist both tokens               |
| `/google`   | 200, ID-token sign-in (flow 1)                                |
| `/google/authorize` | 200 `{ authorizationUrl }` — starts one-shot registration (flow 3) |
| `/google/handoff`   | 200, trades the single-use callback code for a token pair; 401 when spent/expired |

Gaps the client currently works around (**remove the workaround when fixed**):

1. **Two error shapes.** `GlobalExceptionHandler` registers no handler for
   `MethodArgumentNotValidException` or `BadCredentialsException`, so validation
   and wrong-password failures fall through to Spring's default envelope
   (possibly an empty body — no `AuthenticationEntryPoint` is configured).
   `auth-errors.ts` synthesizes messages from status codes to compensate.
2. **Almost no server validation.** The register/login DTOs carry `@NotBlank`
   and nothing else — no `@Size`, no `@Email`, no password policy. Every rule in
   `auth-schema.ts` is currently the *only* enforcement and is trivially
   bypassed by posting straight to the platform.
3. **No `/me` endpoint.** `app/api/auth/me/route.ts` is reserved and returns
   identity decoded from the JWT — no username, name, email or roles, because
   the token does not carry them.
4. **No logout/revocation endpoint.** Sign-out is local only; the refresh token
   stays valid server-side for its full 30 days.

Google OAuth2 **is** now fully implemented on both sides — see the dedicated
section below.

## Reserved features

Where the backend is not ready, the UI is built but visibly inert, so the
finished flow is reviewable now and switches on without a redesign later. Each
carries a comment naming the exact backend blocker.

Currently reserved: `/api/auth/refresh` (works, but nothing calls it
automatically — `withAccessToken` calls it on demand instead), `/api/auth/me`,
and the `/forgot-password` link.

Do not "finish" these by inventing client-side behaviour. Implement the backend
first.

## Authenticated platform calls

`lib/authenticated-platform-request.ts` → `withAccessToken(call, options)`.

Every Bearer-authenticated call to the platform must go through it. It reads the
access-token cookie, runs `call`, and on a 401/403 refreshes **once** and
retries. Without it, any feature works perfectly in a quick manual test (tokens
are fresh right after login) and then starts failing ~15 minutes into a real
session — the access token lives `PT15M` while the session is renewable for 30
days.

- Throws `SessionExpiredError` when there is no session, the refresh fails, or
  the retry still 401s. Route Handlers map that to a 401; Server Components
  `redirect("/login")`.
- **`allowSessionWrite: false` is required from a Server Component.**
  `cookies().set()` is illegal during render, so a rotated pair cannot be
  persisted there. The refreshed token is still used for that request; the next
  mutation through a Route Handler persists a fresh pair properly.

## Google OAuth2 — three distinct flows

The single most important thing to understand: **there are three Google flows,
they do different things, and only two of them share any frontend code.**
Conflating them is the easiest mistake to make in this area.

| | **1. Sign-In** | **2. Delegated authorization** | **3. One-shot registration** |
|---|---|---|---|
| Question | who is this user? | may we act on their behalf? | both, in one trip |
| Where | `/login` | Settings → Integrations | `/register` |
| Endpoint | `POST /api/v1/auth/google` | `/api/v1/integrations/google/*` | `POST /api/v1/auth/google/{authorize,handoff}` |
| Auth | `permitAll` — the ID token *is* the credential | Bearer | `permitAll` — no account exists yet |
| Mechanism | ID token from Google Identity Services, posted | authorization code + browser redirect | authorization code + browser redirect |
| Google Console field | Authorized **JavaScript origin** | Authorized **redirect URI** | Authorized **redirect URI** |
| Client secret used | no | yes (server-side only) | yes (server-side only) |
| Produces | a platform session | encrypted row in `integration.oauth_connections` | **both**, plus a provisioned account |
| Frontend code | `features/auth/` | `features/integrations/google/` | `features/auth/` |

Flows 1 and 3 both live in `features/auth/` because both produce a platform
*session*. Flow 2 lives in `features/integrations/google/`, mirroring the
backend's own `integration` bounded context — it is a *post-login* concern that
never touches the session.

Flow 3 is not a replacement for either. It shares flow 2's authorization-code
machinery on the server (same `initiate`, same callback, distinguished by an
`OAuthFlowMode`) and flow 1's account resolution — but on the frontend it is its
own path end to end.

**Why login is not also one-shot:** flow 3 sends `prompt=consent`, and forcing
the Google consent screen on every sign-in is hostile. A returning user on flow 1
sees an account chooser and no consent screen, because the registration grant
already covers `openid email profile` for the same client ID.

### Flow 1 — Google Sign-In

```
browser (GIS) ──idToken──▶ POST /api/auth/google ──▶ POST /api/v1/auth/google
                              (BFF route)                    (Spring)
                                   │                              │
                                   ◀── { accessToken, refreshToken } ──
                                   │
                          httpOnly cookies, no tokens in the response body
```

- `features/auth/hooks/use-google-identity.ts` injects
  `https://accounts.google.com/gsi/client` once per document and calls
  `initialize` + `renderButton`.
- **An ID token can only come from Google's own rendered button** — there is no
  API that mints one from an arbitrary click. So `google-auth-button.tsx` keeps
  our styled control as the *visible* layer and stacks Google's real button on
  top at `opacity-0`, sized to match via `ResizeObserver`. Google's button takes
  a pixel width and will not stretch, which is why the width is measured rather
  than guessed. The visible layer is `aria-hidden` so the control is not
  announced twice.
- `auto_select: false` — never sign a returning user in on page load. A silent
  session change is surprising, and on a shared machine it is wrong.
- The ID token is never stored. The platform verifies signature, issuer and
  audience, resolves/links/provisions the user, then discards it.
- `NEXT_PUBLIC_GOOGLE_CLIENT_ID` must equal the server's `GOOGLE_CLIENT_ID`
  byte for byte — the platform validates the token's `aud` claim against it, so
  a mismatch fails every sign-in with `invalid_audience`.
- Errors here use `normalizeGoogleSignInError`, **not** `normalizeAuthError`:
  the platform registers typed handlers for this path (`InvalidGoogleIdToken`
  → 401, `GoogleAccountEmailNotVerified` → 409), so its message body is
  meaningful and should be shown verbatim rather than overwritten with
  "incorrect username or password".

### Flow 2 — Delegated authorization (Calendar / Sheets / Drive)

```
1. click  ──▶ POST /api/integrations/google/connect  ──▶ POST /connect (Bearer)
                                                          │
                          ◀── { authorizationUrl } ───────┘   PKCE + state
                                                              stored in Redis
                                                              (10 min, single use)
2. window.location.assign(authorizationUrl)   ← a REAL navigation, never fetch
3. user consents on Google's own UI
4. Google redirects the BROWSER to Spring directly:
      GET {API origin}/api/v1/integrations/google/callback?code=…&state=…
   (no Next.js route is involved — there is nothing to build here)
5. Spring exchanges the code, verifies identity, encrypts + stores tokens,
   then 302s back to {frontend}{redirectAfter}?google=<outcome>
6. GoogleConnectionOutcomeBanner reads ?google=, strips it, and refreshes
```

Non-obvious rules, each of which will bite if ignored:

- **`POST /connect` returns JSON, not a redirect, deliberately.** The caller is
  an XHR; `fetch` follows redirects transparently, so a 302 would make the
  browser try to *fetch* Google's consent page cross-origin and fail. It must
  be a deliberate `window.location.assign`. If you find yourself debugging CORS
  here, the real bug is that something is fetching the URL instead of
  navigating to it.
- **Always pass `redirectAfter: "/dashboard/settings/integrations"` explicitly.**
  The server's `DEFAULT_REDIRECT_AFTER` now agrees (it was corrected backend-side
  from `/settings/integrations`, which lacked the `/dashboard` prefix and would
  have landed outside `proxy.ts`'s protected prefix on a route that does not
  exist). The explicit pass stays: this app's routing is not the server's to
  know, and the two agreeing today is not a reason to depend on it. Enforced in
  `app/api/integrations/google/connect/route.ts`, not in the browser, so a caller
  cannot forget it. Flow 3 deliberately does the opposite — see there for why.
- **No Next.js callback route exists, by design.** Step 4 hits Spring directly.
  The frontend only ever sees the aftermath as a `?google=` parameter. No
  authorization code, state value, or Google token ever reaches frontend code
  at any point in this flow.
- **`scopes` on `OAuthConnectionSummary` are raw granted scope URIs from
  Google**, not `GoogleScope` enum names, and include the three base scopes
  (`openid`/`email`/`profile`) the server always requests. Map them for display
  with `labelForScopeUri`.
- The consent screen lets users untick individual scopes, so granted ≠
  requested. The UI says so, and the list renders what was actually granted.
- Disconnect revokes at Google *first*, then marks the row `REVOKED`. It is not
  a local-only action, which is why the button has an inline confirm step
  (`window.confirm` is banned project-wide).
- The five outcomes are `connected`, `denied`, `invalid_state`, `missing_code`,
  `error`. `denied` is a neutral user choice, not a failure — do not style it
  as an error.

### Flow 3 — One-shot registration (`/register`)

Creates the account **and** grants Calendar/Sheets/Drive in a single Google
round trip. It exists because the product is unusable without calendar access
(`SERVER_AGENTS.md` → "Product invariants"), so a separate consent chore parked
in settings is a chore that never gets done.

```
1. click  ──▶ POST /api/auth/google/authorize ──▶ POST /api/v1/auth/google/authorize
                                                    │  no Bearer — nobody exists yet
                          ◀── { authorizationUrl } ─┘  PKCE + state (mode=SIGN_UP_AND_CONNECT)
2. window.location.assign(authorizationUrl)   ← a REAL navigation, never fetch
3. user consents on Google's own UI
4. Google redirects the BROWSER to Spring directly (no Next.js route involved)
5. Spring: exchange code → verify id_token → resolve-or-provision the user
           → store the encrypted connection → mint JWTs → stash them behind a
           single-use handoff code, then 302 to:
              {frontend}/google/complete?google=connected&handoff=<code>
6. /google/complete → GoogleHandoffExchange
      POST /api/auth/google/handoff ──▶ POST /api/v1/auth/google/handoff
      BFF writes httpOnly cookies ──▶ router.replace("/dashboard")
```

**Why the handoff code exists.** The callback lands on Spring (`:8080`), which
cannot write cookies for the Next origin (`:3000`). The alternatives are worse:
tokens in the query string leak into browser history, `Referer`, and every proxy
log; a Spring-side cookie abandons the httpOnly BFF model entirely. So Spring
hands over an opaque code instead.

**The code is not a token.** Redis stores only a user reference; the JWT pair is
minted at exchange time. It is single-use (`GETDEL`) with a **60-second TTL**.
Two consequences that are easy to get wrong:

- **React StrictMode consumes it twice.** In development, effects run twice —
  the second call hits a spent key and reports failure *on a flow that actually
  succeeded*, telling the user their sign-up broke while their cookie sits right
  there. `google-handoff-exchange.tsx` latches with a `useRef`, **not** state:
  state resets across the double-invoke, a ref does not.
- **Keep `/google/complete` light.** 60 seconds is not much, and a heavy Server
  Component or cold start burns a real fraction of it. Fire the exchange
  immediately, never behind a transition or a Suspense boundary waiting on
  something else.

Non-obvious rules:

- **⚠ `/google/complete` must never be session-gated.** The redirect arrives
  *before any cookie exists* — that is the entire point. `proxy.ts` gates
  `PROTECTED_PREFIXES = ["/dashboard"]`, so a landing route under `/dashboard`
  would bounce to `/login` and **silently discard the handoff code after a real
  consent**. `GUEST_ONLY_ROUTES` is exact-matched on `/login` and `/register`, so
  it does not catch this path either — which also means a user with a stale
  session cookie still reaches the exchange instead of being punted to
  `/dashboard`. Both verified. Re-check both lists if this route ever moves.
- **The server owns the landing path.** `DEFAULT_ONBOARDING_REDIRECT_AFTER` is
  `/google/complete`, and the client deliberately sends **no** `redirectAfter` —
  hardcoding the same path on both sides only invites drift. Renaming the route
  therefore means changing that server constant. This is the opposite of flow 2,
  which always sends one explicitly.
- **`?google=connected` does not mean Calendar was granted.** Users can untick
  scopes and the connection is still written `ACTIVE`. Any "is this workspace
  calendar-connected?" check must read the connection's `scopes` array, never
  `status`. That belongs to the dashboard, not to this flow.
- **`denied` and unticking are completely different outcomes.** Unticking →
  `?google=connected&handoff=…`, account created, narrow scopes. Cancelling →
  `?google=denied`, **nothing created at all** — the user is not registered. The
  landing page's copy distinguishes them.
- **`connected` with no `handoff` is treated as a failure**, deliberately. It
  should not occur from this path, but a flow-2 callback misconfigured to land
  here produces exactly that, and a spinner that never resolves is the worst
  possible outcome.
- **A failed exchange is genuinely awkward and the copy says so.** The account
  and the Google connection already exist by then; only the cookie is missing.
  The recovery is to sign in with Google — *not* to register again.
- **Known edge, backend-side:** an onboarding state that expires (10 min) or is
  replayed resolves to nothing, so the server falls back to
  `DEFAULT_REDIRECT_AFTER` (`/dashboard/settings/integrations`) rather than
  `/google/complete`. With no session that lands on `/login?next=…` — recoverable
  and safe, but not the wording this flow would choose. Fixing it means giving
  the server a mode-aware fallback, which it cannot know once the state is gone.

## The services editor

One route per mode — `/dashboard/services/new` and `/dashboard/services/[id]` —
with the section carried as `?tabName=`, mirroring cal.com's event-type editor.
Five tabs in two groups: Setup (Basics, Availability) and Policies (Price &
Duration, Limits & Buffers, Confirmation). `ServiceEditor` owns the whole form;
each tab is a panel reading `useFormContext`.

- **⚠ The tab is a query param, never a route segment.** Every tab edits one
  aggregate saved by one `POST`/`PUT`, so a path segment would imply the sections
  are separately addressable resources. The practical half matters more: a route
  segment swaps the page *module* on navigation, React unmounts the old one, and
  whatever the user typed is gone — which is exactly what happened when this was
  built as `[id]/policies`. A query param changes a value, not a module, so
  nothing unmounts.
- **⚠ Server pages must not read `searchParams`.** The active tab is read
  client-side via `useSearchParams()` in `ServiceEditor`. If the page or its
  `generateMetadata` depended on the tab, every switch would round-trip to the
  server and re-render the page mid-edit. That is also why there is no per-tab
  `<title>` — it is available, but not worth a round trip per click.
- **All five panels stay mounted; only the active one is displayed** (`TabPanel`
  toggles `flex`/`hidden`). Rendering just the active panel would put the
  unmount problem back, one level down, and hiding preserves per-panel scroll
  position for free.
- **Both modes submit the whole form from any tab.** `PUT /api/v1/services/{id}`
  is full-replace, and a create posts the service and its booking policy in one
  request — so every field stays registered regardless of which tab is visible.
  `onInvalid` switches to the tab holding the first error, otherwise a failed
  submit shows nothing.
- Tab definitions live in `service-editor-navigation.ts` (id, label, group, icon,
  and the fields each tab owns — that last one drives `onInvalid`). An unknown
  or missing `tabName` falls back to Basics.
- Tab links need `scroll={false}`, or every switch jumps to the top of the page.
- `useSearchParams` would need a `<Suspense>` boundary on a prerendered route.
  These routes are dynamic (they read cookies via `getSessionActor()`), so it
  does not apply — re-check if that ever changes.
- **Minutes are canonical for `minimumNotice`/`maximumAdvanceBooking`.** Both
  columns are bare `INTEGER`s with no unit stored anywhere, so the amount/unit
  pair exists only in the form. `duration-unit.ts` owns the conversion:
  `toMinutes` on write, `fromMinutes` on read picking the largest unit that
  divides evenly (120 → "2 Hours").
- **The slug is frozen on edit.** It is derived from the title on create and
  round-tripped untouched afterwards; re-deriving it from a retitle would break
  every booking URL already handed out and can collide with
  `uq_services_organization_slug`.
- **"Requires confirmation" maps to the policy's `autoConfirm`, not to a
  service-level field.** `services.requires_confirmation` used to duplicate it
  and has since been dropped from the schema and the API (`ServiceRequest`/
  `ServiceResponse` no longer carry `requiresConfirmation`) — so the client
  writes and reads `autoConfirm` only.
- **`bookingWindowType` is a plain `VARCHAR(50)`** with no CHECK constraint, no
  native enum and no server-side consumer yet. `ROLLING`/`FIXED` in
  `BOOKING_WINDOW_TYPES` is this client's vocabulary, not a platform contract.

Still reserved here: the cover-image upload (`core.services` has no image column
and the platform exposes no upload endpoint) and the resource-assignment panel.

## The widgets editor

`/dashboard/widgets` is a card list with two stat tiles and type/status filters
bound to the URL; `/dashboard/widgets/new` and `/dashboard/widgets/[id]` share
one `WidgetEditor` — a three-step wizard (Basics, Security, Review) carried as
`?step=`, built on the same rules as the services editor plus a few of its own.

- **The step is a query param, never a route segment**, for the services
  editor's reason and one more: the generated key pair exists only in form
  state. A route segment would unmount the form on every step change and
  discard the secret before it was ever saved. Server pages read no
  `searchParams`; `WidgetEditor` reads the step client-side. All three panels
  stay mounted.
- **The stepper is on top, not a left rail.** A wizard is sequential and reads
  top to bottom; the services editor's rail exists because its tabs are
  peers. Every stepper cell is a plain `Link` — gating lives in Continue
  (`trigger(fieldsForStep)`) and in Save, so someone going back to fix a value
  is never trapped on a later step. "Complete" ticks are derived from the
  current values, never stored.
- **The secret lives in form state and nowhere else.** `POST
  /api/widgets/credentials` is the only route whose body carries a plaintext
  secret; it is generated on demand, held in `credentials` until Save, and
  never written to storage, a cookie, the URL, or a Server Component prop. A
  reload mid-wizard loses it — that is correct, and the Review step says so.
  Nothing logs request or response bodies on the widget BFF routes.
- **Only `INLINE` is selectable.** The platform accepts four `WidgetType`s; this
  client writes one. The other three render disabled with a "Soon" chip so the
  roadmap is legible without pretending to work. `SUPPORTED_WIDGET_TYPES` is
  the single list the picker and the filter both read.
- **Origin is an embed allow-list, not a redirect URL.** The platform normalises
  it to `scheme://host[:port]` and is the authority; the client regex is a UX
  guardrail. A blank origin persists as `null`, and the card shows the product
  host (`pleasebookmee.com`, the same spelling as
  `SUCCESS_REDIRECT_URL_PLACEHOLDER`) as the visible fallback with a tooltip
  saying what `null` actually means.
- **Delete is a soft delete.** `DELETE` sets `status = REVOKED`; revoked widgets
  never appear in the list and are excluded from `total`, so the Disabled
  tile's "n revoked" detail is the only trace of them. A revoked widget's edit
  URL → `notFound()`, since the platform answers `409` to any `PUT` on it.
- **The enabled toggle is edit-only.** Create always yields `ACTIVE`, so
  `status` is only meaningful on `PUT`; the form's `enabled` boolean maps to
  `ACTIVE`/`DISABLED`.
- **`statusOverrides` cannot re-label a BFF error.** `normalizeApiError`
  returns the BFF's already-normalised body before consulting overrides, so
  `createWidget` rewrites its 409 message after normalising instead.
- Third copies of `StatTile`, pagination and the status badge live under
  `features/widgets/components/` on purpose — extracting shared components was
  out of scope for TASK-0008 and is noted as a follow-up.

## Environment

`lib/env.ts` validates everything at startup with zod, split into two surfaces:

- `clientEnv` — safe anywhere; only `NEXT_PUBLIC_*` values.
- `serverEnv()` — a **function**, throws if called in the browser.

`NEXT_PUBLIC_*` must be referenced as complete literal property accesses
(`process.env.NEXT_PUBLIC_FOO`) — Next.js does a build-time find-and-replace, so
`process.env[key]` silently yields `undefined` in the browser.

Copy `.env.example` → `.env` to get started. `.gitignore` ignores `.env*` but
un-ignores `.env.example`.

## Google OAuth2 — local setup & open gaps

### Redirect URI

The redirect URI must point at the **Spring** callback, never a Next.js URL.
Set in `server/.env`:

```
GOOGLE_REDIRECT_URI=http://localhost:8080/api/v1/integrations/google/callback
```

It must stay identical to `GoogleIntegrationController`'s mapping
(`@RequestMapping("/api/v1/integrations/google")` + `@GetMapping("/callback")`).
`GoogleAuthorizationUrlBuilder` sends this value as `redirect_uri` on the
authorization request and `DefaultGoogleTokenClient.exchangeAuthorizationCode`
sends it again on the token exchange; Google requires both to match the
registered value byte for byte, so if that mapping ever moves, this must move
with it.

There is deliberately **no Next.js route at the redirect URI**. The Spring
callback is `permitAll`, owns the code exchange, and redirects back to the
frontend itself — a Next.js hop would duplicate it and have nothing to call.

### Google Cloud Console (cannot be verified from the codebase)

Console → Credentials → your OAuth client:

- **Authorized redirect URIs** must contain the exact URL above. A trailing
  slash difference fails with `redirect_uri_mismatch`.
- **Authorized JavaScript origins** must contain `http://localhost:3000` — a
  *different* field, required by flow 1 (Sign-In), not flow 2.

### `oauthConnectionId` wire type — plan doc was wrong

`GOOGLE_OAUTH_FLOW.md` §2 predicted `BigInteger` serializes as a JSON *string*.
It does not: the server registers no custom Jackson number handling (no
`ObjectMapper` bean, no `jackson:` block in `application.yaml`), so Spring
Boot's defaults apply and it serializes as a **number**. Typed as `number` in
`google-connection.ts`. Nothing in the UI reads it — every operation is keyed by
`oauthConnectionUid` — so if this ever does change, the blast radius is one line.

### One-shot registration: built, with one open prerequisite

Flow 3 is implemented on both sides. `features/auth/GOOGLE_ONE_SHOT_FLOW.md` is
the build plan it was written from; this file is the current description.

**It still provisions tenant-less accounts.** `UserProvisioningService` writes no
`tenant.tenants` row (`SERVER_AGENTS.md` → "Workspace provisioning —
INCOMPLETE"), and this flow provisions through that same component. A green
end-to-end run therefore proves the *flow* works, not that the workspace is
complete. Do not read one as the other.

Related: every user **is** a tenant under the current product rule, with no null
case. The nullable-tenant language in `SECURITY.md` §5 is stale and is being
corrected alongside the loader code, not before it.

### Still-open gaps (not blockers)

- **No scope enforcement at call time.** Granted scopes are persisted but
  nothing checks them before a Calendar/Sheets call. Belongs in the consumer,
  once one exists.
- **No concurrency guard on connect.** Two simultaneous consents for the same
  Google account both attempt the upsert; the unique constraint prevents a
  duplicate row but the loser surfaces as a raw 500 rather than being retried.
- **`GET /connections` has no BFF route**, deliberately — the page's Server
  Component calls the gateway directly. Add one only if something client-side
  needs to refetch without a full `router.refresh()`; don't build it
  speculatively.

## Before you commit

```bash
npx tsc --noEmit    # types
npx eslint .        # lint
npm run build       # full production build
```
