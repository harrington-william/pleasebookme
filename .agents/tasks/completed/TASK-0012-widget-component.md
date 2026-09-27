# Task Contract

## 1. IDENTITY

Title: Embedded widget — the `widget/` component library (`pleasebookme/`
       core + the barbershop ecosystem's first variant, `kinetic`), the
       Bearer widget channel `/api/v1/widget/**` that serves it, built on
       the booking flow extracted out of `service/widget/fullpage`, and the
       small identity-pipeline completions the channel needs to be a real
       authenticated surface — `requireWidget()`, the widget-loader guard
       order, an active-status check at request time, and rate limiting.
Domain: Server / `service/widget/` (new orchestration package — the booking
        flow extracted out of `service/widget/fullpage`, plus the embedded
        channel), `service/widget/fullpage` (shrinks to a resolver + a
        delegate), `security/identity` (two small, additive completions —
        see D5), `security/config` (one interceptor path pattern), `tenant`
        seed (V144)
        Widget / `widget/pleasebookme/**` (the library), `widget/app/**`
        (the dev harness)
Priority: High — the dashboard can already create a widget with a key pair,
          and nothing on the platform can consume it. This is the surface
          the business actually sells (source-code widgets pasted into
          client sites); until it exists the widget page is a form that
          mints credentials for nothing.
Risk: Medium. The flow that `service/widget/fullpage` runs today is moved,
      not rewritten — its tests move with it. Four small, well-precedented
      completions land under `security/identity` and `security/config`
      (D5) — none of them touch `security/authorization`, which stays
      completely out of this task (D4). Risk concentrates in: (a) the
      extraction being a true move, (b) the four completions staying
      additive — mirroring an existing method/pattern rather than
      inventing one, (c) the library's portability rule — one `@/` import
      inside `pleasebookme/` silently breaks every future paste, (d) the
      token strategy in the browser, which must survive a 15-minute
      expiry without a refresh token.

Status: ACTIVE

Decided by the task author on 2026-09-22 (do not re-open without escalating):

| # | Decision | Choice |
|---|---|---|
| D1 | Distribution model | **Copy-paste source.** The business builds the tenant's Next.js site and pastes the widget in; there is no loader script, iframe, npm package or CDN in this task. `widget/pleasebookme/` is the library and the *only* folder that is ever copied; `widget/app/` is a dev harness that renders the library and is never copied. Inside `pleasebookme/` every import is relative — no `@/`, nothing from `widget/components`, `widget/lib` or `widget/app`. The later stages (npm package, iframe) build on this folder without restructuring it (§16). |
| D2 | Server channel | `/api/v1/widget/**`, Bearer, widget JWT. Four endpoints mirroring the public channel 1:1 (§9.2) minus the `{orgSlug}` segment — the token already says which tenant. The organization is derived from `WidgetPrincipal.tenantId()`; no tenant, organization or widget identifier is accepted from the caller. |
| D3 | Flow home and naming | **Updated 2026-09-22 — the sibling rename has already landed.** `service/publicbooking` is now `service/widget/fullpage`, its classes are `FullPageWidgetController` / `FullPageWidgetService(Impl)`, its five DTOs are already `WidgetOrganizationResponse`, `WidgetServiceSummary`, `WidgetServiceResponse`, `WidgetBookingRequest`, `WidgetBookingResponse`, and the hosted page's client route is `/booking/{orgSlug}`. This task therefore **moves, and does not rename**: the channel-independent flow leaves `service/widget/fullpage/` and lands at `service/widget/barbershop/` as `BarbershopBookingService` / `BarbershopBookingServiceImpl`, taking the five DTOs and `SlotUnavailableException` with it under their existing names. The full-page adapter keeps its own names and its `/api/v1/public` mapping, and shrinks to "resolve org by slug → delegate". Result: `service/widget/{barbershop, embedded, fullpage}`. |
| **D4** | **What stays out of scope: the authorization-policy engine, and only that** | A cross-domain authorization policy plan owns `security/authorization` — `@PreAuthorize`, `AuthorizationService`, `ScopeResolver`, and wiring `WidgetCapabilityPolicy`/`WidgetTenantIsolationPolicy` onto these endpoints all stay out of this task. **Correction, 2026-09-22:** an earlier pass of this contract read "no new policies" as "no security work at all" and dropped four things that are not policy work — they are completions of the existing identity pipeline (`security/identity`) that every other actor type already has. D5 lists them; they are back in scope. |
| **D5** | **The four completions this task restores, and exactly what they touch** | (1) `CurrentPrincipalProvider.requireWidget()` + `DefaultCurrentPrincipalProvider` impl, mirroring the existing `requireUser()` exactly (throws `ForbiddenActorException` for a non-widget principal, `UnauthenticatedException` for none). (2) `DefaultWidgetIdentityLoader.loadByPublicKey`: the `status != ACTIVE` and `expiresAt` checks move to run **before** `validateOrigin`'s `originValidation` guard, so a revoked or expired widget is refused at bootstrap regardless of whether an origin is registered — today they only run when `originValidation = true`. (3) `EmbeddedWidgetOrganizationResolver` calls `principal.isActive()` after `requireWidget()` and throws the existing `WidgetNotActiveException` (401) on `false` — the same check `ActorStatusPolicy` would make through the authorization engine for every other resource, done here directly because the engine is not invoked (D4). (4) `PublicRateLimitInterceptor` is registered for `/api/v1/widget/**` too, reusing the existing Redis buckets, keys and fail-closed behaviour unchanged. Touches: `security/identity/context/*`, `security/identity/loader/widget/DefaultWidgetIdentityLoader`, `security/config/WebConfig` (one line in `addInterceptors`). **Does not touch** `security/authorization/**`, `security/token/**`, `security/config`'s CORS registration, `WidgetPrincipal`, or `application.yaml`. |
| D6 | Bootstrap contract unchanged | `POST /api/v1/auth/widget/bootstrap` keeps its current contract: `{ publicKey, secretKey, origin }` in the **body**, all three `@NotBlank`. The library sends `origin: window.location.origin`. Reading `Origin` from the request header instead is not built here — `SECURITY.md` describes the header check as the intent; the code checks the body value; this task records that discrepancy (§9.8) rather than closing it. |
| D7 | CORS: the existing allow-list, not an open one | `app.cors.allowed-origins` already contains `http://localhost:3002`, so the harness needs **zero** configuration change. A real client site's origin is added to that list by hand as an onboarding step (§9.7) — which fits the business model, since the business already builds and deploys the client's site. No `allowedOriginPatterns("*")`, no reflecting `widget_origins` into CORS. |
| **D8** | **Where the key pair lives** | The client agency's own site repo — in a `.env`/`.env.local` file the agency creates when it pastes the widget in, never committed. PleaseBookMe does not store a second copy anywhere on the client side, and stores nothing new on the server side beyond the existing `widgets.secret_key` hash. The flow: create a widget in the dashboard → the create response shows `publicKey`/`secretKey` once → the agency copies both into `NEXT_PUBLIC_PBM_PUBLIC_KEY` / `NEXT_PUBLIC_PBM_SECRET_KEY` in the site's own `.env.local` → the site's own page reads `process.env.NEXT_PUBLIC_PBM_*` and passes them as props to `PleaseBookMeProvider`, exactly as the harness does in this task (§9.8 Step 28). `PleaseBookMeProvider` itself never reads `process.env` — it takes `publicKey`/`secretKey` as plain string props, so the library has no opinion on *how* a consuming site sources them; env vars are simply what every Next.js site already does for a public key. Consequence, already priced into D1: `NEXT_PUBLIC_*` values are inlined into the client bundle at build time, so both keys are visible to anyone who opens the page's JavaScript — the same accepted posture as before, now made explicit rather than assumed. |
| D9 | Session in the widget | The 15-minute access token lives in memory only. No refresh token for widgets (`WidgetBootstrapResponse` has none; do not add one). On a 401 the client re-bootstraps once, single-flight, and replays the request; a second 401 surfaces as the `unavailable` state. On a 429 the client surfaces the countdown copy and does not retry automatically. |
| D10 | Ecosystem | `WidgetOrganizationResponse` gains `ecosystem` (the `tenant.ecosystems.code`, e.g. `GENERAL`, `BARBERSHOP`). The library's `ecosystems/registry.ts` maps a code to a widget component; `GENERAL` and `BARBERSHOP` both map to the barbershop widget because "Consultant Meeting" is appointment-centric too. A `BARBERSHOP` row is seeded (V144); provisioning still assigns `GENERAL` (changing that is onboarding's decision, §16). No server-side dispatch by ecosystem — one flow exists. |
| D11 | Logic per ecosystem, presentation per variant | `ecosystems/barbershop/logic/` is headless (no JSX): types, steps, schema, the slots hook and `useBookingFlow` — the state machine lifted out of `client/features/public-booking/components/booking-widget.tsx`. `variants/kinetic/` is components only and owns no state beyond form inputs. A second barbershop variant is a second `variants/` folder; a hotel is a second `ecosystems/` folder with its own `logic/`. |
| D12 | Styling | `core/styles/tokens.css` carries every non-default token the variant uses — the type scale, the 4 px spacing scale, `surface*`, `success`/`warning` — as `@theme inline` mappings plus a `.pbm-widget { … }` block with the colour values. Colour mappings **must** be `@theme inline` so a value set on `.pbm-widget` is what `bg-primary` resolves to inside the widget and the host's own `--primary` is untouched outside it. Every class used under `variants/` must resolve on a host that has Tailwind v4 + shadcn defaults + `tokens.css` and nothing else; the harness's `globals.css` proves it by defining none of them itself. No Stitch hex reaches the code (TASK-0011 rule, same mapping as §9.10 there). |
| D13 | Booking behaviour not built | No holds (`SELECTEDSLOT.CREATE` is allow-listed in `WidgetCapabilityPolicy` for when the engine is wired, but the flow does not hold; the host lock covers correctness), no capacity, no captcha, no idempotency, no notifications, no customer upsert. As TASK-0011. |
| D14 | Demo posture | As TASK-0011 D20: agents build, open the harness once to look, and stop. **Agents do not test the flow's functionality**; the author does, with the B-series in §12, which is off-limits to agents and reviewers. |
| D15 | The hosted page is not migrated | `client/features/public-booking/` keeps its own copy of the flow in this task. Making the hosted page "client #0" of the library is a later task (§16); the date math is copied here **verbatim** so that later diff is empty. |

---

## 2. INTENT

Make a widget the dashboard created actually do something. A tenant (today:
the business, on the tenant's behalf) creates a widget in the dashboard,
receives a `publicKey`/`secretKey` pair, and receives a folder of source.
The agency puts the key pair in the site's own `.env.local` (D8) and pastes
the folder into the site with three environment variables; it then renders
the same six-step booking flow the hosted page renders — Service → Date →
Time → Details → Review → Done — against the same slot engine and the same
lock-and-revalidate write, as that widget's tenant.

Two things are built, and they must stay distinguishable:

| | Server: the embedded channel | Widget: the library |
|---|---|---|
| Question | how does a *widget* reach the booking flow? | what does the business paste into a site? |
| Identity | `WidgetPrincipal` from the widget JWT | a key pair in the site's own `.env.local` (D8) |
| Tenant scope | derived from the principal — never from the caller | none; the server decides |
| Lives in | `service/widget/{barbershop,embedded}` | `widget/pleasebookme/{core,ecosystems}` |
| Reuses | the flow extracted from `service/widget/fullpage`, byte for byte where possible | `client/features/public-booking/**`, ported file by file (§9.6) |

```text
tenant site (barbershop.com)                                   Spring
────────────────────────────                                   ──────
site's own .env.local: NEXT_PUBLIC_PBM_PUBLIC_KEY / _SECRET_KEY / _API_URL   (D8)
<PleaseBookMeProvider publicKey secretKey apiUrl>
  BarbershopBookingWidget
    useBookingFlow(api) ──▶ session.bootstrap()  ──POST─▶ /api/v1/auth/widget/bootstrap   { publicKey, secretKey, origin }
                                                 ◀─────  { accessToken }                  ← unchanged endpoint (D6)
                        ──▶ api.getOrganization() ──GET──▶ /api/v1/widget/organization      Bearer
                                                          rate-limited (D5.4) ─┤
                                                          EmbeddedWidgetOrganizationResolver:
                                                            requireWidget() → isActive()? → tenant → served? → org   (D5.1, D5.3)
                                                          BarbershopBookingService.getOrganization(served)   ⟵ the same code the hosted page runs
                        ──▶ api.getSlots(...)     ──GET──▶ /api/v1/widget/services/{svc}/slots?date=
                        ──▶ api.createBooking()   ──POST─▶ /api/v1/widget/services/{svc}/bookings
                                                          lock host user → SlotService → insert booking + attendee, or 409
```

### This library is a demo of the *mechanism*

The `kinetic` variant is the hosted page's demo design re-homed, not a new
design. What is being proven is that a folder can be pasted into a foreign
Next.js repo and work: relative imports, self-contained tokens, env-sourced
keys, no router, no BFF, no cookies. Fidelity and polish are deferred
exactly as in TASK-0011 (D14).

### What this task still does not defend against

D4 draws the line precisely: the authorization *policy engine* is out —
no `@PreAuthorize`, no scope resolution, no per-permission-slug capability
check. What is in scope is everything the existing identity pipeline
already does for a user, done the same way for a widget: a valid,
unexpired, correctly-typed, active-status Bearer token is required, and
requests are rate limited. What remains genuinely deferred, unchanged from
before: no per-request origin re-verification after bootstrap (origin is
checked once, at bootstrap, only when an origin was registered), the
bootstrap origin arrives in the body rather than the header (D6), and the
authorization engine's fine-grained capability model
(`WidgetCapabilityPolicy`'s slug allow-list) is not consulted — the channel
exposes exactly four operations by having exactly four endpoints, not by a
policy that could be asked to permit a fifth. Record any further gap you
notice in `SECURITY.md` (§9.8) and move on — do not build past D4.

---

## 3. DELIVERABLES

### Server — `service/widget/` (new)

1. `service/widget/ServedOrganization` — `record(OrganizationEntity organization, String ecosystemCode)`, what both channel resolvers produce and the flow consumes. The ecosystem code is read into the record by the resolver so the flow never touches `tenant.ecosystem` lazily.
2. `service/widget/ServedTenantStatuses` — the `Set<TenantStatus>` of `{ACTIVE, TRIAL}` moved out of `FullPageWidgetServiceImpl`, shape of `ReservedOrganizationSlugs`.
3. `service/widget/barbershop/service/BarbershopBookingService` +
   `service/impl/BarbershopBookingServiceImpl` — the four flow methods,
   each taking a `ServedOrganization` (§9.3). Moved from
   `FullPageWidgetServiceImpl` with the slug/tenant resolution removed.
4. `service/widget/barbershop/dto/` — `WidgetOrganizationResponse` (+ `ecosystem`),
   `WidgetServiceSummary`, `WidgetServiceResponse`, `WidgetBookingRequest`,
   `WidgetBookingResponse`. **Moved** from `service/widget/fullpage/dto/` — they
   already carry these names, so this is a package change and one added field,
   no rename (§9.2).
5. `service/widget/barbershop/exception/SlotUnavailableException` — moved;
   its `GlobalExceptionHandler` import updated, status 409 unchanged.
6. `service/widget/embedded/EmbeddedWidgetOrganizationResolver` — §9.4,
   using `requireWidget()` and the `isActive()` check from D5.
7. `service/widget/embedded/service/EmbeddedWidgetService` +
   `service/impl/EmbeddedWidgetServiceImpl` — resolve → delegate, four
   methods, no other logic.
8. `service/widget/embedded/controller/EmbeddedWidgetController` —
   `@RequestMapping("/api/v1/widget")`, four handlers (§9.2).

### Server — `service/widget/fullpage/` (shrinks; names unchanged)

9. `service/widget/fullpage/SlugOrganizationResolver` — the former
   `resolveServedOrganization` (`findBySlug` → tenant → served-status gate)
   returning `ServedOrganization`. There is no reserved-slug check to carry:
   it was removed when the hosted page moved under `/booking/`.
10. `FullPageWidgetServiceImpl` — becomes resolve → delegate to
    `BarbershopBookingService`; loses every repository field. Method
    signatures, `FullPageWidgetService` and the DTO types are unchanged.
11. `FullPageWidgetController` — import changes only.

### Server — `security/identity`, `security/config` (D5 — the four completions)

12. `CurrentPrincipalProvider.requireWidget()` + implementation in
    `DefaultCurrentPrincipalProvider` — mirror of `requireUser()`.
13. `DefaultWidgetIdentityLoader.loadByPublicKey` — hoist the `status`
    and `expiresAt` checks above the `originValidation` guard in
    `validateOrigin`, so they run unconditionally.
14. `WebConfig.addInterceptors` — add `"/api/v1/widget/**"` to the existing
    `PublicRateLimitInterceptor` registration.

### Server — `tenant/`

15. `server/src/main/resources/db/migration/tenant/V144__tenant_seed_barbershop_ecosystem.sql`
    + `database/init/tenant/V227__tenant_seed_barbershop_ecosystem.sql`
    (commented original) — one `INSERT` shaped like V140, `code = 'BARBERSHOP'`,
    `status = 'ACTIVE'`, `ON CONFLICT (code) DO NOTHING`.

### Server — tests

16. `service/widget/barbershop/BarbershopBookingServiceImplTest` — the
    existing `FullPageWidgetServiceImplTest` cases minus the two resolver
    cases, calling the flow with a `ServedOrganization`.
17. `service/widget/fullpage/SlugOrganizationResolverTest` — unknown slug;
    missing tenant; suspended tenant; served tenant returns the record with
    the ecosystem code.
18. `service/widget/embedded/EmbeddedWidgetOrganizationResolverTest` — user
    principal → `ForbiddenActorException`; no principal →
    `UnauthenticatedException`; inactive widget → `WidgetNotActiveException`;
    tenant suspended → `OrganizationNotFoundException`; happy path returns
    the tenant's organization and reads the tenant id from the principal
    and nowhere else.
19. `security/identity/context/DefaultCurrentPrincipalProviderTest` —
    add (or extend) coverage for `requireWidget()`: a `WidgetPrincipal`
    passes through; a `UserPrincipal` throws `ForbiddenActorException`; no
    authentication throws `UnauthenticatedException`.
20. `security/identity/loader/widget/DefaultWidgetIdentityLoaderTest` — a
    revoked widget with `originValidation = false` is refused
    (`WidgetNotActiveException`); an expired widget with validation off is
    refused; an active widget with validation off and no origin succeeds.
21. `ServerApplicationTests` stays green (context load catches a missing
    handler import or a duplicate bean name).

### Widget — `widget/pleasebookme/` (the library)

22. `core/api/client.ts`, `core/api/widget-api.ts`, `core/api/errors.ts`
23. `core/auth/session.ts`, `core/auth/PleaseBookMeProvider.tsx`,
    `core/auth/use-pleasebookme.ts`
24. `core/lib/cn.ts`, `core/lib/date-math.ts` (+ `date-math.test.ts`),
    `core/lib/format.ts`, `core/lib/calendar-link.ts`
25. `core/ui/button.tsx`, `input.tsx`, `label.tsx`, `native-select.tsx`,
    `spinner.tsx`, `error-message.tsx`
26. `core/styles/tokens.css`
27. `ecosystems/barbershop/logic/types.ts`, `steps.ts`, `details-schema.ts`,
    `use-available-slots.ts`, `use-booking-flow.ts`
28. `ecosystems/barbershop/variants/kinetic/BarbershopBookingWidget.tsx`,
    `components/*` (§9.6), `theme.css`, `README.md`
29. `ecosystems/registry.ts`
30. `pleasebookme/README.md` — the distribution guide, including the D8
    key-storage step (§9.7)

### Widget — harness and tooling

31. `app/globals.css` — imports `../pleasebookme/core/styles/tokens.css`;
    defines nothing the library defines.
32. `app/page.tsx` — catalog: one card per `ecosystem × variant`, linking to
    its harness route.
33. `app/barbershop/kinetic/page.tsx` — reads `NEXT_PUBLIC_PBM_*` from
    `.env.local` (D8) and passes them as props into the provider — the
    reference implementation of the step every real client site repeats.
34. `package.json` — `@hookform/resolvers` (runtime, the port needs it),
    `vitest` (dev), a `test` script, `"dev": "next dev -p 3002"`;
    `.env.example` with the three `NEXT_PUBLIC_PBM_*` names and no values.

### Documentation

35. `SERVER_AGENTS.md` — new `# Widget channel` section: the package layout
    of `service/widget/`, the resolver-derives-tenant-from-the-token rule,
    the four D5 completions and exactly what they touch, the CORS
    allow-list as an onboarding step (D7), V144, schema version → v144. The
    `# Public booking page` section gets a one-line pointer to the
    extraction.
36. `SECURITY.md` — "Widget Authentication" gains an **Embedded widget
    channel** subsection: what authenticates it (bootstrap, unchanged),
    what gates it now (`requireWidget()`, the active-status check, rate
    limiting — D5), what scopes it (the principal's tenant), and the
    narrower deferred list from D4/D6 (the authorization engine's capability
    model is not consulted; bootstrap's origin is a body field, not a
    header). "Not Yet Implemented" updated: the first widget-only endpoints
    now exist and are rate limited.
37. `widget/AGENTS.md` — replaces the create-next-app stub: the library /
    harness split, the relative-import rule, the token rule, the D8
    key-storage rule, the port provenance, the distribution steps, the
    roadmap (npm → iframe).
38. Obsidian: `API/Widget/Embedded/Widget API Summary.md` +
    `Get widget organization.md`, `Get widget service.md`,
    `Get widget slots.md`, `Create widget booking.md` via
    `.skills/documentation/api-documentation`; `API/Public/Get public organization.md`
    gains the `ecosystem` row; `Services/Widget/Barbershop Booking Service.md`
    and `Services/Widget/Embedded Widget Service.md` via
    `.skills/documentation/service-documentation`;
    `Services/Widget/Full Page Widget Service.md` updated to say it
    delegates; `Security/Identity/WidgetPrincipal.md` gains a line on
    `requireWidget()` and the active-status check; `Database/Schemas/Tenant/`
    ecosystem seed note (V144).
39. `.agents/decisions/ADR-0002-widget-library-is-copied-source.md` — D1,
    D8, D11, D12, the rejected alternatives (loader + iframe now; Vite
    bundle), and the roadmap.

### Closing report

40. Modified-file list, executed validation commands **with output**, the
    exact JSON of one real response from bootstrap and each of the four
    widget endpoints, one desktop and one mobile screenshot of the harness
    at each step, unresolved risks, the §16 items you were tempted to do and
    did not, a line confirming
    `git diff --stat -- server/src/main/java/com/pleasebookme/server/security/authorization`
    is empty (D4), and a **"Ready for the author's functional pass"** line
    confirming no B-series scenario was executed by an agent.

Review agents produce `.agents/reviews/TASK-0012-widget-component-review.md`.

---

## 4. SCOPE

### In scope

- Everything in §3.
- The extraction of the flow out of `service/widget/fullpage` — it is the
  only way two channels share one implementation, and the flow's tests
  move with it.
- The `ecosystem` field on the organization response (both channels see it;
  the hosted page ignores it).
- The V144 seed and its init mirror.
- The four D5 completions to the identity pipeline, and documenting the
  narrower D4/D6 deferred list in `SECURITY.md`.

### Out of scope (reasoning in §16)

- The authorization policy engine — `@PreAuthorize`, `AuthorizationService`,
  `ScopeResolver`, wiring `WidgetCapabilityPolicy`/`WidgetTenantIsolationPolicy`
  onto these endpoints (D4).
- Reading `Origin` from the bootstrap request's header instead of its body;
  any per-request origin re-verification after bootstrap (D6).
- Open or dynamic CORS (D7).
- A secrets vault, a server-side copy of the key pair beyond the existing
  hash, or any UI for retrieving a lost secret key (D8) — losing the secret
  means rotating the widget's credentials, same as today.
- ~~Renaming `service/publicbooking` → `service/widget/fullpage` and moving
  the client route under `/booking`~~ — **done on 2026-09-22, before this
  task started.** Nothing here renames it again.
- Migrating `client/features/public-booking/` to consume the library.
- A loader script, an iframe host route, an npm package, a CDN build.
- A dashboard "download your widget" surface; the README is the delivery.
- Changing provisioning's default ecosystem; an ecosystem picker at signup.
- Holds, capacity, captcha, idempotency, notifications, customer upsert.
- Any change to `service/slot/`, `WidgetServiceImpl`, `WidgetController`,
  `JwtAuthenticationFilter`, `AuthenticationTokenFactory`,
  `AuthenticatedPrincipal`, `WidgetPrincipal`, the policies under
  `security/authorization/`.
- A second variant or a second ecosystem.

---

## 5. BOUNDARIES

- **`git diff --stat -- server/src/main/java/com/pleasebookme/server/security/authorization`
  must be empty** (D4). If a change seems to require one, it is
  Escalation #4. Everything else under `security/` is open, but only the
  four D5 completions should actually appear there.
- **The widget channel accepts no tenant, organization, user or widget
  identifier from the caller.** Not in the path, not in the query, not in
  the body. `EmbeddedWidgetOrganizationResolver` is the only place the
  tenant is decided and it reads it from the principal.
- **`service/widget/barbershop` reads no principal.** No
  `CurrentPrincipalProvider`, no `SecurityContextHolder`, no
  `CurrentOrganizationProvider`. It receives a `ServedOrganization` and
  trusts it. The two resolvers are the only principal/slug-aware classes.
- **`security/authorization` is not imported anywhere under
  `service/widget`.** D4. Do not add `@PreAuthorize`, do not call
  `AuthorizationService`, do not add a `ScopeResolver`, do not read
  `WidgetCapabilityPolicy.ALLOWED_SLUGS`.
- **`CurrentPrincipalProvider.requireWidget()` does exactly what
  `requireUser()` does for `UserPrincipal` — nothing more.** It type-checks
  and throws; it does not check `isActive()` (that stays in the resolver,
  same layering as `requireUser()`, which does not check `UserPrincipal`
  account status either).
- **`DefaultWidgetIdentityLoader`'s status/expiry checks move; the origin
  check itself does not change.** Still exact-equality against
  `widget_origins.origin`, still skipped entirely when
  `originValidation = false`.
- **The rate limiter's bucket logic is not touched**, only its path
  registration. Same Redis keys, same fail-closed behaviour, same
  `RateLimitProperties`.
- **The slot engine is read, not changed.** As TASK-0011.
- **The public channel's wire contract does not change** except the added
  `ecosystem` field. Same paths, same statuses, same 404 collapsing.
- **No `@/` import, no `next/*` import, no `window.location` outside
  `session.ts`, no cookie under `widget/pleasebookme/`.** `next/navigation`
  in the port becomes a callback or a state; `router.refresh()` on 404
  becomes the `unavailable` state (§9.5).
- **No token in `localStorage`/`sessionStorage`/a cookie.** Memory only (D9).
- **`PleaseBookMeProvider` never reads `process.env` itself.** D8 — the
  consuming page/site reads its own env and passes props. This keeps the
  library agnostic to how a given site manages its env vars.
- **The harness's `globals.css` defines no token the library defines.** If
  a class in a variant only works because the harness defined it, the
  library is broken.
- **No Stitch hex in the library.** Same grep rule as TASK-0011.
- **Do not modify** `BookingEntity`, `AttendeeEntity`, `ServiceEntity`,
  `OrganizationEntity`, `TenantEntity`, `WidgetEntity`, `WidgetPrincipal`,
  `SlotServiceImpl`, `RateLimitProperties`, `CorsProperties`,
  `application.yaml`, `client/**` (nothing in the dashboard changes in this
  task), `widget/components/ui/*` (shadcn-owned, unused by the library).
- **Do not add a `MethodArgumentNotValidException` handler.** As TASK-0011.

---

## 6. CONSTRAINTS

### Architectural

1. Interface × impl, `@Service` + `@RequiredArgsConstructor`, records for
   DTOs, derived finders only, controllers translate and nothing else,
   `ResponseEntity<T>`, `201` via `ResponseEntity.status(HttpStatus.CREATED)`
   (`CODING_CONVENTIONS.md`, `.skills/technologies/spring-boot/*`).
2. `service/widget` is orchestration (`SERVER_AGENTS.md` → "Package
   structure"): it spans `organization`, `tenant`, `core`, `service/slot`
   and reads a principal. Not under `core/booking`, not under the
   `widget/` bounded context (which is the CRUD for widget rows — two
   different things with one word; keep them apart).
3. **Simple-name discipline against the `widget/` bounded context.** The
   CRUD package already owns `WidgetResponse`, `WidgetDetail`,
   `WidgetCreateRequest`, `WidgetUpdateRequest`, `WidgetService`,
   `WidgetServiceImpl`, `WidgetController`, `WidgetNotFoundException`. No
   class introduced by this task may share a simple name with any of them
   (`GlobalExceptionHandler` imports by simple name). The names in §3 were
   chosen against that list; if you need another, check
   `find server/src -name '<Name>.java'` first.
4. Transactions: flow reads `@Transactional(readOnly = true)`, flow
   `createBooking` `@Transactional`; channel-service reads
   `@Transactional(readOnly = true)`; channel-service `createBooking` has
   **no** annotation, so the resolver runs outside the write transaction
   and the lock is still taken *inside* it, first thing after the service
   and policy are resolved. The resolvers are not transactional themselves.
5. The flow is moved, not rewritten. Reviewers diff
   `BarbershopBookingServiceImpl` against the pre-task
   `FullPageWidgetServiceImpl` and expect: resolver code gone, one parameter
   type changed, one field added to one response, imports. Anything else
   is a finding.
6. `requireWidget()` and `DefaultWidgetIdentityLoader`'s reordering are
   each a small, additive diff against an existing, precedented method —
   not new mechanisms. If either needs more than the mirror described in
   D5, stop and escalate (#5, #6) rather than design something new.
7. Comments explain *why* (`.skills/workflows/quality-code-comments`).
   Server: why the resolver checks `isActive()` itself instead of relying
   on the authorization engine (D4); why the loader's checks moved; why
   the channel write method is not transactional; a pointer from the
   resolver to `SECURITY.md`'s deferred list. Widget: the relative-import
   rule at the top of `README.md`; why `@theme inline`; why the token is
   memory-only; why single-flight re-bootstrap; the carried comments from
   `date-math.ts` (verbatim); why `PleaseBookMeProvider` takes keys as
   props rather than reading `process.env` (D8).
8. Widget conventions: `client/AGENTS.md` applies where it is not about the
   BFF — `cn()`, zod v4 (`z.email()`, `{ error }`), react-hook-form +
   `zodResolver`, native controls, `lucide-react` icons, Geist via the
   *host's* layout (the library does not load fonts).
9. Widget dependencies: `@hookform/resolvers` added (runtime); `vitest`
   added (dev). The library's peer list (README) is exactly: `react`,
   `react-dom`, `axios`, `zod`, `react-hook-form`, `@hookform/resolvers`,
   `lucide-react`, `clsx`, `tailwind-merge`, `tailwindcss@4`. Anything else
   is Escalation #7.

### Security

10. **The authorization policy engine stays untouched** (D4) —
    `security/authorization/**` has no diff. This is the one boundary that
    does not relax in this task.
11. `requireWidget()` mirrors `requireUser()` exactly: same exception
    types, same "throw, don't return null" contract. A user token calling
    a widget endpoint gets 403 (`ForbiddenActorException`), not 404 or a
    silent empty response.
12. A widget whose `status` is not `ACTIVE` is refused **both** at
    bootstrap (D5.2, unconditionally now) **and** at every widget-channel
    call (D5.3, via the resolver) — a token minted a moment before
    revocation dies at its very next request, not merely at expiry.
13. The served-tenant gate and the 404 collapsing apply to the widget
    channel exactly as to the public one: a suspended tenant's widget gets
    `OrganizationNotFoundException` (404) with the same message shape, and
    it must not contain the tenant id or the widget uid.
14. Rate limiting on `/api/v1/widget/**` uses the existing read/write
    buckets and fails closed on a Redis error exactly as the public
    channel does — no new "allow on error" branch.
15. The bootstrap endpoint never echoes the secret; the widget never logs
    the token; `session.ts` exposes `getToken()` only to `client.ts`.
    `PleaseBookMeProvider` never logs its `secretKey` prop.
16. CORS keeps its allow-list (D7); the secret key's real protection is
    that it is bound to one origin at CORS level *and* checked once at
    bootstrap when an origin is registered on the widget — not that it is
    hard to find in the bundle (D8 says plainly that it isn't).

### Performance (`.skills/workflows/performance-avoid-quadratic`)

17. `EmbeddedWidgetOrganizationResolver`: `TenantRepository.findById` plus
    the tenant's `organization` and `ecosystem` associations — three
    queries at most; read `ecosystem.getCode()` into the record once.
    `principal.isActive()` is a pure in-memory check on the JWT claims —
    no extra query to re-read widget status from the database.
18. The flow's query counts are TASK-0011 §6 #13–15, unchanged. Reviewers
    re-count after the move.
19. `useBookingFlow` re-fetches nothing on a step change; the slots hook
    keeps its request-key memoisation; `session.ts` bootstraps at most once
    per mount and at most once per 401 (single-flight promise).
20. The interceptor issues one Redis round-trip per request on the widget
    channel too, exactly as it does on the public channel — no per-request
    difference introduced by adding the second path pattern.

### Design

21. The `kinetic` variant is the hosted page's components moved, with class
    names unchanged where the token exists in `tokens.css`. No new screens,
    no new states, no polish (D14).
22. Native `<button>`/`<input>`/`<select>`; 40 px day cells, 48 px pills
    and primary buttons — carried, not re-derived.

---

## 7. DEPENDENCIES

### Already done — do not rebuild

| Component | Status |
|---|---|
| `POST /api/v1/auth/widget/bootstrap` → `WidgetBootstrapResponse(accessToken)`; `WidgetBootstrapRequest(publicKey, secretKey, origin)` all `@NotBlank`; `AuthServiceImpl.bootstrapWidget` → `WidgetIdentityLoader.loadByPublicKey` → `JwtEngine.issueAccessToken` | Exists, **unchanged** (D6). The library calls it as built. |
| `JwtAuthenticationFilter` `case WIDGET -> widgetIdentityLoader.loadByUid(...)`; `AuthenticationTokenFactory` widget branch; `DefaultCurrentPrincipalProvider.find()` returns the `WidgetPrincipal` | Exists — a widget Bearer already reaches `CurrentPrincipalProvider`. `requireWidget()` (this task) is a thin addition on top, not a new resolution path. |
| `WidgetPrincipal(actorType, widgetUid, tenantUid, tenantId, status)` + `isActive()` | Exists, **unchanged**. `tenantId` and `isActive()` are the two fields this task reads. |
| `ForbiddenActorException` → 403, `UnauthenticatedException` → 401, `WidgetNotActiveException` → 401 — all already handled in `GlobalExceptionHandler` | Exist — no new exception type or handler is needed for D5. |
| `SecurityConfig`: `anyRequest().authenticated()`; bootstrap already `permitAll` | Exists — **no matcher is added**; `/api/v1/widget/**` is caught by the existing default. |
| `app.cors.allowed-origins` contains `http://localhost:3002` | Exists — the harness needs no CORS change (D7). |
| `PublicRateLimitInterceptor`, `RedisRateLimiter`, `RateLimitProperties`, `WebConfig.addInterceptors` | Exists — this task adds one path pattern to the existing registration, nothing else. |
| `service/widget/fullpage/**` — the flow, `FullPageWidgetServiceImplTest` (9 cases), `SlotUnavailableException` 409 | Exists — this is what moves. `ReservedOrganizationSlugs` is **not** part of it any more; it guards organization creation only. |
| `TenantEntity.organization`, `.ecosystem`, `.status`; `EcosystemEntity.code`; `V140` seeds `GENERAL` | Exist. |
| `client/features/public-booking/**` (1 559 lines) — the port source; `client/lib/api-error.ts`; `client/lib/axios.ts` (`attachCorrelationId`); `client/lib/utils.ts` (`cn`); `client/components/ui/{input,label}.tsx`; `client/components/form/native-select.tsx` | Exist. §9.6 has the per-file verdict. |
| `client/app/globals.css` `@theme` block (type scale, spacing) + `:root` colours | The source of `tokens.css` values. |
| `widget/` scaffold: Next 16, React 19, Tailwind v4, shadcn `components.json` (`base-nova`), `lib/utils.ts`, `components/ui/button.tsx`, `axios`, `zod@4`, `react-hook-form` | Exists. The harness uses it; the library does not import from it. |
| Dashboard widget page: create widget → credentials shown once → optional origin | TASK-0007/0008. This is how the demo widget is created (Step 2), and how the key pair reaches the agency for D8's flow. |

### Baseline

```text
cd server && ./gradlew compileJava && ./gradlew test
cd ../client && npm run lint && npm run build
cd ../widget && npm run lint && npm run build
```

All green on a clean tree before Step 1. Record the widget `build` output —
it is the create-next-app stub and must build.

### Required infrastructure

- Postgres + Redis via the compose file; `server/.env` with `JWT_SECRET`,
  `REDIS_PASSWORD`, the Google and token-encryption keys (boot fails
  without them, unrelated to this task).
- A dev user with a provisioned workspace (register or Google one-shot) —
  gives an org, an `ACTIVE` `GENERAL` tenant, `consultant-meeting` with a
  policy, Mon–Fri 09–17.
- One widget created through the dashboard for that org, `ACTIVE`, **no
  origin registered** (so `origin_validation` is `false` and bootstrap is a
  pure key check for the harness). Its `publicKey`/`secretKey` go into
  `widget/.env.local` — the harness's own stand-in for D8's "site's own
  `.env.local`".
- A second widget, `DISABLED` or `REVOKED`, for the D5.2/D5.3 manual checks.
- The harness runs on **3002** (`next dev -p 3002`) — the client owns 3000,
  and 3002 is already in the CORS allow-list.

---

## 8. INPUT CONTEXT

### Skills to invoke (mandatory)

- `.skills/workflows/task-generating` — this contract's shape; read once.
- `.skills/technologies/spring-boot/*` and `CODING_CONVENTIONS.md`.
- `.skills/workflows/performance-avoid-quadratic`.
- `.skills/workflows/quality-code-comments`.
- `.skills/documentation/api-documentation` and
  `.skills/documentation/service-documentation` for #38.
- `.skills/domains/authorization/identity-access-engineering` — read before
  touching `security/identity`; it is what makes D5's "mirror, don't
  invent" instruction checkable.

### Read in full, in this order

1. `SERVER_AGENTS.md` → "Package structure", "Public booking page",
   "Widget dashboard backend", "CORS".
2. `SECURITY.md` → "Widget Authentication", "Public Booking Endpoints", and
   the "Known gaps" list. Read D4/D6 of this contract alongside it: the
   authorization engine's capability model and the header-vs-body origin
   check remain gaps this task records rather than closes; everything else
   D5 lists is being closed.
3. `.agents/tasks/active/TASK-0011-full-page-widget.md` §1 (decisions), §9.2
   (wire contract), §9.4 (the flow), §9.8 (the port provenance), §9.12
   (date math), §16. The flow you are moving was specified there; do not
   re-derive it.
4. `.agents/tasks/completed/TASK-0008-widget-page-frontend.md` — how the
   demo widget gets created in the dashboard, and where the key pair is
   shown (this is the moment D8's flow starts).
5. `.agents/decisions/ADR-0001-public-booking-page-is-not-a-widget.md` —
   the two widget kinds. This task builds the *other* one.
6. `obsidian/PleaseBookMe/Database/Schemas/Widget/Widget Types.md` — the
   three types are presentational; nothing here branches on `type`.
7. `widget/CLAUDE.md` / `widget/AGENTS.md` — "This is NOT the Next.js you
   know": read `widget/node_modules/next/dist/docs/` for anything you touch
   under `widget/app/`.

### Source to inspect before writing

Server: `service/widget/fullpage/**` (every file), `FullPageWidgetServiceImplTest`,
`security/identity/context/{CurrentPrincipalProvider,DefaultCurrentPrincipalProvider}`
(read the existing `requireUser()` as the mirror target),
`security/identity/principal/WidgetPrincipal` (read only — `isActive()`
already exists), `security/identity/loader/widget/DefaultWidgetIdentityLoader`,
`security/config/{SecurityConfig,WebConfig}`,
`security/ratelimit/PublicRateLimitInterceptor`, `tenant/tenants/entity/TenantEntity`,
`tenant/ecosystem/entity/EcosystemEntity`,
`db/migration/tenant/V140__tenant_seed_general_ecosystem.sql`,
`global/handler/GlobalExceptionHandler` (the `SlotUnavailableException`,
`ForbiddenActorException`, `UnauthenticatedException`,
`WidgetNotActiveException` handlers — confirm none need edits).

Widget: `client/features/public-booking/**` (every file — the port source),
`client/lib/api-error.ts`, `client/lib/axios.ts` (the browser instance
shape; the library's client is *not* the BFF client), `client/lib/utils.ts`,
`client/components/ui/{input,label}.tsx`, `client/components/form/native-select.tsx`,
`client/app/globals.css` lines 13–200 (the `@theme inline` colour mappings,
the static `@theme`, the `:root` values), `widget/app/globals.css`,
`widget/tsconfig.json`, `widget/components.json`, `widget/package.json`.

### Domain facts

- `WidgetPrincipal.tenantId` → `tenant.tenants.id`; `tenant.organization_id`
  is `NOT NULL`; `tenant.ecosystem_id` is `NOT NULL`. A widget always
  resolves to exactly one organization.
- `WidgetPrincipal.isActive()` already exists (`status == WidgetStatus.ACTIVE`)
  and needs no change — only a new call site in the resolver.
- The access token is `PT15M` (`security.jwt.access-token-life-time`);
  widgets get no refresh token. Hence D9.
- `app.rate-limit.public.{read,write}` already has values
  (`120/min`, `5/min`) from TASK-0011; this task adds no new property.
- Spring Boot's `open-in-view` default is on; do not rely on it — the
  resolver copies `ecosystem.getCode()` into the record (§3 #1).
- `flyway_schema_history` high-water mark before this task: **143**.
  V144 is the only migration; it is a seed, not DDL.

---

## 9. FUNCTIONAL REQUIREMENTS

### 9.1 Migration — V144 and its mirror

```sql
INSERT INTO tenant.ecosystems (code, name, description, icon, status)
VALUES ('BARBERSHOP', 'Barbershop', 'Barbers, hairdressers and salons booking chairs by appointment', '💈', 'ACTIVE')
ON CONFLICT (code) DO NOTHING;
```

Flyway copy comment-free; init copy (`V227`) with the *why*: the code is
what `ecosystems/registry.ts` switches on; provisioning still assigns
`GENERAL`; the icon column is `NOT NULL`. After apply:
`SELECT code FROM tenant.ecosystems ORDER BY code` → `BARBERSHOP, GENERAL`.

### 9.2 Wire contract — the library types against this verbatim

```text
POST /api/v1/auth/widget/bootstrap                        permitAll, existing CORS allow-list
  body      { publicKey @NotBlank, secretKey @NotBlank, origin @NotBlank }   ← UNCHANGED (D6)
  200       { accessToken }
  401       bad credentials, inactive widget (now unconditional, D5.2), expired widget
  401       origin mismatch (WidgetOriginMismatchException, only when originValidation = true)
  409       WidgetRevokedException, if that is the path a revoked widget hits (existing behaviour, unchanged)

GET  /api/v1/widget/organization                          Bearer (widget), existing allow-list, rate-limited (read)
  200 WidgetOrganizationResponse
      { name, slug, logoUrl, bannerUrl, bio, timezone, weekStart,
        ecosystem,                                          ← "GENERAL" | "BARBERSHOP" | … (tenant.ecosystems.code)
        services: [ WidgetServiceSummary
          { slug, title, description, location, durationMinutes,
            minPrice, maxPrice, currency, autoConfirm } ] }
  401 no / expired / malformed Bearer (filter, existing) · inactive widget (WidgetNotActiveException, D5.3)
  403 a non-widget principal (ForbiddenActorException, D5.1)
  404 tenant not ACTIVE/TRIAL (OrganizationNotFoundException — same message shape as the public channel)
  429 rate limited (D5.4) + Retry-After

GET  /api/v1/widget/services/{serviceSlug}
  200 WidgetServiceResponse   — TASK-0011 §9.2 `PublicServiceResponse`, field for field
  401/403/429 as above · 404 as above · unknown service slug · service has no booking policy (ServiceNotFoundException)

GET  /api/v1/widget/services/{serviceSlug}/slots?date=YYYY-MM-DD
  200 AvailableSlotsResponse  — the TASK-0009 record, unchanged
  400 malformed date (Spring default) · 401/403/429 as above · 404 as above

POST /api/v1/widget/services/{serviceSlug}/bookings                          rate-limited (write)
  WidgetBookingRequest        — the existing full-page record, field for field, same annotations
  201 WidgetBookingResponse   { bookingUid, status, startTime, endTime, timezone, serviceTitle, organizationName }
  400 validation · 401/403/429 as above · 404 as above · 409 SlotUnavailableException

GET  /api/v1/public/{orgSlug}                             unchanged, plus `ecosystem` in the body
```

`Instant`s serialise as in `BookingResponse` (ISO-8601 `Z`); no Jackson
annotations. Numbers are JSON numbers; the library types them `number`.

### 9.3 `service/widget/barbershop` — the flow after extraction

```java
public interface BarbershopBookingService {
    WidgetOrganizationResponse getOrganization(ServedOrganization served);
    WidgetServiceResponse getService(ServedOrganization served, String serviceSlug);
    AvailableSlotsResponse getSlots(ServedOrganization served, String serviceSlug, LocalDate date);
    WidgetBookingResponse createBooking(ServedOrganization served, String serviceSlug, WidgetBookingRequest request);
}
```

`BarbershopBookingServiceImpl` fields: `ServiceRepository`,
`BookingPolicyRepository`, `AvailabilityRepository`, `UserRepository`,
`BookingRepository`, `AttendeeRepository`, `SlotService` — the pre-task
list minus `OrganizationRepository` and `TenantRepository`, which only the
resolvers need now. Bodies are the pre-task bodies with
`resolveServedOrganization(organizationSlug)` replaced by
`served.organization()`, and `getOrganization` adding
`served.ecosystemCode()` to the response. `resolveService`,
`resolvePolicy`, `toSummary`, `serviceNotFound` move unchanged;
`organizationNotFound` moves to the resolvers. The comment block on the
lock, the `AWAITING_HOST` choice and the `IN` query move with the code.

### 9.4 The two resolvers

```java
// service/widget/fullpage/SlugOrganizationResolver   (@Component)
public ServedOrganization resolve(String organizationSlug)
    organizationRepository.findBySlug → 404      // no reserved-slug check: see ReservedOrganizationSlugs
    tenantRepository.findByOrganizationOrganizationId → 404
    !ServedTenantStatuses.SERVED.contains(tenant.getStatus()) → 404
    return new ServedOrganization(organization, tenant.getEcosystem().getCode());

// service/widget/embedded/EmbeddedWidgetOrganizationResolver   (@Component)
public ServedOrganization resolve()
    WidgetPrincipal principal = currentPrincipalProvider.requireWidget();   // 401 / 403 — D5.1
    if (!principal.isActive())                                              // D5.3
        throw new WidgetNotActiveException("Widget is not active");         // 401, existing exception
    TenantEntity tenant = tenantRepository.findById(principal.tenantId())
        .orElseThrow(() -> new OrganizationNotFoundException("Organization not found for widget"));
    if (!ServedTenantStatuses.SERVED.contains(tenant.getStatus()))
        throw new OrganizationNotFoundException("Organization not found for widget");
    return new ServedOrganization(tenant.getOrganization(), tenant.getEcosystem().getCode());
```

The 404 message on the widget channel must not contain the tenant id or the
widget uid. Both resolvers carry a comment saying they are the *only* place
the served organization is decided for their channel; the embedded one also
comments that `isActive()` is checked here rather than through the
authorization engine because the engine is not invoked on this channel
(D4), with a pointer to `SECURITY.md`'s widget-channel subsection.

### 9.5 The four identity-pipeline completions (D5)

**`CurrentPrincipalProvider`** gains:

```java
WidgetPrincipal requireWidget();
```

`DefaultCurrentPrincipalProvider.requireWidget()`:

```java
@Override
public WidgetPrincipal requireWidget() {
    AuthenticatedPrincipal principal = require();   // 401 when absent — unchanged

    if (principal instanceof WidgetPrincipal widgetPrincipal) {
        return widgetPrincipal;
    }

    throw new ForbiddenActorException("Invalid actor: " + principal.actorType());
}
```

Byte-for-byte the shape of `requireUser()`, substituting `WidgetPrincipal`
for `UserPrincipal`.

**`DefaultWidgetIdentityLoader.validateOrigin`** — before:

```java
private void validateOrigin(WidgetEntity widget, String origin) {
    if (!widget.getOriginValidation()) return;
    if (widget.getStatus() != WidgetStatus.ACTIVE) throw new WidgetNotActiveException(...);
    if (widget.getExpiresAt() != null && widget.getExpiresAt().isBefore(Instant.now()))
        throw new WidgetExpiredException(...);
    // origin equality check
}
```

after — the two guards move above the early return, the origin check stays
behind it:

```java
private void validateOrigin(WidgetEntity widget, String origin) {
    if (widget.getStatus() != WidgetStatus.ACTIVE) throw new WidgetNotActiveException(...);
    if (widget.getExpiresAt() != null && widget.getExpiresAt().isBefore(Instant.now()))
        throw new WidgetExpiredException(...);
    if (!widget.getOriginValidation()) return;
    // origin equality check, unchanged
}
```

A comment at the top explains why: a widget with no registered origin was
previously exempt from the status/expiry checks entirely, so revoking it
did not stop bootstrap from succeeding.

**`WebConfig.addInterceptors`** — one line:

```java
registry.addInterceptor(publicRateLimitInterceptor)
    .addPathPatterns("/api/v1/public/**", "/api/v1/widget/**");
```

No change to `PublicRateLimitInterceptor` itself — it already buckets by
method (`POST` → write, else → read) and by `request.getRemoteAddr()`,
which applies unchanged to an authenticated widget call.

### 9.6 Widget library — `core/`

**`api/client.ts`** — `createWidgetClient({ apiUrl, getToken, refreshToken })`
returns an axios instance: `baseURL = apiUrl`, `timeout 20_000`,
`withCredentials: false`, JSON headers, `X-Correlation-Id` per request (carry
`attachCorrelationId` from `client/lib/axios.ts`), a request interceptor
setting `Authorization: Bearer ${await getToken()}`, and a response
interceptor that on **401, once per request** (`config._retried` flag) calls
`await refreshToken()` and replays. A **429** rejects to the caller with the
`Retry-After` value attached (mirroring `normalizeApiError`'s existing
handling) — it is a real, reachable status on this channel now (D5.4), not
a placeholder. Everything else rejects to the caller.

**`api/errors.ts`** — `ApiError`, `ApiRequestError`, `normalizeApiError`
ported from `client/lib/api-error.ts` with the "our own BFF already returns
the normalized shape" branch removed (there is no BFF), and the 429
`Retry-After` read kept as a live path.
`normalizeBookingError(status)` — the four copy strings from
`public-booking-errors.ts` (400/409/429/default).

**`api/widget-api.ts`** — `createWidgetApi(client): BookingApi` where

```ts
export interface BookingApi {
  getOrganization(): Promise<Organization>;
  getService(serviceSlug: string): Promise<Service>;
  getSlots(serviceSlug: string, date: string): Promise<AvailableSlots>;
  createBooking(serviceSlug: string, request: BookingRequest): Promise<Booking>;
}
```

against `/api/v1/widget/...`. The four functions wrap errors in
`ApiRequestError` exactly as `public-booking-api.ts` does. `BookingApi` is
the seam the hosted page will implement over its BFF later (D15); it is a
type in `ecosystems/barbershop/logic/types.ts`, not in `core` — the
*shape* of the API is the ecosystem's, the *transport* is core's.

**`auth/session.ts`** — `createSession({ apiUrl, publicKey, secretKey })`
returning `{ getToken, refresh, invalidate }`. `getToken()` returns the
cached token or bootstraps; `refresh()` clears and bootstraps; both share
one in-flight promise so concurrent 401s issue one bootstrap. Bootstrap is
a plain `axios.post(`${apiUrl}/api/v1/auth/widget/bootstrap`, { publicKey, secretKey, origin: window.location.origin })`
— `origin` is a required body field on the endpoint as built (D6); it is
the only `window` read in the library. A bootstrap failure rejects with the
normalised error; nothing is retried. The token is a closure variable.

**`auth/PleaseBookMeProvider.tsx`** — `"use client"`; props `apiUrl`,
`publicKey`, `secretKey`, `children` — all plain strings, never read from
`process.env` internally (D8, Boundaries). Builds session → client → api
once (`useMemo` on the three strings) and provides `{ api }` through
context. **`auth/use-pleasebookme.ts`** — `usePleaseBookMe()` returning the
context, throwing a readable error when used outside the provider (the one
error a pasting developer will hit first).

**`lib/`** — `cn.ts` (`clsx` + `tailwind-merge`, as `client/lib/utils.ts`),
`date-math.ts` **verbatim** from `client/features/public-booking/lib/date-math.ts`
including every comment, plus its test; `format.ts`, `calendar-link.ts`
verbatim.

**`ui/`** — `button.tsx`, `input.tsx`, `label.tsx` (from
`client/components/ui`, with `@/lib/utils` → `../lib/cn`),
`native-select.tsx` (from `client/components/form`), `spinner.tsx`,
`error-message.tsx` (from the port). These are the only primitives a variant
may import.

**`styles/tokens.css`** —

```css
@theme inline {
  --color-background: var(--pbm-background);  /* one line per colour the variants use: */
  --color-foreground, --color-primary, --color-primary-foreground, --color-muted-foreground,
  --color-border, --color-input, --color-ring, --color-destructive, --color-surface,
  --color-surface-container, --color-surface-hover, --color-success, --color-warning
}
@theme {
  /* copied from client/app/globals.css lines 74–132: the type scale (display …
     mono-label) and the 4 px spacing scale (xs … 2xl, gutter) */
}
.pbm-widget {
  --pbm-background: #0b0d10; …   /* the :root values from client/app/globals.css, one per mapping above */
}
```

The variables are namespaced `--pbm-*` so the host's shadcn `--primary`
is neither read nor overwritten; the mappings are `@theme inline` so a
value declared on `.pbm-widget` is what the utility resolves to (a
non-inline `@theme` fixes the value at `:root` and the scope would be
ignored — say so in a comment). The variant's root element carries
`className="pbm-widget"`. If a class from the port has no token here and
no shadcn default, the class is renamed to one that does — never add a
token for one use.

### 9.7 Widget library — `ecosystems/barbershop/`

**`logic/types.ts`** — `types/public-booking.ts` with `Public` dropped
from every name (`Organization`, `ServiceSummary`, `Service`,
`BookingRequest`, `Booking`, `BookingStatus`), `Organization.ecosystem: string`
added, plus the `BookingApi` interface above.

**`logic/steps.ts`**, **`logic/details-schema.ts`** — verbatim.

**`logic/use-available-slots.ts`** — the port with `getSlots` taken from a
`api: BookingApi` input instead of an import, and `organizationSlug`
removed from the key (the widget has one organization).

**`logic/use-booking-flow.ts`** — everything stateful in
`booking-widget.tsx` lines 33–170, as a hook:

```ts
export function useBookingFlow(api: BookingApi): {
  status: "loading" | "ready" | "unavailable";     // organization fetch: pending / done / any non-2xx
  organization: Organization | null;
  step: BookingStep; service: Service | null; visitorZone; selectedDate; selectedSlot;
  details; submitting; submitError; retryAfter; result;
  loadingServiceSlug; serviceError;
  availability: ReturnType<typeof useAvailableSlots>;
  actions: { selectService, changeZone, selectDate, selectSlot, submitDetails, confirmBooking,
             goToStep, goBack, navigateCompleted, bookAnother };
}
```

Differences from the source, all of them: the organization is fetched by
the hook on mount (`api.getOrganization()`), so `status` exists; the
`router.refresh()` on a 404 during confirm becomes `status = "unavailable"`;
`window.scrollTo(0, 0)` on step change moves into the variant (a DOM
concern); everything else — the step-back rule, the 409 → refresh slots →
back to `time`, the 429 countdown (now live, D5.4), the 400 → back to
`details` — is carried line for line.

**`variants/kinetic/`** — every file of
`client/features/public-booking/components/` except `booking-widget.tsx`,
which becomes `BarbershopBookingWidget.tsx`: `usePleaseBookMe().api` →
`useBookingFlow(api)` → the same JSX, reading state from the hook. Three
states before the flow: `loading` (spinner), `unavailable` (the
`not-found.tsx` copy: "This booking page isn't available." / "Check the
link you were given, or contact the business directly."), `ready`. Imports
rewritten: `@/features/public-booking/lib/*` → `../../../../core/lib/*`,
`@/lib/utils` → `../../../../core/lib/cn`, `@/components/*` →
`../../../../core/ui/*`, types → `../../logic/types`. `next/navigation`
removed. `booking-success.tsx`'s "Book another" calls
`actions.bookAnother()`; its "Return to website" uses
`service.successRedirectUrl` via a plain `<a>` as today.

**`variants/kinetic/theme.css`** — empty except a comment: the variant
uses `tokens.css` as-is; a second variant overrides `--pbm-*` here.

**`variants/kinetic/README.md`** — the paste steps (§9.8) specialised to
this variant's import line.

**`ecosystems/registry.ts`** —

```ts
import { BarbershopBookingWidget } from "./barbershop/variants/kinetic/BarbershopBookingWidget";
export const ECOSYSTEM_WIDGETS: Record<string, React.ComponentType> = {
  BARBERSHOP: BarbershopBookingWidget,
  GENERAL: BarbershopBookingWidget,   // appointment-centric as well
};
export function widgetForEcosystem(code: string): React.ComponentType | null
```

The harness uses it; a pasted site imports a variant directly and never
needs it. It exists so the iframe stage has its switch ready.

### 9.8 Distribution — `pleasebookme/README.md`

Exactly this sequence, with the peer-dependency list from §6 #9, the env
names, and D8's key-storage step spelled out (this is the step the author
called out specifically — do not compress it):

1. In the PleaseBookMe dashboard, create a widget for the client's
   organization. Copy the `publicKey`/`secretKey` shown — **this is the
   only time the secret is shown**; if lost, rotate the widget.
2. In the client's own site repo, create or open `.env.local` (never
   committed) and add:
   ```
   NEXT_PUBLIC_PBM_API_URL=https://api.pleasebookme.app
   NEXT_PUBLIC_PBM_PUBLIC_KEY=<publicKey>
   NEXT_PUBLIC_PBM_SECRET_KEY=<secretKey>
   ```
   This `.env.local` is the **only** place the key pair lives outside the
   dashboard and the server's own hashed copy (D8) — there is no PleaseBookMe
   vault, no second dashboard download, no server-side plaintext copy.
3. Copy `pleasebookme/core/` and `pleasebookme/ecosystems/barbershop/`
   into the site (any path; the folder is self-contained).
4. Install the peers.
5. `@import "<path>/core/styles/tokens.css";` in the site's Tailwind entry
   CSS; if the folder is outside Tailwind's auto-detected sources, add
   `@source "<path>";`.
6. In a client component or page, read the three env vars and pass them as
   props — Next.js already inlines `NEXT_PUBLIC_*` at build time, so this
   is the same pattern as any other public key in a Next.js app:
   ```tsx
   <PleaseBookMeProvider
     apiUrl={process.env.NEXT_PUBLIC_PBM_API_URL!}
     publicKey={process.env.NEXT_PUBLIC_PBM_PUBLIC_KEY!}
     secretKey={process.env.NEXT_PUBLIC_PBM_SECRET_KEY!}
   >
     <BarbershopBookingWidget />
   </PleaseBookMeProvider>
   ```
7. In the dashboard, add the client site's origin to the CORS allow-list
   (an ops step, not a client one — see `SERVER_AGENTS.md`) and, if the
   widget was created with a registered origin, confirm it equals the
   site's origin (`https://www.barbershop.com` — scheme, host, non-default
   port; no path).

Then the two rules (relative imports; tokens) and the roadmap paragraph
(npm package: a `package.json` with an `exports` entry per variant; iframe:
`app/embed/…` in this harness rendering `widgetForEcosystem`).

### 9.9 What the documentation must still say about the remaining gaps

`SECURITY.md` is the repository's record of security posture. After D5's
four completions land, the list is shorter than the previous draft of this
task assumed, and it is written down in the same voice as the existing
"Known gaps" list:

- bootstrap's `origin` is a body field, not the browser `Origin` header, so
  a non-browser caller supplies its own value — the existing prose
  describing a header check is aspirational (D6, carried from before);
- no origin is re-verified after bootstrap — a token, once issued, is not
  bound to the origin it was bootstrapped from;
- the authorization engine's capability model
  (`WidgetCapabilityPolicy`'s slug allow-list) is not consulted on this
  channel — the four endpoints are the entire capability surface today by
  construction, not by policy;
- both items are deferred to the cross-domain authorization policy plan by
  explicit decision, with this task named.

What is **no longer** a gap, and should be removed from any earlier note
that listed it as one: a revoked widget's token no longer keeps working
until expiry (D5.2/D5.3 close this); the channel is rate limited (D5.4);
a non-widget principal cannot reach the channel (D5.1).

---

## 10. NON-FUNCTIONAL REQUIREMENTS

### Must not exist after this task

1. A diff under `server/src/main/java/com/pleasebookme/server/security/authorization`.
2. `service/widget/fullpage/service/impl/FullPageWidgetServiceImpl` with any
   repository field, any `@Transactional` write, or any reference to
   `SlotService`.
3. Any class named `Public*` anywhere under `service/widget/` — the name is retired platform-wide.
4. Any class introduced here whose simple name exists under
   `server/.../widget/` (the bounded context).
5. `@PreAuthorize`, `AuthorizationService`, `ScopeResolver`, or any read of
   `WidgetCapabilityPolicy`/`WidgetTenantIsolationPolicy` under
   `service/widget/`.
6. A new `requestMatchers(...)` entry in `SecurityConfig`, a new CORS
   registration, or a change to `RateLimitProperties`/`application.yaml`.
7. `requireWidget()` doing anything beyond type-narrowing (no
   `isActive()` check inside it — that stays in the resolver).
8. `localStorage`, `sessionStorage`, `document.cookie`, `next/navigation`,
   `next/link`, `@/`, or `process.env` inside `PleaseBookMeProvider` or
   any other file under `widget/pleasebookme/`.
9. A hex colour literal under `widget/pleasebookme/` outside `tokens.css`.
10. A token definition in `widget/app/globals.css` that `tokens.css` also
    defines.
11. A second copy of `date-math.ts` that differs from the client's by more
    than the import lines (`diff` must be empty after stripping imports).
12. A refresh token, a token in a URL, a bootstrap that runs on the server
    side of a Next.js site, or any server-side/dashboard storage of the key
    pair beyond the existing `secret_key` hash (D8).

### Behavioural

13. The public channel answers exactly as before this task for every
    request in TASK-0011's M-series, plus `ecosystem` in `GET /{orgSlug}`.
14. A widget token issued before this task keeps working against the new
    endpoints (no claim change) — until that widget is revoked, at which
    point D5.3 refuses it on the very next request rather than at expiry.
15. Bootstrap accepts the same inputs and returns the same outputs as
    before this task; only the internal check order inside
    `validateOrigin` changes (D5.2), and only for a widget whose status is
    not `ACTIVE` or whose token has expired.
16. `/api/v1/widget/**` is rate limited on the same buckets as
    `/api/v1/public/**`; a 429 there carries `Retry-After` exactly as the
    public channel's does.
17. The library builds in the harness with zero warnings from Next about
    server/client boundaries (`"use client"` on the provider, the hooks'
    consumers and the variant root).

---

## 11. ACCEPTANCE CRITERIA

### Database

1. `flyway_schema_history` shows `144`, `success = true`;
   `tenant.ecosystems` has `BARBERSHOP`; booting twice is idempotent.

### Server — extraction

2. `BarbershopBookingServiceImplTest` passes with the pre-task assertions
   (lock before engine via `InOrder`; nothing written on a missing slot;
   `AWAITING_HOST` when `autoConfirm = false`; one `IN` query for policies;
   schedule zone for the date).
3. `SlugOrganizationResolverTest`: unknown slug → `OrganizationNotFoundException`;
   suspended tenant → `OrganizationNotFoundException`; the record carries
   the ecosystem code.
4. `FullPageWidgetServiceImpl` compiles to resolve + delegate; the full-page
   controller's four handlers are byte-identical apart from imports.

### Server — the four D5 completions

5. `DefaultCurrentPrincipalProviderTest` covers `requireWidget()`'s three
   outcomes (widget passes, user throws 403, none throws 401).
6. `DefaultWidgetIdentityLoaderTest` proves a revoked or expired widget
   with `originValidation = false` is now refused where it previously
   succeeded.
7. `EmbeddedWidgetOrganizationResolverTest` covers all five outcomes in
   §3 #18 and asserts `tenantRepository.findById` is called with the
   principal's `tenantId` and nothing else is read from the request.
8. M-series (below) shows a widget token stops working immediately after
   its widget is revoked in the dashboard (M9), and that the 121st read
   within a minute from one address is 429 with `Retry-After` (M11).

### Server — widget channel

9. M3–M6 return the JSON shapes in §9.2 for the demo widget; M7 (a user
   JWT) is 403; M8 (no token) is 401.
10. M10 (booking POST) returns 201 and a `core.bookings` row with
    `user_id = host`, `service_id`, `status` per the policy, plus one
    `core.attendees` row; repeating it returns 409.
11. `git diff --stat -- server/src/main/java/com/pleasebookme/server/security/authorization`
    is empty (D4).

### Widget — library and harness

12. `npm run lint`, `npm run build`, `npm test` green in `widget/`;
    `date-math.test.ts` runs there unchanged.
13. `grep -rn "@/\|next/\|process\.env" widget/pleasebookme/core/auth/PleaseBookMeProvider.tsx`
    is empty; `grep -rn "@/\|next/" widget/pleasebookme` (elsewhere) is
    empty; `grep -rnE "#[0-9a-fA-F]{6}" widget/pleasebookme --include='*.ts' --include='*.tsx'` is empty.
14. `diff <(sed '/^import/d' client/features/public-booking/lib/date-math.ts) <(sed '/^import/d' widget/pleasebookme/core/lib/date-math.ts)` is empty.
15. V1–V7: the harness at `http://localhost:3002/barbershop/kinetic` shows
    the six steps with the hosted page's look, and the "missing env" notice
    when `.env.local` is absent. Desktop + one mobile width. Look only (D14).

### Documentation

16. #35–#39 exist; `SERVER_AGENTS.md` says v144; `SECURITY.md` reflects the
    shorter, current gap list (§9.9); `widget/AGENTS.md` no longer says
    "Create Next App" and states the D8 key-storage rule plainly.

---

## 12. VALIDATION

### Automated (required — paste output)

```text
cd server && ./gradlew test
cd widget && npm run lint && npm run build && npm test
cd client && npm run build            # unchanged, must still pass
```

### Manual — server (curl; record commands and output)

Preconditions: server up, demo org `<org>` with `consultant-meeting`, demo
widget `ACTIVE` with no registered origin, `PK`/`SK` its keys; a second
widget for M9.

- M1 `psql`: `SELECT version, success FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 1` → 144, t.
- M2 `curl -X POST :8080/api/v1/auth/widget/bootstrap -H 'Content-Type: application/json' -d '{"publicKey":"PK","secretKey":"SK","origin":"http://localhost:3002"}'` → 200, save `T`.
- M3 `curl :8080/api/v1/widget/organization -H "Authorization: Bearer $T"` → 200, `ecosystem` present, no tenant/organization/user id anywhere in the body.
- M4 `…/widget/services/consultant-meeting` → 200.
- M5 `…/widget/services/consultant-meeting/slots?date=<next weekday>` → 200, non-empty.
- M6 `…/widget/services/nope/slots?date=…` → 404.
- M7 M3 with a **user** access token → 403.
- M8 M3 with no `Authorization` header → 401.
- M9 bootstrap the second widget, then revoke it in the dashboard; M3
  against `/api/v1/widget/organization` with its still-unexpired token →
  401 (`WidgetNotActiveException`). Then re-bootstrap it → also refused,
  same status. Restore or discard that widget afterward.
- M10 `POST …/widget/services/consultant-meeting/bookings` with a
  `slotStart` from M5 → 201; check both rows in `psql`; repeat → 409.
- M11 `for i in $(seq 1 121); do curl -s -o /dev/null -w '%{http_code}\n' :8080/api/v1/widget/organization -H "Authorization: Bearer $T"; done | sort | uniq -c` → 120×200, 1×429 with `Retry-After`.
- M12 `GET /api/v1/public/<org>` → unchanged shape + `ecosystem`.

### Visual walkthrough — agents (`run` the harness against the same server, then `claude-in-chrome`)

- V1 `/` — the catalog renders one card.
- V2 `/barbershop/kinetic` with no `.env.local` — the notice, nothing else.
- V3–V7 with `.env.local` — step 1 loads the demo org's services; click
  through Service → Date → Time → Details → Review **looking only**: each
  step renders with the tokens (dark surface, precision blue primary,
  Geist), nothing unstyled, nothing overflowing at 375 px. Do not submit
  the Review step; do not exercise error paths.

### Functional browser testing — **author only** (agents and reviewers must not run this)

- B1 Full flow to Done on the harness; booking visible in the dashboard.
- B2 Paste `pleasebookme/core` + `ecosystems/barbershop` into a throwaway
  `create-next-app` on port 3003, put the keys in *that* project's own
  `.env.local` (D8), add `http://localhost:3003` to
  `app.cors.allowed-origins`, restart the server, complete a booking from
  there. This is the acceptance test of D1, D7 and D8 and only the author
  runs it.
- B3 Leave the harness idle > 15 min, then pick a date → slots load
  (silent re-bootstrap).
- B4 Two tabs, same slot, confirm both → one 201, one 409-and-back-to-time.
- B5 Visitor zone ≠ Sydney, a date whose slots span two schedule dates.

---

## 13. ESCALATION

Stop and report to the orchestrator when:

1. The baseline is not green, or `widget/` does not build as scaffolded.
2. Moving the flow requires changing *any* assertion in the eight
   pre-task test cases beyond the parameter type — behaviour drift.
3. `ServerApplicationTests` fails after the move with a bean-name or
   simple-name collision you cannot resolve within §6 #3's list.
4. **Anything appears to require a change under `security/authorization`**
   (D4) — a missing scope resolver, a permission check the resolver
   "should" go through. Report what you observed; do not add it.
5. `requireWidget()` cannot be written as a straight mirror of
   `requireUser()` — for example if `WidgetPrincipal` is not directly
   reachable from `AuthenticatedPrincipal` the way `UserPrincipal` is.
6. Reordering `validateOrigin`'s checks changes an existing test's
   expected exception type or status for a case unrelated to
   `originValidation = false` — that is a sign the two checks are not
   independent of the origin guard the way D5.2 assumes.
7. The harness is refused by CORS from `http://localhost:3002` — the
   allow-list already contains it, so something else is wrong. Do not open
   CORS to `*` (D7).
8. `useBookingFlow` cannot carry the 409/429/400 branches without
   `next/navigation` — report which branch; do not import it.
9. You need any dependency beyond §6 #9 in the library, or any server
   dependency at all.
10. A class in the port uses a token that has no `tokens.css` entry and no
    shadcn default and you cannot substitute one without visible change —
    record and continue (D14); escalate only if a step becomes unusable.
11. A test green at baseline fails and the fix would be to edit that test.
12. You find yourself wanting to add a tenant/organization/widget id to any
    widget-channel request, read the principal inside the flow, add a
    `@PreAuthorize`, read `WidgetCapabilityPolicy`, open CORS, store the key
    pair anywhere but the site's own env, or read `Origin` from the
    bootstrap header instead of the body. All of it is deferred by
    decision; raise it, do not build it.
13. `date-math.ts` needs any edit to compile under the widget's tsconfig —
    align the tsconfig, not the file.
14. You are asked to run a B-series scenario or "make sure a paste works
    end to end". Decline and point here (D14).

Do not silently resolve architectural or security ambiguity. Design
ambiguity you *do* resolve yourself, toward the simpler option, and record.

---

## 14. IMPLEMENTATION PLAN

### Phase A — ground truth (Steps 1–3)

1. Clean tree; baseline (§7) in all three projects; record outputs.
2. Create the demo widget through the dashboard (`TASK-0008` flow): name
   "Harness", no origin, copy the credentials into `widget/.env.local`
   (gitignored — confirm with `git check-ignore`). Create a second widget
   for M9/B-series revocation checks.
3. Read §8's server list; write down the eight test names in
   `FullPageWidgetServiceImplTest` and which two are resolver tests
   (`getOrganization_formerlyReservedSlugIsJustAnUnknownOrganization`,
   `createBooking_tenantFailureStopsBeforeLock`).

### Phase B — the seed (Step 4)

4. `V144` + `V227`; boot; M1. Nothing else in this phase — the seed is
   independent of every other step and proves the boot before code moves.

### Phase C — extraction (Steps 5–10)

5. Create `service/widget/ServedOrganization` and `ServedTenantStatuses`.
6. Move `service/widget/fullpage/dto/*` → `service/widget/barbershop/dto/*`
   with the renames (IDE move + rename; let it rewrite imports). Move
   `SlotUnavailableException`. Compile.
7. Create `BarbershopBookingService` and `BarbershopBookingServiceImpl`:
   copy `FullPageWidgetServiceImpl`, delete `resolveServedOrganization`,
   `SERVED_TENANT_STATUSES`, the two repository fields, and
   `organizationNotFound`; change the first parameter of the four methods;
   add `served.ecosystemCode()` to `getOrganization`'s response. Keep the
   comments.
8. Create `SlugOrganizationResolver` from the deleted code. Rewrite
   `FullPageWidgetServiceImpl` to inject the resolver and the flow and
   delegate. Public controller: imports only. Compile; boot; smoke
   `GET /api/v1/public/<org>` — nothing changed on the wire except
   `ecosystem`.
9. Split the tests: six cases → `BarbershopBookingServiceImplTest`
   (construct a `ServedOrganization` in `setUp`); two cases →
   `SlugOrganizationResolverTest` plus the three new resolver cases.
   `./gradlew test` green.
10. Update `GlobalExceptionHandler`'s import for `SlotUnavailableException`
    (Step 6 likely did it); confirm `ServerApplicationTests`.

### Phase D — the four D5 completions (Steps 11–14)

11. `CurrentPrincipalProvider.requireWidget()` + impl (§9.5). Test first
    (§3 #19).
12. `DefaultWidgetIdentityLoader.validateOrigin` reorder (§9.5). Test first
    (§3 #20) — write the failing case (revoked widget, validation off,
    still succeeds today) before the fix, confirm it fails, then fix.
13. `WebConfig.addInterceptors`: add `"/api/v1/widget/**"` (§9.5).
14. `EmbeddedWidgetOrganizationResolver` (§9.4), using both of the above.
    Test first (§3 #18): a `WidgetPrincipal` with `status = ACTIVE` and
    `tenantId = 7` must lead to `tenantRepository.findById(7)`; a
    `WidgetPrincipal` with `status = REVOKED` must throw
    `WidgetNotActiveException` before any repository call; a
    `UserPrincipal` must never reach the repository.

### Phase E — the embedded channel (Steps 15–17)

15. `EmbeddedWidgetService` + impl: four one-liners (`resolve()` → flow).
    Transactions per §6 #4.
16. `EmbeddedWidgetController` at `/api/v1/widget`: copy the public
    controller, drop the `organizationSlug` path variable, name the bean
    explicitly (`@RestController("embeddedWidgetController")`; the public
    one is `publicBookingApiController`).
17. Boot; M2–M12. Record every status. M9 needs a real revoke through the
    dashboard mid-sequence — do it, don't simulate it.

### Phase F — library core (Steps 18–23)

18. `widget/package.json`: add `@hookform/resolvers`, `vitest`, scripts
    `"test": "vitest run"`, `"dev": "next dev -p 3002"`. `.env.example`.
19. `pleasebookme/core/lib/`: copy `cn`, `date-math.ts` + test, `format.ts`,
    `calendar-link.ts`. Fix only import paths. `npm test` — the date-math
    suite runs in the new home.
20. `core/api/errors.ts` from `client/lib/api-error.ts` (§9.6), 429 path
    kept live.
21. `core/auth/session.ts` (§9.6) with a small vitest: two concurrent
    `getToken()` calls → one bootstrap; `refresh()` after a 401 → a new
    token; a failed bootstrap rejects both waiters.
22. `core/api/client.ts` and `core/api/widget-api.ts`.
23. `core/ui/*` and `core/styles/tokens.css` (§9.6). Harness
    `globals.css`: add the import; remove nothing shadcn, add nothing else.

### Phase G — barbershop logic (Steps 24–26)

24. `logic/types.ts`, `steps.ts`, `details-schema.ts`.
25. `logic/use-available-slots.ts` (api injected).
26. `logic/use-booking-flow.ts` (§9.7). Keep the source's comments on the
    step-back rule and the 409/429 paths. `npm run lint`.

### Phase H — the `kinetic` variant (Steps 27–30)

27. Copy `client/features/public-booking/components/*` except
    `booking-widget.tsx` into `variants/kinetic/components/`; rewrite
    imports per §9.7; `"use client"` where the source has it.
28. `BarbershopBookingWidget.tsx` from `booking-widget.tsx`: provider hook →
    flow hook → JSX; the three pre-flow states; `pbm-widget` on the root;
    the scroll-to-top effect.
29. `theme.css`, `README.md` (§9.8, in full — the key-storage steps
    especially), `ecosystems/registry.ts`.
30. `npm run lint && npm run build`. Then the greps in §11 #13–14.

### Phase I — harness (Steps 31–32)

31. `app/page.tsx`, `app/barbershop/kinetic/page.tsx` (reads `.env.local`,
    the reference implementation of D8's Step 6), `app/layout.tsx` title.
32. `run` the harness; V1–V7. Screenshots into the report.

### Phase J — documentation and report (Steps 33–35)

33. `SERVER_AGENTS.md`, `SECURITY.md` (§9.9 — write the *shorter* gap list,
    don't just append), `widget/AGENTS.md`, `pleasebookme/README.md`.
34. Obsidian (#38) and ADR-0002 (#39).
35. Closing report (#40), including the empty `security/authorization`
    diff.

---

## 15. REVIEW STRATEGY

### What to review

1. **The authorization-engine diff is empty** — run
   `git diff --stat -- server/src/main/java/com/pleasebookme/server/security/authorization`
   first. Non-empty is blocking (D4).
2. **The four completions are mirrors, not new mechanisms** —
   `requireWidget()` next to `requireUser()`, line by line; the
   `validateOrigin` diff touches only check *order*, not check *content*;
   the `WebConfig` diff is one line.
3. **Tenant scoping by construction** — read
   `EmbeddedWidgetOrganizationResolver` top to bottom; the only input is
   the principal. Then grep `service/widget/embedded` for `@PathVariable`,
   `@RequestParam`, `@RequestBody`: the controller has one path variable
   (`serviceSlug`), one query param (`date`), one body
   (`WidgetBookingRequest`) — nothing that names a tenant.
4. **The move is a move** — `git diff --no-index` the pre-task
   `FullPageWidgetServiceImpl` against `BarbershopBookingServiceImpl` (use
   `git show <base>:server/.../FullPageWidgetServiceImpl.java`). Expected
   hunks: imports, class/field header, four signatures, one
   `ecosystemCode` line, the deleted resolver. Any hunk inside
   `createBooking`'s lock → engine → save sequence is blocking.
5. **The revocation and rate-limit tests are real** — M9 and M11 in the
   closing report must show an actual dashboard revoke and an actual
   121-request loop, not a described expectation.
6. **The deferred list is recorded and shorter than before** — `SECURITY.md`
   carries §9.9's shorter list; nothing on it duplicates something D5
   actually closed.
7. **Library portability** — the greps in §11 #13–14; every import under
   `pleasebookme/` starts with `.`; `PleaseBookMeProvider` has no
   `process.env` read; `tokens.css` colour mappings are `@theme inline`;
   harness `globals.css` adds only the import.
8. **Session** — token is a closure variable; one in-flight promise; the
   401 interceptor retries once (`_retried`); no storage APIs;
   `window.location.origin` appears only in `session.ts`.
9. **Hook carries the behaviour** — compare `use-booking-flow.ts` to
   `booking-widget.tsx` lines 33–170: same transitions, same error
   branches, `router.refresh()` → `unavailable`, nothing else changed.
10. **The README's D8 steps are complete and in order** — a reader who has
    never seen this codebase should be able to follow steps 1–7 and end
    with a working, CORS-permitted, key-holding site.

### How to review

- Server first, in Phase order; the extraction must be clean before the
  channel is judged.
- Run `./gradlew test` and the three widget commands yourself; do not
  accept pasted output alone for #9 of §11.
- Do not run B-series scenarios; do not "just try" a paste (D14). The
  author does.
- **Do not file findings for the two items still on the D4/D6/§9.9 deferred
  list** (the authorization engine's capability model; bootstrap's
  body-vs-header origin). They are decisions with a named owner. Do file
  findings if either the empty `security/authorization` diff or any of the
  four D5 completions is missing, wrong, or more than a mirror.
- Findings as `.agents/reviews/TASK-0012-widget-component-review.md`
  with reviewer role, scope, findings, severity, evidence, recommendation.

### Core components to review

`EmbeddedWidgetOrganizationResolver`, `SlugOrganizationResolver`,
`BarbershopBookingServiceImpl`, `EmbeddedWidgetController`,
`DefaultCurrentPrincipalProvider.requireWidget`,
`DefaultWidgetIdentityLoader.validateOrigin`, `WebConfig`,
`core/auth/session.ts`, `core/auth/PleaseBookMeProvider.tsx`,
`core/api/client.ts`, `core/styles/tokens.css`, `logic/use-booking-flow.ts`,
`variants/kinetic/BarbershopBookingWidget.tsx`, `pleasebookme/README.md`,
and the `SECURITY.md` diff.

---

## 16. NOT IN THIS TASK — recorded so it is not rediscovered

| Item | Why not here | Where it goes |
|---|---|---|
| **`@PreAuthorize` / the authorization engine on widget endpoints** — note for then: `AuthorizationPermissionEvaluator.decide` passes `ResourceScope.unscoped()`, so `WidgetTenantIsolationPolicy` denies every widget call today; wiring the engine needs an evaluator change first. `WidgetCapabilityPolicy.ALLOWED_SLUGS` is the intended capability set | D4 | Cross-domain authorization plan |
| **Origin in the request header at bootstrap** (today it is a body field, so a non-browser caller picks its own) and **origin binding after bootstrap** (e.g. an origin claim in the widget JWT) | D6 — `SECURITY.md` describes the header check; the code reads the body. Recorded as a known gap, unchanged by this task | Token/identity hardening task |
| **Open or dynamic CORS** (`allowedOriginPatterns("*")`, or reflecting `widget_origins`) | D7 — manual onboarding adds each client's origin to the allow-list, which is both simpler and tighter | Only if self-serve onboarding arrives |
| **A secrets vault / server-side plaintext copy of the key pair / a "re-download my keys" dashboard feature** | D8 — the agency's own `.env.local` is the store; losing the secret means rotating the widget | Only if self-serve onboarding needs it |
| Captcha, idempotency on the widget POST | D13 / TASK-0011 §16 | As listed there |
| Hosted page consuming the library (`client/features/public-booking` → `pleasebookme/` + a `BookingApi` over the BFF) | D15; needs a cross-project import or a workspace package — a build-system decision. The client feature folder keeps its `public-booking` name until then, deliberately | After this task |
| Loader script + iframe host (`app/embed/…`), postMessage bridge | D1; the business pastes source today. Note for then: the loader must bootstrap from the host page so the origin is authentic — an iframe-side bootstrap carries the iframe's origin for every tenant | Iframe task |
| npm package (`@pleasebookme/widgets`), `exports` per variant | D1; the folder is already shaped for it | Packaging task |
| Widget refresh token / longer widget access TTL | D9; re-bootstrap is free | — |
| Per-widget (rather than per-IP) rate limiting | D5.4 reuses the existing per-IP bucket; a per-key bucket needs the widget resolved before the interceptor runs | After the authorization plan |
| Provisioning default ecosystem / ecosystem choice at signup / dashboard ecosystem switch | Onboarding decision; `GENERAL` stays | Onboarding task |
| "Download widget" / paste instructions in the dashboard | The README is the delivery while the business does the pasting | Dashboard task |
| Second barbershop variant; hotel ecosystem (`logic/` for stays) | One variant proves the mechanism | Per-ecosystem tasks |
| Holds, capacity, notifications, customer upsert, month availability | TASK-0011 §16 verbatim | As listed there |
| Vite/Shadow-DOM bundle instead of Next components | Rejected: the business's sites are Next.js, and the iframe stage reuses this harness | ADR-0002 |

---

## 17. CLOSING REPORT

Status: **reviewed 2026-09-22**, `.agents/reviews/TASK-0012-widget-component-review.md` exists.

The implementing agent completed Phases A–I and crashed before Phase J; the
review wrote the missing documentation, fixed a lint warning, and recorded two
findings the author still owns (the `open-in-view` dependency in both
resolvers, and V144 being a no-op because V100 already seeded `BARBERSHOP` —
a defect in **this contract**, not in the implementation). The M-series,
V-series and B-series were not executed and remain the author's.

Move this file to `.agents/tasks/completed/` once the author's functional pass
is done.
