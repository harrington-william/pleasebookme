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
  (auth)/{login,register}/page.tsx   Route files: metadata only, no logic
  api/auth/*/route.ts                BFF boundary — the only caller of gateways
  dashboard/page.tsx                 PLACEHOLDER, replace with the real one
components/
  ui/                                shadcn-managed. Do not hand-edit; the CLI
                                     overwrites this directory.
  background/                        Our own shared visual primitives
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

## Platform contract & known gaps

Auth endpoints are under `/api/v1/auth` and are the only `permitAll()` paths.

| Endpoint    | Notes                                                        |
| ----------- | ------------------------------------------------------------ |
| `/register` | 201, returns tokens directly — registration signs you in      |
| `/login`    | 200, authenticates by **username**, not email                 |
| `/refresh`  | 200, **rotates** the pair — persist both tokens               |

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

## Google OAuth2 — two unrelated flows

The single most important thing to understand: **there are two Google flows and
they share nothing on the frontend.** Conflating them is the easiest mistake to
make in this area.

| | **Sign-In** (authentication) | **Delegated authorization** |
|---|---|---|
| Question | who is this user? | may we act on their behalf? |
| Endpoint | `POST /api/v1/auth/google` | `/api/v1/integrations/google/*` |
| Auth | `permitAll` — the ID token *is* the credential | Bearer |
| Mechanism | ID token from Google Identity Services, posted | authorization code + browser redirect |
| Google Console field | Authorized **JavaScript origin** | Authorized **redirect URI** |
| Client secret used | no | yes (server-side only) |
| Produces | a platform session (our cookies) | encrypted row in `integration.oauth_connections` |
| Frontend code | `features/auth/` | `features/integrations/google/` |

They live in separate feature folders on purpose: `features/auth/` owns the
platform *session*; `features/integrations/google/` owns a *post-login*
integration that maps onto the backend's own `integration` bounded context.

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
  The server's `DEFAULT_REDIRECT_AFTER` is `/settings/integrations` — *without*
  the `/dashboard` prefix — which would land outside `proxy.ts`'s protected
  prefix on a route that does not exist. Never rely on the server default. This
  is enforced in `app/api/integrations/google/connect/route.ts`, not in the
  browser, so a caller cannot forget it.
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
