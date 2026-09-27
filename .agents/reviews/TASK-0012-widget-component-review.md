# TASK-0012 — Embedded widget component: review

Reviewer role: orchestrator (post-implementation review; the implementing agent crashed during Phase J)  
Date: 2026-09-22  
Verdict: **Accepted with fixes applied.** No blocking findings. Implementation (Phases A–I) is faithful to the contract; documentation (Phase J) was missing entirely and has been written as part of this review.

## Scope reviewed

- `server/src/main/java/com/pleasebookme/server/service/widget/**` (all three subpackages)
- `security/identity/context/*`, `security/identity/loader/widget/*`, `security/config/{SecurityConfig,WebConfig}`
- `server/src/test/.../service/widget/**`, `.../security/identity/**`
- `widget/pleasebookme/**`, `widget/app/**`, `widget/package.json`
- `db/migration/tenant/V144`, `database/init/tenant/V227`

## Verification run

| Check | Result |
|---|---|
| `./gradlew test` | **266 tests, 0 failed** (was 258 pre-task; +8 new) |
| `npm run lint` (widget) | **0 problems** (1 warning found and fixed — see F1) |
| `npm test` (widget) | 3 files, **14 tests passed** |
| `npm run build` (widget) | compiled; routes `/`, `/barbershop/kinetic` |
| `npm run build` (client) | compiled — the hosted page is unaffected by the extraction |
| `git diff --stat security/authorization` | **empty** ✓ (D4) |
| `grep "from \"@/\|from \"next/" pleasebookme` | empty ✓ |
| `grep "process.env" pleasebookme` | empty ✓ (D8) |
| `grep -E "#[0-9a-fA-F]{6}" pleasebookme --include=*.tsx` | empty ✓ |
| `grep "localStorage\|sessionStorage\|document.cookie" pleasebookme` | empty ✓ (D9) |

Not run: the M-series (needs a live server and a dashboard-created widget) and the V-series browser walkthrough. Both remain for the author. In place of the visual check I verified the token pipeline objectively — see "Styling" below.

## What is correct

**Tenant scoping by construction (D2).** `EmbeddedWidgetOrganizationResolver` takes its tenant only from `WidgetPrincipal.tenantId()`. The controller's entire input surface is one path variable (`serviceSlug`), one query param (`date`) and one body (`WidgetBookingRequest`) — nothing that could name a tenant. Cross-tenant access is not a check that could be forgotten.

**The authorization engine is untouched (D4).** No diff under `security/authorization`, no `@PreAuthorize`, no `AuthorizationService`, no `ScopeResolver` anywhere under `service/widget`.

**The four completions are mirrors, not new mechanisms (D5).** `requireWidget()` is `requireUser()` with the type substituted, same exceptions. `validateOrigin`'s diff moves two guards above the early return and changes no check's content. `WebConfig` adds one path pattern. The resolver's `isActive()` call is three lines with a comment naming what it substitutes for.

**No new `SecurityConfig` matcher.** `/api/v1/widget/**` is caught by the existing `anyRequest().authenticated()`, exactly as §9.5 required. Verified by reading the matcher list.

**The move is a move (§15 #4).** `BarbershopBookingServiceImpl` keeps the lock (line 171) before slot generation (181), the exact-`TimeSlot` check (187), `AWAITING_HOST` vs `ACCEPTED`, the policy `IN` query, and the carried comments. The only additions are the `ServedOrganization` parameter and `served.ecosystemCode()` in one response. The booking-title fix (`" with "`) survived the move.

**Transactions (§6 #4).** Both channel services annotate their three reads `@Transactional(readOnly = true)` and leave `createBooking` unannotated, so the resolver runs before the flow's write transaction and the host lock is taken inside it. See F3 for the consequence.

**Session and transport (D9).** Token is a closure variable; one in-flight bootstrap shared by concurrent callers; 401 replay guarded by `_retried`; no storage API. The client goes further than the contract asked and avoids a second bootstrap when another request already refreshed the token — a real race, handled well.

**Tests.** All five test classes required by §3 #16–#20 exist, and `EmbeddedWidgetOrganizationResolverTest` asserts the user-principal case never reaches the repository, which is the assertion that matters.

**Styling (D12).** `tokens.css` uses `@theme inline` for colours with `--pbm-*` namespacing and a fallback to the host's own variable outside `.pbm-widget`, so the widget themes its subtree without touching the host's `--primary`. Cross-checked every colour utility used in the library (14 names) against the tokens declared: all resolve. Then verified against the built CSS that `bg-surface`, `surface-hover`, `text-headline-sm`, `text-body-md`, `.p-lg`, `.gap-xs`, `text-warning`, `.pbm-widget` and `--pbm-background` are all emitted — objective proof the pipeline works, which a screenshot could not give without a backend.

## Findings

### F1 — `use-available-slots.ts` tripped `react-hooks/exhaustive-deps` · Low · **Fixed**

The hook read its props off an `input` object, so the rule demanded the whole object as a dependency. Not a live defect — `useBookingFlow` passes a `useCallback`-stable `onError` and the provider memoises `api`, so nothing refetched in a loop — but a caller passing an object literal with an inline `onError` would have refetched on every render, and the contract requires a clean lint.

Fixed by destructuring the props in the signature and depending on the individual values, with a comment recording why the object form is a trap. Lint is now 0 problems.

### F2 — V144 is a no-op; `BARBERSHOP` was already seeded by V100 · Low · **Documented, not reverted**

`V100__seed_ecosystems.sql` already inserts `BARBERSHOP`. V144's `ON CONFLICT (code) DO NOTHING` therefore swallows the insert, and the row keeps V100's name `Barbershops` rather than V144's `Barbershop`. Confirmed against the live database: V100, V140 and V144 all applied; `tenant.ecosystems` holds `BARBERSHOP | Barbershops` and `GENERAL | General`.

**This is a contract defect, not an implementation defect** — TASK-0012 §3 #15 and §9.1 specified the seed, and §8's domain facts named only V140. The agent followed instructions.

Not reverted: V144 is already applied, and deleting a migration below the high-water mark makes Flyway's `validate` fail at boot with "Detected applied migration not resolved locally" — the trap `SERVER_AGENTS.md` already documents. The Flyway copy must also stay byte-identical because its checksum is recorded. The init mirror (`V227`) now carries a full explanation, and `Table Ecosystems.md` records the no-op.

Left for the author: if the `BARBERSHOP` row should carry the fuller description, that is a new migration doing an `UPDATE`, not an edit to V144.

### F3 — Both resolvers depend on `open-in-view` · Medium (latent) · **Documented, not changed**

`TenantEntity.organization` and `.ecosystem` are both `FetchType.LAZY`. Both resolvers read `tenant.getEcosystem().getCode()` and hand `tenant.getOrganization()` forward. On the `createBooking` path the channel service is deliberately not `@Transactional`, so the resolver runs with no transaction of its own and relies on Spring's `open-in-view` default keeping a session open for the request.

It works today and the tests cannot catch it, because they mock the repository. Setting `spring.jpa.open-in-view: false` — which Spring recommends in a log line on every boot — would break `createBooking` on **both** channels with `LazyInitializationException`.

Not changed, for two reasons: it is not a present defect, and the obvious one-line fix is wrong. Annotating the resolver `@Transactional(readOnly = true)` would not help, because the organization proxy would still be uninitialised and detached when the flow reads its fields. The real fix is to initialise the organization inside the resolver — a fetch-join finder on `TenantRepository`, or resolving the organization through `OrganizationRepository` — which is a design choice the contract did not make. Recorded in `SERVER_AGENTS.md` → "Widget channel" and in `Embedded Widget Service.md`.

### F4 — Phase J (documentation) entirely missing · High · **Fixed**

The agent crashed before the documentation phase; every deliverable from §3 #30–#34 and #37–#39 was absent. Written during this review:

- `widget/pleasebookme/README.md` — the seven-step distribution guide, with D8's key-storage steps spelled out rather than compressed
- `widget/pleasebookme/ecosystems/barbershop/variants/kinetic/README.md`
- `widget/AGENTS.md` — replaced the `create-next-app` stub with the library/harness split, the two portability rules and their greps, and the provenance note
- `SERVER_AGENTS.md` — new `# Widget channel` section; schema version to v144
- `SECURITY.md` — new "Embedded widget channel" subsection with the deferred list from D4/D6
- `.agents/decisions/ADR-0002-widget-library-is-copied-source.md`
- Obsidian — `API/Widget/Embedded/` (summary + four endpoint pages), `Services/Widget/Barbershop Booking Service.md`, `Services/Widget/Embedded Widget Service.md`, plus updates to `Full Page Widget Service.md`, `Get public organization.md`, `WidgetPrincipal.md`, `Table Ecosystems.md`

### F5 — `max-w-2xl` silently collapses to 48px · Medium · **Fixed, and documented**

Found while theming the harness. `tokens.css` defines a spacing scale keyed by t-shirt sizes (`--spacing-xs` … `--spacing-2xl`). Tailwind's width utilities read the **container** namespace for those same names and fall back to the spacing scale when a key is missing there, so on any page importing these tokens:

```text
max-w-2xl  ->  48px   (--spacing-2xl)     not 42rem
max-w-3xl  ->  48rem  (--container-3xl)   fine — 3xl is not a spacing key
```

A `max-w-2xl` container collapses to 48 pixels and its text wraps one word per line. No build error, no lint error. Confirmed in the browser: the harness's configuration notice reported `width: 64px, max-width: 48px`.

The **library is clean** — it uses no `w-*`/`max-w-*` t-shirt size at all, and neither does the client app. The only occurrence was in the harness page written during this review; fixed with `max-w-[42rem]`. Documented in `widget/AGENTS.md` → "The t-shirt size collision", because a future variant would hit it with no signal.

## Theme — Obsidian Dark (author request, 2026-09-22)

The library's own palette was already the client's Obsidian Dark, correctly scoped to `.pbm-widget`. What was not dark was everything **around** it: `body` had no background, so the harness rendered a dark widget on a white page, and the catalog used `gray-*` utilities.

Changes:

- `tokens.css` — completed the copy of the client's `:root`: added `card`, `card-foreground`, `popover`, `popover-foreground`, `secondary`, `secondary-foreground`, `accent`, `accent-foreground`, `destructive-foreground`, and the universal 10px radius scale. Radius mappings fall back to **Tailwind's own defaults** rather than a host variable, because a host without shadcn has no `--radius` and `rounded-lg` must not break on their pages. Verified in the emitted CSS: `.rounded-lg{border-radius:var(--pbm-radius-lg,.5rem)}`.
- `tokens.css` — carried the client's autofill repaint, scoped to `.pbm-widget`. The widget's details form is exactly where Chrome autofill fires, and without it the inputs flash near-white against the dark surface.
- `app/layout.tsx` — `<body className="pbm-widget bg-background text-foreground …">`, so the harness wears the library's own theme class rather than defining tokens of its own. This is also the pattern a client site uses to theme a whole booking page.
- `app/page.tsx`, `app/barbershop/kinetic/page.tsx` — restyled onto theme tokens.

Verified in the browser rather than by inspection: `body` computes to `rgb(11,13,16)` / `rgb(249,250,251)`, a surface panel to `rgb(18,21,26)`, radius 14px — the client's `#0b0d10`, `#f9fafb`, `#12151a` and `--radius × 1.4` exactly. Dark Reader was active in the test browser and did not alter the values.

Note: the Next.js dev overlay reports one hydration issue on every page. It is the Dark Reader extension injecting `data-darkreader-proxy-injected` onto `<html>`, not application code.

### F6 — the widget was unusable in a browser: two missing CORS entries · High · **Fixed**

Found on the first real browser run against a live server (2026-09-23), after the author created a widget through the dashboard.

`core/api/client.ts` sets `X-Correlation-Id` on every request — carried from the client's axios instance, where it is same-origin and free. From a tenant's site every call is cross-origin, so that header has to be allow-listed or the **preflight fails and the request never goes out**. `app.cors.allowed-headers` listed only `Authorization` and `Content-Type`.

The symptom was maximally unhelpful: bootstrap succeeded (its preflight only needs `content-type`, because `session.ts` uses a bare axios call), then the very first `getOrganization()` was blocked, so the widget rendered "This booking page isn't available" — which reads like a server or tenant problem, not a CORS one.

A second, quieter instance of the same class: `errors.ts` reads `Retry-After` for the 429 countdown, but cross-origin JavaScript cannot read a header that is not in `Access-Control-Expose-Headers`. The countdown would always have fallen back to a guessed 60 seconds.

Both fixed in `application.yaml` with comments explaining why each entry exists. Verified by preflight (`Access-Control-Allow-Headers: authorization, x-correlation-id`, `Access-Control-Expose-Headers: Location, Retry-After`) and then in the browser — the widget now renders the service list against live data.

**Why the review missed it**: every check I ran, including the M-series shape, used curl, and curl does not enforce CORS. A cross-origin library cannot be validated by a same-origin tool. Worth carrying into the next task: any header the library adds is a deployment prerequisite, and only a browser proves it.

## Not filed as findings

Per §15, the items on the D4/D6 deferred list are decisions with a named owner and are not defects: the authorization engine being unconsulted, bootstrap's body-vs-header origin, no post-bootstrap origin re-verification, and both keys shipping in the client bundle. All four are recorded in `SECURITY.md`.

## Remaining for the author

1. The **M-series** (§12) — needs a running server and a dashboard-created widget. M9 (revoke mid-session) and M11 (rate limit) are the two that exercise code paths nothing else covers.
2. The **V-series** browser walkthrough, and then the **B-series** functional pass, including B2 — pasting the library into a throwaway `create-next-app` on port 3003. B2 is the only real test of D1 and D8, and no agent may run it (D14).
3. A decision on F3.
