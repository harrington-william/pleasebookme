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
2. **No CORS.** `SecurityConfig` on the server configures no CORS at all. Since
   the browser only ever talks to its own origin, that never becomes a problem.
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
  schemas/                           zod schemas + form→wire mappers
  services/                          <domain>-api.ts     browser → BFF
                                     <domain>-gateway.ts server → Spring
                                     <domain>-errors.ts  error normalization
  types/                             wire contract mirroring server DTOs
lib/                                 cross-cutting: axios, env, session, jwt
```

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
5. **Google OAuth2 is not wired.** Dependencies and `application.yaml` exist,
   but `security/oauth/google/` is empty and `SecurityConfig` never calls
   `.oauth2Login()`.

## Reserved features

Where the backend is not ready, the UI is built but visibly inert, so the
finished flow is reviewable now and switches on without a redesign later. Each
carries a comment naming the exact backend blocker.

Currently reserved: Google sign-in (both screens, gated on
`NEXT_PUBLIC_GOOGLE_OAUTH_ENABLED`), `/api/auth/refresh` (works, nothing calls
it automatically yet), `/api/auth/me`, and the `/forgot-password` link.

Do not "finish" these by inventing client-side behaviour. Implement the backend
first.

## Environment

`lib/env.ts` validates everything at startup with zod, split into two surfaces:

- `clientEnv` — safe anywhere; only `NEXT_PUBLIC_*` values.
- `serverEnv()` — a **function**, throws if called in the browser.

`NEXT_PUBLIC_*` must be referenced as complete literal property accesses
(`process.env.NEXT_PUBLIC_FOO`) — Next.js does a build-time find-and-replace, so
`process.env[key]` silently yields `undefined` in the browser.

Copy `.env.example` → `.env` to get started. `.gitignore` ignores `.env*` but
un-ignores `.env.example`.

## Before you commit

```bash
npx tsc --noEmit    # types
npx eslint .        # lint
npm run build       # full production build
```
