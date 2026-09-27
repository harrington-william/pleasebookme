# Task Contract

## 1. IDENTITY

Title: Full-page booking widget — the hosted public booking page every tenant
       gets by default (`pleasebookme.app/{organizationSlug}` and
       `/{organizationSlug}/{serviceSlug}`), the `permitAll` public API that
       serves it, Redis rate limiting in front of that API, write-time slot
       re-validation, the removal of `FULL_PAGE` from `WidgetType`, and a
       frontend that ports the MVP v2 booking widget onto the Stitch
       "Kinetic Flow" design.
Domain: Database / `widget.widget_type` enum (V143, already authored — see §7)
        Server / `service/publicbooking` (new cross-domain orchestration over
        `organization`, `tenant`, `core.service`, `core.bookingpolicy`,
        `core.availability`, `core.booking`, `core.attendee`, `service/slot`)
        Server / `security/ratelimit` (new — Redis fixed-window limiter +
        interceptor), `security/config` (one matcher, one interceptor
        registration, forward-headers)
        Server / `widget/enums`, `organization/organizations`,
        `service/workspace` (small edits)
        Client / `app/(booking)/…`, `app/api/public/…`,
        `features/public-booking/`, `features/widgets/types` (enum cleanup),
        `app/globals.css` (one type-scale entry)
Priority: High — this is the first surface through which a tenant's *customer*AttendeeRepository, and SlotService — it doesn't lean on core.booking any more than it leans on organization or tenant, it just happens to write one bookings row at the end. That's 
          can create a booking. Until it exists the product has a dashboard
          and no product.
Risk: Medium. One migration that recreates a native enum (already reviewed by
      the author). Three new unauthenticated endpoints — the first
      `permitAll` paths outside `/api/v1/auth`. A write path reachable by
      anonymous callers, protected by rate limiting and a pessimistic lock.
      Risk concentrates in: (a) the trust rule for `X-Forwarded-For`, (b) the
      lock + re-validation ordering inside the booking transaction, (c) the
      visitor-timezone → schedule-timezone date mapping in the client, which
      is hand-written `Intl` arithmetic with no library behind it.

Status: ACTIVE

Decided by the task author on 2026-09-17 (do not re-open without escalating):

| # | Decision | Choice |
|---|---|---|
| D1 | What the full-page widget *is* | **Not a widget row.** No `widget.widgets` record, no public/secret key, no `widget_origins`, no `WidgetPrincipal`, no bootstrap. It is a first-party public page keyed by `organization.slug`. `FULL_PAGE` leaves `WidgetType`; tenants choose only `INLINE`/`POPUP`/`EMBEDDED` for embeds on their own sites (TASK-0012). |
| D2 | Authentication model for the public API | **None.** `/api/v1/public/**` is `permitAll`. No guest JWT, no new `AuthenticatedPrincipal` type, no `IdentityLoader`. The authorization engine (`security/authorization`) is not involved. Rejected alternative: a credential-less "guest" token — it authenticates nothing and only adds ceremony. |
| D3 | URL shapes | Server: `/api/v1/public/{orgSlug}`, `/api/v1/public/{orgSlug}/service/{serviceSlug}`, `…/service/{serviceSlug}/slots?date=`, `…/service/{serviceSlug}/bookings`. Client: `/{orgSlug}` and `/{orgSlug}/{serviceSlug}` — prefix-less, Calendly-style. Consequence: the platform reserves a small set of root slugs (§9.6). |
| D4 | Abuse controls in this task | **Rate limiting only**: Redis fixed-window, keyed by client IP, enforced on Spring, **fail-closed** (a Redis error is a 500, not a bypass — consistent with the compose note "failing loudly beats silently dropping security state"). hCaptcha and IP/email/phone blacklists are deferred (§16) — the hook point is named in §9.4. |
| D5 | Where the browser's requests go | **The BFF hop is preserved** (`client/AGENTS.md` → "The browser never calls Spring Boot directly"). Public reads are Server Components calling the gateway; slots and booking POST go through Route Handlers under `app/api/public/…`. Route Handlers forward the visitor's IP as `X-Forwarded-For`; Spring enables `server.forward-headers-strategy: native` so Tomcat's `RemoteIpValve` rewrites `getRemoteAddr()` **only when the immediate peer is an internal proxy**. Locally there is no upstream header, so every visitor shares one bucket — documented, not a defect. |
| D6 | Write-time re-validation | Inside the booking transaction: pessimistic lock on the **host user row** (conflicts are host-wide, TASK-0009 D3), then `SlotService.getAvailableSlots(serviceId, dateInScheduleZone)` must contain exactly `TimeSlot(slotStart, slotStart + defaultDuration)`; otherwise `SlotUnavailableException` (409). The engine is not modified. |
| D7 | Booking row shape | `user = service.user` (host), `service`, `title = "<service.title> with <attendee.name>"`, `description = notes`, `location = service.location`, `startTime/endTime` = the validated slot, `status = policy.autoConfirm ? ACCEPTED : AWAITING_HOST`, everything else entity default. One `core.attendees` row: `name`, `phone`, `email`, `timezone` from the request, `locale = null`. |
| D8 | Who is served | The org's tenant must exist with `status ∈ {ACTIVE, TRIAL}`. Unknown slug, reserved slug, missing tenant, any other status, unknown service, service without a policy → **the same 404** (`OrganizationNotFoundException` / `ServiceNotFoundException`), so the endpoint cannot be used to tell "suspended" from "nonexistent". `organizations.is_private` is **not** consulted — provisioning sets it `true` on every new org and its meaning is undocumented; gating on it would 404 every fresh tenant's page. |
| D9 | Which services are listed | Every `core.services` row of the organization that has a `core.booking_policies` row. There is no `is_active`/`hidden` column on services; a service without a policy is not bookable and is omitted (and 404s on direct access). |
| D10 | Contact fields | Mirror `core.attendees`: `phone` is `NOT NULL` → required; `email` is nullable → optional. No `@Email` (CODING_CONVENTIONS: no format validators the column does not declare); the client's zod `z.email()` is the UX guard. **Author: confirm this matches the launch market's expectation before Phase E** — it is the one product-facing consequence of "mirror the migration". |
| D11 | Visitor-date → schedule-date mapping | Done in the **browser hook**, not the server. The engine's `date` is a calendar date in `schedule.timezone` (TASK-0009 D2); a visitor in another zone picks a *local* date, whose instants can span one or two schedule dates. The hook computes those (≤ 2) with the `Intl`-based date math ported from MVP v2 (§9.12), calls the BFF once per schedule date, merges, and keeps slots whose `slotStart` falls on the chosen local date. The BFF slots route is a 1:1 pass-through. |
| D12 | CRM | No `customer.customers` row is created. The attendee row is the record. |
| D13 | Limits (defaults, configurable) | Read bucket: 120 requests / 60 s / IP across the three GETs. Write bucket: 5 requests / 60 s / IP on the POST. 429 carries `Retry-After`. |
| D14 | Sources of truth for the frontend, in precedence order | (1) **Platform data** — what §9.2 actually returns; nothing is rendered that the API does not provide. (2) **Stitch structure** — `design/widget/stitch/step_*` for layout, hierarchy, copy, states, and interaction (§9.9) — **as a demo reference, not a target**: it is the project's demo design, not the final one, and §9.9 already simplifies it. Do not compare against `screen.png`. (3) **MVP v2 behaviour** — `legacy_reference/pleasebookme-mvp-v2/client/` for state handling, hooks, date math, formatting, validation (§9.8). (4) **Client tokens** — `client/app/globals.css` / `design/client/DESIGN.md` prose for every colour, type size, radius and spacing; the Stitch Material palette is *mapped* onto them (§9.10), never copied as hex. |
| D15 | The flow | **Six steps**: Service → Date → Time → Details → Review → Done. Stitch's step 2 "Staff" is dropped (the platform has no staff/resource choice on the public path); its step 6 "Review" is **kept** — the booking `POST` fires on "Confirm Booking" in Review, not on the Details submit. Progress bar and "STEP n OF 6" eyebrows count six. |
| D16 | Where step 1 lives | `/{orgSlug}` *is* step 1 (Choose a Service) rendered inside the same shell; "Continue" navigates to `/{orgSlug}/{serviceSlug}`, which opens at step 2 with "Service" ticked in the rail. A shared deep link to `/{orgSlug}/{serviceSlug}` therefore lands on Date with step 1 already complete. |
| D17 | Date math | **No date or calendar library.** `utils/dateUtils.ts` from MVP v2 is ported and extended with `Intl.DateTimeFormat(...).formatToParts` arithmetic (§9.12). The MVP's `react-day-picker` is **not** carried — its `Date`-object API is browser-local and cannot disable "before today" in a *display* timezone the visitor chose; the month grid is hand-rolled from the ported math. `date-fns` is not added. |
| D18 | Layout | The full-bleed shell of Stitch steps 2–7 (fixed 256 px rail + main column) for **every** step, including Service. Step 1's centered 800×600 card frame is the *embedded* widget's framing and belongs to TASK-0012. |
| D19 | Step state | In memory, as in MVP v2's `BookingWidget` — not `?step=`. A reload restarts the flow. |
| A1 | **Amendment, 2026-09-21 — after the author's first look at the demo.** Supersedes D16, D18, the "no selection state" and "no motion" rows of §9.9's simplification table, and every `app/(booking)/` path below. | (1) The routes moved to `app/(public)/[organizationSlug]/` so the page **inherits `MarketingHeader`** — visitors need a way off the page. (2) The shell is a **centered card** (`max-w-5xl`, viewport-tall on `md+`), not full-bleed. (3) **One `BookingWidget` owns all six steps in memory**; `/{orgSlug}/{serviceSlug}` renders the same widget preselected at Date; picking a service fetches its detail through a new `GET /api/public/{org}/service/{svc}` BFF route instead of navigating. (4) The sidebar **morphs**: org identity (logo, name, bio, shared service location) on step 1, step list + selected-service summary/location from step 2, with `animate-in fade-in slide-in-from-left-2`; the progress bar animates. (5) **Step-back rule:** going to step N keeps choices ≤ N and clears everything after. (6) Service cards are buttons with the price vertically centred at 24 px; the previous choice stays outlined. (7) `cursor-pointer` on every clickable control. `booking-flow.tsx`, `booking-rail.tsx`, `booking-mobile-bar.tsx` are gone; `booking-widget.tsx`, `booking-sidebar.tsx` replace them. |
| D20 | **Demo posture — who tests what** | The frontend in this task is a **demo**: agents build it, then open it in a browser **only to look at it** (the V-series in §12) and confirm nothing is visibly broken. **Agents do not test the flow's functionality** — no race checks, no timezone-boundary walks, no keyboard matrices, no error-path clicking. **The author tests functionality personally** using the B-series, which is kept in §12 as the author's own checklist and is off-limits to agents and reviewers. Polish (animations, hover reveals, pixel fidelity) is explicitly deferred (§16); many fixes are expected after the author's pass. |
| A2 | **Amendment, 2026-09-22 — naming and route, applied after the build.** Supersedes the `service/publicbooking` package name, the `PublicBooking*`/`Public*` class names, the `/{organizationSlug}` client route in D3/D16/A1, and the reserved-slug half of §9.6. | (1) The server package is **`service/widget/fullpage`** and the classes are `FullPageWidgetController`, `FullPageWidgetService(Impl)` and `Widget*` DTOs (`WidgetOrganizationResponse`, `WidgetServiceSummary`, `WidgetServiceResponse`, `WidgetBookingRequest`, `WidgetBookingResponse`) — "public booking" is retired as a name; the hosted page is the platform's *full-page widget* and `service/widget/` holds one subpackage per booking channel. (2) The client route is **`/booking/{organizationSlug}`** and `/booking/{organizationSlug}/{serviceSlug}`, under `app/(public)/booking/`. (3) **`/api/v1/public/**` is unchanged**, as are the BFF routes under `app/api/public/**`, the `permitAll` matcher, the rate limiter and every response shape. (4) `ReservedOrganizationSlugs` no longer gates reads — the prefix removes the collision it existed for, and organization creation already refuses those names, so the resolver check was dead. It still guards `OrganizationServiceImpl.createOrganization`/`updateOrganization` and `WorkspaceProvisioningServiceImpl`, where it now protects brand names. The §11 acceptance criterion "reserved slug 404s before any query" is withdrawn; the replacement test is `getOrganization_formerlyReservedSlugIsJustAnUnknownOrganization`. Recorded in ADR-0001's Amendment. |

---

## 2. INTENT

Give every tenant a working public booking page at
`pleasebookme.app/{organizationSlug}` the moment their workspace is
provisioned, with no configuration and no credentials — the Calendly/Cal.com
model: a visitor opens the link, picks a service, picks a day, picks a time,
types a name and a phone number, reviews, confirms, and has a booking. No
account, no login, no captcha (yet).

Two widget kinds now exist and must not be conflated:

| | Full-page widget (this task) | Embedded widget components (TASK-0012, `widget/`) |
|---|---|---|
| Lives on | `pleasebookme.app` | the tenant's own website |
| Exists because | the tenant *did not* embed anything | the tenant embedded a script/iframe |
| Per-tenant setup | none — exists by default | create a widget, pick `INLINE`/`POPUP`/`EMBEDDED`, register an origin |
| Authenticates as | nobody (`permitAll`) | `WidgetPrincipal` via public/secret key + origin (`POST /api/v1/auth/widget/bootstrap`) |
| Protected by | rate limiting, later captcha/blacklist | origin check, key pair, tenant binding |
| Design source | the same Stitch "Kinetic Flow" mocks, full-bleed variant | the same mocks, card-framed variant |

The server side of this task is three read endpoints and one write endpoint
under `/api/v1/public`, a rate limiter in front of them, and a booking-creation
path that — for the first time in this codebase — re-checks the slot engine
before writing. The client side is a port of the MVP v2 widget's *behaviour*
(step machine, request-keyed hooks, `Intl`-based date math, zod form) onto the
Stitch *design* (rail + progress bar + six one-decision screens), built with
the client's own tokens.

```text
browser ─▶ Next.js (booking) pages       ──gateway──▶ GET /api/v1/public/{org}
        ─▶ app/api/public/…/slots        ──────────▶ GET /api/v1/public/{org}/service/{svc}/slots?date=
        ─▶ app/api/public/…/bookings     ──────────▶ POST /api/v1/public/{org}/service/{svc}/bookings
                                                          │
                              PublicRateLimitInterceptor ─┤ 429 if the IP bucket is full
                              PublicBookingServiceImpl ───┤ resolve org → tenant gate → service → policy
                                                          │ lock host user row
                                                          │ SlotService.getAvailableSlots(service, date)  ⟵ TASK-0009, untouched
                                                          │ slot ∈ result ? insert booking + attendee : 409
```

### This frontend is a demo

The Stitch "Kinetic Flow" mocks are the project's *demo* design — a starting
point the author will iterate on after seeing it work, not a specification
to match. §9.9 already strips them to what a quick demo needs (no
animations, no hover-reveals, no sticky gradients, no icons inside inputs).
Build the simple version, make it look coherent with the client's tokens,
open it once to look at it, and stop. Do not spend time on fidelity, motion,
or edge-case UX — that time is reserved for after the author's own
functional pass (D20).

### What this task deliberately does not defend against

Nothing here proves the caller is a human or a repeat customer. A single IP
can create 5 bookings a minute; a botnet can create more. That is the accepted
v1 posture: the platform launches with rate limiting, and hCaptcha and
blacklists are queued behind it (§16). Do not "harden" beyond §9 in this task
— record the gap in `SECURITY.md` and move on.

---

## 3. DELIVERABLES

### Database

1. `server/src/main/resources/db/migration/widget/V143__drop_widget_type_full_page.sql`
   — **already written and reviewed by the author**; applied through Flyway on
   the first boot of Phase A. Do not edit it.
2. `database/init/widget/V226__drop_widget_type_full_page.sql` — the commented
   original, per the dual-location workflow. Same statements as V143 plus the
   *why* comments (§9.1).

### Server — `widget/`, `organization/`, `service/workspace/`

3. `widget/enums/WidgetType` — remove `FULL_PAGE`.
4. `organization/organizations/ReservedOrganizationSlugs` — the reserved-slug
   set (§9.6); checks in `OrganizationServiceImpl.createOrganization` /
   `updateOrganization` and in
   `WorkspaceProvisioningServiceImpl.createOrganization`.

### Server — `security/ratelimit/` (new) and `security/config/`

5. `ratelimit/RateLimiter` (interface) + `ratelimit/RedisRateLimiter`
   (`@Component`, Lua `INCR`+`EXPIRE`, §9.3).
6. `ratelimit/RateLimitProperties` (`@ConfigurationProperties("app.rate-limit")`,
   record, `@Validated`, shape of `CorsProperties`).
7. `ratelimit/PublicRateLimitInterceptor` (`HandlerInterceptor`) registered in
   `WebConfig.addInterceptors` for `/api/v1/public/**`.
8. `ratelimit/exception/RateLimitExceededException` + handler in
   `GlobalExceptionHandler` → 429 with `Retry-After`.
9. `SecurityConfig` — one matcher: `.requestMatchers("/api/v1/public/**").permitAll()`.
10. `application.yaml` — `server.forward-headers-strategy: native`,
    `app.rate-limit.public.{read,write}.{limit,window}`.

### Server — `service/publicbooking/` (new)

11. `controller/PublicBookingController` — `@RequestMapping("/api/v1/public")`,
    four handlers (§9.2).
12. `dto/PublicOrganizationResponse`, `dto/PublicServiceSummary`,
    `dto/PublicServiceResponse`, `dto/PublicBookingRequest`,
    `dto/PublicBookingResponse` — records.
13. `service/PublicBookingService` + `service/impl/PublicBookingServiceImpl`.
14. `exception/SlotUnavailableException` + handler → 409.

### Server — repositories (derived finders only)

15. `OrganizationRepository.findBySlug(String)` → `Optional`.
16. `ServiceRepository.findByOrganizationOrganizationIdAndSlug(BigInteger, String)` → `Optional`.
17. `BookingPolicyRepository.findByServiceServiceIdIn(Collection<BigInteger>)` → `List`.
18. `UserRepository.findLockedByUserId(BigInteger)` → `Optional`, annotated
    `@Lock(LockModeType.PESSIMISTIC_WRITE)`.

### Client — `client/`

19. `features/widgets/types/widget.ts` — `WIDGET_TYPES` and
    `WIDGET_TYPE_LABELS` lose `FULL_PAGE`; `components/widget-type-picker.tsx`
    loses the `FULL_PAGE` option (and the `Maximize2` import).
20. `app/globals.css` — one addition to the static type scale:
    `--text-headline-sm` (18 px / 26 px / 600), the Stitch card-title size the
    client scale lacks (§9.10).
21. `app/(booking)/layout.tsx`, `app/(booking)/not-found.tsx`,
    `app/(booking)/[organizationSlug]/page.tsx`,
    `app/(booking)/[organizationSlug]/[serviceSlug]/page.tsx`.
22. `app/api/public/[organizationSlug]/service/[serviceSlug]/slots/route.ts` (GET),
    `app/api/public/[organizationSlug]/service/[serviceSlug]/bookings/route.ts` (POST).
23. `features/public-booking/`:
    - `types/public-booking.ts`
    - `schemas/booking-details-schema.ts` (ported `customerSchema`)
    - `services/public-booking-gateway.ts`, `public-booking-api.ts`,
      `public-booking-errors.ts`
    - `hooks/use-available-slots.ts` (ported `useSlots` request-key pattern +
      the D11 fan-out)
    - `lib/date-math.ts` (ported + extended `dateUtils.ts`, §9.12),
      `lib/format.ts` (ported `format.ts`), `lib/calendar-link.ts`
      (Google Calendar template URL), `lib/booking-steps.ts` (step
      definitions, copy, progress)
    - `components/` — `booking-shell.tsx` (rail + main + progress),
      `booking-rail.tsx`, `booking-mobile-bar.tsx`, `booking-footer.tsx`,
      `step-header.tsx`, `selected-service-strip.tsx`,
      `service-picker.tsx` + `service-card.tsx` (step 1),
      `booking-flow.tsx` (steps 2–6 state machine),
      `booking-calendar.tsx` + `timezone-select.tsx` (step 2),
      `slot-list.tsx` + `slot-group.tsx` (step 3),
      `booking-details-form.tsx` (step 4),
      `booking-review.tsx` (step 5),
      `booking-success.tsx` (step 6),
      `spinner.tsx`, `error-message.tsx` (ported MVP primitives, re-skinned)

### Tests

24. Server: `service/publicbooking/PublicBookingServiceImplTest`,
    `security/ratelimit/RedisRateLimiterTest`,
    `security/ratelimit/PublicRateLimitInterceptorTest`,
    `organization/organizations/OrganizationServiceImplTest` (reserved slug),
    `ServerApplicationTests`.
25. Client: `vitest` as a **devDependency only**, with
    `features/public-booking/lib/date-math.test.ts` covering §9.12 — the only
    automated proof of D11. No other client tests are required.

### Documentation

26. `SERVER_AGENTS.md` — new `# Public booking page` section; correct the
    stale "Current applied schema version: v141" to v143; update the
    `widget/` package note and the widget-dashboard note (`WidgetType` now
    has three values).
27. `SECURITY.md` — new section **Public Booking Endpoints** (what is
    `permitAll`, what protects it, what it is *not*, the `X-Forwarded-For`
    trust rule, the deferred controls).
28. `client/AGENTS.md` — new "The public booking page" section (route group,
    BFF public routes never read cookies, the date math and where it lives,
    the `X-Forwarded-For` forwarding, the Stitch → token mapping, why there
    is no date library); fix "Auth endpoints … are the only `permitAll()`
    paths"; fix "The platform accepts four `WidgetType`s".
29. Obsidian (`obsidian/PleaseBookMe/`):
    - `Database/Schemas/Widget/Widget Types.md` — currently **empty**; populate
      with the three types and a "Removed: FULL_PAGE (V143)" note.
    - `Database/Schemas/Widget/Table Widgets.md`, `DB.md`,
      `API/Widget/Widgets/List widgets.md` — drop `FULL_PAGE` from the value lists.
    - `Services/PublicBooking/Public Booking Service.md` — via
      `.skills/documentation/service-documentation`.
    - `API/Public/Public API Summary.md` + `Get public organization.md`,
      `Get public service.md`, `Get public slots.md`, `Create public booking.md`
      — via `.skills/documentation/api-documentation` and the templates in
      `API/`.
30. `.agents/decisions/ADR-0001-public-booking-page-is-not-a-widget.md` — the
    folder does not exist yet; create it. Context (two widget kinds), D1/D2,
    the rejected guest-token alternative and why, consequences (§16 list).

### Closing report

31. Modified-file list, executed validation commands **with output**, the
    exact JSON of one real response from each of the four endpoints, the
    V-series visual notes (one desktop screenshot per step plus one mobile
    screenshot — no comparison against the mocks), unresolved risks, the
    §16 items you were tempted to do and did not, and a **"Ready for the
    author's functional pass"** line confirming that no B-series scenario
    was executed by an agent.

Review agents produce `.agents/reviews/TASK-0011-full-page-widget-review.md`.

---

## 4. SCOPE

### In scope

- Everything in §3.
- The `V143` apply (through Flyway, by booting) and its `database/init` mirror.
- The reserved-slug guard (§9.6) — small, and the only thing that makes D3's
  prefix-less URL safe.
- `Retry-After` on 429.
- The `availableWeekdays`, `weekStart` and `successRedirectUrl` fields on the
  public responses — each exists because a Stitch screen needs it (§9.9).
- The Google Calendar link on the success screen — a template URL built from
  data already on the page, no API, no auth.

### Out of scope (reasoning in §16)

- hCaptcha or any bot challenge; IP/email/phone blacklists.
- Idempotency on the public POST (`idempotency_key` stays `null`).
- Attendee-facing cancel/reschedule links, confirmation emails, any
  `notification.*` write.
- `customer.customers` upsert.
- A "your booking page" link in the dashboard.
- A month/range availability endpoint; `maxActiveBookingPerBooker`,
  `capacity`, `allowOverlap`, `allowMultipleAttendee`.
- Re-validation inside the dashboard's `BookingServiceImpl.createBooking`.
- Light theme (`step_1_select_service_light`) / tenant branding on the public page.
- Stitch's "Staff" step, "Any Available", ratings, reviews, "Popular" badge,
  service cover images, "Need Help?", Privacy/Terms/Support links,
  "Add to Google Calendar" *via the Calendar API* (the template link is in).
- `?step=` deep links / browser-back between steps.
- Any change to `service/slot/` (the engine), `WidgetServiceImpl`,
  `WidgetIdentityLoader`, `JwtAuthenticationFilter`, `AuthenticationTokenFactory`,
  `AuthenticatedPrincipal`, or anything under `security/authorization/`.
- The embedded widget workspace `widget/` (TASK-0012).

---

## 5. BOUNDARIES

- **`/api/v1/public/**` is the only new `permitAll` matcher.** Do not widen
  `/api/v1/slots` or `/api/v1/bookings`; they keep their client-supplied
  `userId`/unscoped `serviceId` shapes and stay Bearer-only.
- **No principal is read anywhere in `service/publicbooking`.** No
  `CurrentPrincipalProvider`, no `SecurityContextHolder`, no
  `CurrentOrganizationProvider`. Identity comes from the URL path only.
- **No new actor type, no new `IdentityLoader`, no new branch in
  `JwtAuthenticationFilter`.** D2 is settled.
- **The slot engine is read, not changed.** `SlotService.getAvailableSlots`
  is called as-is. If it does not answer the question you need, that is
  Escalation #4, not a reason to add a method to it.
- **Booking and attendee rows are written through repositories directly**, in
  one `@Transactional` method on `PublicBookingServiceImpl` — the same reason
  `WorkspaceProvisioningServiceImpl` does (there is no principal, so
  `BookingServiceImpl.createBooking`'s `BookingRequest.userId` shape is the
  wrong tool). Do not call `BookingService`/`AttendeeService`.
- **The lock is on `auth.users` (the host), taken *before* the slot engine
  runs.** Not on the service row (conflicts are host-wide), not after the
  check (that is the race).
- **The rate limiter is `security/` infrastructure**, shape of
  `OAuthStateStore`/`RedisOAuthStateStore`: an interface, one Redis
  implementation, `StringRedisTemplate` injected. No new dependency. No
  Bucket4j.
- **`server.forward-headers-strategy` must be `native`, never `framework`.**
  `framework` installs `ForwardedHeaderFilter`, which trusts `X-Forwarded-For`
  from *any* peer — a direct caller could then spoof its way past IP limiting.
  `native` uses Tomcat's `RemoteIpValve`, which rewrites the address only when
  the immediate peer matches `server.tomcat.remoteip.internal-proxies`
  (default: loopback + RFC 1918 ranges). Record the production knob in
  `SERVER_AGENTS.md`; do not set a production value here.
- **Public BFF routes never touch cookies or `withAccessToken`.** A signed-in
  tenant visiting their own page must not have a Bearer token attached — a
  stale one would make `JwtAuthenticationFilter` answer 401 on a public route.
- **No date library, no calendar library, no icon font.** `Intl` for every
  zone conversion and every formatted string; `lucide-react` for every icon
  (§9.10). The MVP's `react-day-picker` and Stitch's Material Symbols are
  both replaced, deliberately (D17).
- **No Stitch hex value reaches the client code.** `#adc6ff`, `#111317`,
  `#1e2023`, `#424754`, `#c2c6d6` and friends are mapped to client tokens in
  §9.10. `bg-[#3B82F6]` is `bg-primary`. A reviewer grepping the diff for
  `#[0-9a-fA-F]{6}` should find nothing under `features/public-booking`.
- **No booking data is rendered that the API did not return.** No ratings, no
  "Popular", no staff, no address on the org rail unless a service supplies
  `location`, no "cancellation policy" line.
- **Do not modify** `BookingEntity`, `AttendeeEntity`, `ServiceEntity`,
  `OrganizationEntity`, `TenantEntity`, `BookingPolicyEntity`,
  `SlotServiceImpl`, `BookingSpecifications`, `WidgetServiceImpl`, `proxy.ts`,
  `components/ui/*` (shadcn-owned).
- **Do not add a `MethodArgumentNotValidException` handler.** The client
  synthesizes validation messages from the 400 exactly as `auth-errors.ts`
  does; changing the global 400 shape is its own task.
- **Do not put the visitor-date mapping on the server.** D11.

---

## 6. CONSTRAINTS

### Architectural

1. Interface × impl, `@Service` + `@RequiredArgsConstructor`, records for
   DTOs, derived finders only, controller = DTO translation only
   (`CODING_CONVENTIONS.md`, `.skills/technologies/spring-boot/*`). Return
   `ResponseEntity<T>`; `201` for the POST via `ResponseEntity.status(HttpStatus.CREATED)`.
2. `service/publicbooking` sits in the orchestration layer because it spans
   `organization`, `tenant`, `core` and `service/slot` (`SERVER_AGENTS.md` →
   "Package structure"). Do not put it under `core/booking`.
3. `PublicBookingServiceImpl.createBooking` is `@Transactional`
   (`org.springframework.transaction.annotation`). The read methods are
   `@Transactional(readOnly = true)`.
4. `PublicBookingRequest` validation maps off the `core.attendees` /
   `core.bookings` columns, per the CODING_CONVENTIONS table (§9.2).
5. Comments explain *why*, per `.skills/workflows/quality-code-comments`.
   Server: the `native` forward-headers choice; fail-closed on Redis errors;
   lock-before-engine ordering; `AWAITING_HOST` vs `PENDING`; the
   same-404-for-every-reason rule; `is_private` deliberately ignored;
   `findByServiceServiceIdIn` (why one `IN` query); `availableWeekdays` being
   a hint, not a contract. Client: why `toDateString` avoids `toISOString`
   (carried from MVP); why `startOfDayInZone` re-derives once (DST); the D11
   fan-out; why `loading` is derived from the request key (carried from MVP);
   why there is no date library; the Morning/Afternoon/Evening boundaries.
6. Client: `client/AGENTS.md` conventions in full — BFF hop, `features/<domain>/`
   layout, `cn()`, tokens, zod v4, async `params`/`cookies`, Base UI form
   controls, Geist as the only font. `features/public-booking/` mirrors the
   server package name.
7. Client dependencies: **none at runtime.** `vitest` as a devDependency for
   §9.12 only. Anything else is Escalation #9.

### Security

8. Every value that identifies *whose* data is read or written comes from the
   path (`orgSlug`, `serviceSlug`) or from rows resolved from it — never from
   the body. The body carries attendee fields, `slotStart`, `notes`, nothing
   else.
9. Responses expose only what a public page needs. No `organizationId`,
   `userId`, `tenantId`, `scheduleId`, `profileId`; no booking ids of *other*
   bookings; no buffer/capacity policy internals; no `metadata`. The one
   pre-existing numeric id in `AvailableSlotsResponse.serviceId` is accepted
   because the record is reused verbatim.
10. The four "not available" reasons in D8 are indistinguishable on the wire.
11. Rate limiting is enforced **on Spring**, never only in the BFF. The BFF
    limiter is not built.
12. Fail-closed: `RedisRateLimiter` does not catch `DataAccessException`.

### Performance (`.skills/workflows/performance-avoid-quadratic`)

13. `GET /{orgSlug}`: organization, tenant, services, policies — **four**
    queries, the last an `IN`. No query in a loop over services.
14. `GET /{orgSlug}/service/{serviceSlug}`: organization, tenant, service,
    policy, availabilities — five.
15. `POST …/bookings`: organization, tenant, service, policy, locked user,
    then the engine's own six, then two inserts. Nothing in a loop.
16. The interceptor issues **one** Redis round-trip per request (the Lua
    script), plus one `TTL` only on the 429 path.
17. Client: `useAvailableSlots` issues ≤ 2 BFF calls per selected date and
    never re-fetches on a timezone change alone when the schedule-date set
    is unchanged (memoise on the computed key, §9.8).

### Design (see §9.9–§9.11 for the demo spec)

18. This is a demo (D20). Build the **simplified** version in §9.9 — the
    "Demo simplifications" list there is binding. Do not compare screenshots
    against `screen.png`; do not chase fidelity. If a Stitch detail is not in
    §9.9, leave it out.
19. Colours, type, radius and spacing come only from `app/globals.css`
    utilities. The single sanctioned addition is `--text-headline-sm` (#20).
    This is the one design rule that is *not* relaxed — the demo must look
    like it belongs to the app.
20. Use native `<button>`, `<input>`, `<textarea>`, `<select>` (or the
    existing `components/ui/*` wrappers) so keyboard and focus behaviour come
    for free. No custom ARIA widgets, no roving tabindex, no focus
    management on step change.
21. Touch targets: day cells 40 px, slot pills 48 px, primary buttons 48 px —
    cheap to get right, keep them.

---

## 7. DEPENDENCIES

### Already done — do not rebuild

| Component | Status |
|---|---|
| `V143__drop_widget_type_full_page.sql` | Written, reviewed by the author, **not yet applied**. Flyway applies it on the first `bootRun` of Phase A. High-water mark before this task: V142 (`SERVER_AGENTS.md` says v141 — stale, fix in #26). |
| `service/slot/SlotService.getAvailableSlots(BigInteger serviceId, LocalDate date)` → `AvailableSlotsResponse(serviceId, date, timezone, slots)`; `TimeSlot(Instant slotStart, Instant slotEnd)` record | TASK-0009. `TimeSlot` is a record, so `List.contains(new TimeSlot(...))` is value equality. `now` is computed inside, so minimum notice is enforced by the re-validation for free. |
| `OrganizationRepository.existsBySlug`, `ServiceRepository.findByOrganizationOrganizationId`, `BookingPolicyRepository.findByServiceServiceId`, `AvailabilityRepository.findByScheduleScheduleId`, `TenantRepository.findByOrganizationOrganizationId` | Exist. |
| `OrganizationNotFoundException`, `ServiceNotFoundException`, `BookingPolicyNotFoundException` + handlers (404) | Exist. D8 reuses the first two. |
| `StringRedisTemplate` bean; `RedisOAuthStateStore` / `RedisSessionHandoffStore` as the injection precedent; `RedisOAuthStateStoreTest` as the Mockito precedent | Exist. |
| `WebConfig implements WebMvcConfigurer` (`addCorsMappings`) | Exists — add `addInterceptors` there. |
| `CorsProperties` record (`@Validated @ConfigurationProperties`) registered via `@EnableConfigurationProperties` on `WebConfig` | The shape for `RateLimitProperties`. |
| `JwtAuthenticationFilter` passes through when there is no `Authorization` header | Verified — public routes need no filter change. It returns 401 on a *bad* Bearer regardless of path, hence Boundary "public BFF routes never touch cookies". |
| `GlobalExceptionHandler` handler shape (`ApiErrorResponse(status, message, data, client, path)`) | Copy the `ResourceNotFoundException` handler. |
| Client: `platformClient()` / `bffClient`, `normalizeApiError`, `ApiRequestError`, feature layout, `design/client/DESIGN.md`, `app/(public)/layout.tsx` (marketing header — the thing the new group avoids), `components/ui/{button,input,label,select}.tsx`, `components/form/native-select.tsx`, `GridBackground` in the root layout | Exist. |
| Client: `proxy.ts` matcher excludes `api`; `PROTECTED_PREFIXES = ["/dashboard"]`, `GUEST_ONLY_ROUTES = ["/login", "/register"]` | Verified — `/{orgSlug}` is neither. No change. |
| MVP v2 client (`legacy_reference/pleasebookme-mvp-v2/client/`) | The behavioural source. Every file is listed in §8 with a carry/adapt/drop verdict. |
| Stitch mocks (`design/widget/stitch/step_1…7`, each `code.html` + `screen.png`; `kinetic_flow/DESIGN.md` = `design/widget/DESIGN.md`; `barber_appointment_booking_flow/code.html` = `step_1`) | The visual source. Read in §8, mapped in §9.10, specified in §9.9. |
| Provisioning (`WorkspaceProvisioningServiceImpl`) creates org (`isPrivate = true`, slug `<name-without-spaces>-organization`, uid fallback on collision, `weekStart` default `MONDAY`), `ACTIVE` tenant, schedule Mon–Fri 09:00–17:00, service `consultant-meeting` with policy (30/30, notice 120, advance 43200, `autoConfirm` default `true`) | This is why a fresh account has a working page (M1) with zero setup. |

### Baseline

```text
cd server
./gradlew compileJava
./gradlew test
cd ../client
npx tsc --noEmit && npx eslint .
```

All green on a clean tree before anything changes, else Escalation #1.

### Required infrastructure

- Postgres + Redis via compose (`SERVER_AGENTS.md` → Infrastructure). Redis is
  now on the request path of every public call, not just OAuth.
- For §12: one dev account provisioned through `/register` (gives `U`, org
  `O` with a slug, tenant `T`, service `S` = `consultant-meeting`). A second
  service `S2` on the same host, created via the dashboard, with a price and
  a `location` so the price and location paths render. `curl` for the
  M-series; the client dev server for the V-series (agents) and, later, the
  B-series (author).

---

## 8. INPUT CONTEXT

### Skills to invoke (mandatory)

| Skill | When |
|---|---|
| `.skills/technologies/flyway/SKILL.md`, `.skills/technologies/postgres/tables/SKILL.md` | Phase A — the init mirror and confirming V143's ledger row. |
| `.skills/technologies/spring-boot/services`, `controller-declaration`, `repositories`, `method-declaration` | Before any server signature. The `@Lock` finder and `findByServiceServiceIdIn` are the two repository additions that need care. |
| `.skills/workflows/quality-code-comments/SKILL.md` | Before Phase D and again before Phase H. The comments in §6 #5, nothing else. |
| `.skills/workflows/performance-avoid-quadratic/SKILL.md` | Before Phase E and Phase I (the D11 fan-out must stay ≤ 2 calls). |
| `.skills/domains/authorization/identity-access-engineering/SKILL.md` | Before Phase D — you are adding the first `permitAll` namespace outside auth. |
| `.skills/domains/api/api-tester/SKILL.md` | Phase J — M-series. |
| `.skills/domains/design/ui-designer/SKILL.md` | Before Phase I, and once per Stitch screen while building it. |
| `.skills/domains/design/ux-architect/SKILL.md` | Before Phase I — the six-step machine, back navigation, error placement, focus management on step change. |
| `run`, `claude-in-chrome` | Phase J **V-series only** — look and screenshot, never test (D20). Batch tool loads into one `ToolSearch`. |
| `.skills/documentation/service-documentation`, `api-documentation`, `document-generating` | Phase K. |
| `.skills/workflows/code-review/SKILL.md` | Reviewers. |

**Not needed:** `.skills/technologies/spring-boot/entity-declaration` (no entity
changes). If you open it, your plan has drifted.

### The MVP v2 widget — read every file, then apply the verdict

All paths under `legacy_reference/pleasebookme-mvp-v2/client/`. The MVP was a
single embedded instance against the *old* API (`/slots?userEmail&eventTypeSlug&date`,
`/appointments?userUID`, Bearer via a demo login). None of its API layer
survives; its **behaviour** does.

| File | Verdict | Why |
|---|---|---|
| `utils/dateUtils.ts` | **Carry + extend** → `features/public-booking/lib/date-math.ts` | The "mathematics". `toDateString` (local `YYYY-MM-DD` without `toISOString`'s UTC shift), `isPastDay`/`startOfToday`, `formatSlotTime` (`Intl` in a named zone), and the `formatToParts` extractor inside `toEventTypeLocalDateTimeString` — the last is the exact technique D11 needs. Full spec in §9.12. |
| `utils/format.ts` | **Carry + adapt** → `lib/format.ts` | `formatDuration` verbatim. `formatPrice` becomes `formatPriceRange(min, max, currency)` (the platform has two price columns) and drops the hard-coded `"vi-VN"` locale for the browser's. |
| `hooks/useSlots.ts` | **Carry the pattern** → `hooks/use-available-slots.ts` | The request-key state (`{key, slots, error}`, `loading = state.key !== requestKey`, `active` flag in cleanup). Extended to fan out to ≤ 2 schedule dates and merge (D11). |
| `hooks/useTeam.ts`, `useTeamServices.ts`, `useTeamStaff.ts` | **Drop** | Client-side fetching of the business; the new pages get organization and service from Server Components. `useTeamServices`' dedupe-by-title and `useTeamStaff` are staff concepts that do not exist on the public path. |
| `components/BookingWidget.tsx` | **Carry the shape** → `components/booking-flow.tsx` | The `Step` union, `STEP_COPY`, per-step `useState`s, `reset()`, `handleSubmitCustomer` (submitting/submitError), the guarded `step === X && data ?` rendering, the bottom summary strip. Staff state goes; a `review` step comes in (D15). |
| `components/calendar/DayPickerCalendar.tsx` | **Drop the library, keep the intent** → `components/booking-calendar.tsx` | `weekStartsOn` from the business, `disabled before today`, outside days shown, fixed six-week height, custom day button with `outside`/`disabled`/`selected`/`today` modifiers — all re-created on a hand-rolled grid (D17). |
| `components/slots/SlotList.tsx` | **Carry + re-skin** → `components/slot-list.tsx` | Loading / error / empty / list states; `selectedSlot?.slotStart === slot.slotStart` identity. Adds Morning/Afternoon/Evening grouping (Stitch step 4). |
| `components/booking/CustomerForm.tsx` + `lib/validation/customerSchema.ts` | **Carry + adapt** → `components/booking-details-form.tsx` + `schemas/booking-details-schema.ts` | The zod schema (name/phone/email/note, phone regex, email `""`-or-valid union) and the submit → `safeParse` → field-error map flow. Re-skinned to Stitch step 5. `onSubmit` now moves to Review instead of posting (D15). Phone `maxLength` becomes 50 (the platform column), note max 1000 stays. |
| `components/SuccessScreen.tsx` | **Carry the data, replace the layout** → `components/booking-success.tsx` | Service / time / reference rows; "Book another". Layout is Stitch step 7. |
| `components/service/ServiceList.tsx` | **Carry + re-skin** → `components/service-picker.tsx` + `service-card.tsx` | Title / price / duration / description per card; empty state copy. Selection + Continue per Stitch step 1 instead of select-navigates. |
| `components/staff/StaffList.tsx` | **Drop** | No staff step. |
| `components/common/Shell.tsx` | **Drop** | The centered card frame is the *embedded* frame (D18). |
| `components/common/Spinner.tsx`, `ErrorMessage.tsx` | **Carry + re-skin** | Same props; tokens instead of `gray-*`/`red-*`. |
| `lib/api/*`, `lib/auth/*`, `config/demo.ts`, `types/index.ts` | **Drop** | Old API, demo login, `localStorage` tokens. Types are rewritten from §9.2. `EntityId = number` and the "BigInteger serializes as a JSON number" comment are still true. |
| `app/layout.tsx`, `app/page.tsx`, `app/globals.css` | **Drop** | Geist font is already the client's; `AuthGate` has no equivalent. |

### The Stitch mocks — read every `code.html`; glance at `screen.png` once

All under `design/widget/stitch/`. This is the project's **demo design**, not
the final one (D14, D20). Read `code.html` for structure and copy; open each
`screen.png` once to understand the intent, then close it — §9.9 is what you
build, and it deliberately simplifies the mocks. Nothing is compared against
the PNGs afterwards. (They disagree with the HTML in small ways anyway: the
step 7 rail renders icon *names* because the icon font failed to load; step 4
shows "14:30 PM".)

| Mock | Used for | Notes |
|---|---|---|
| `step_1_select_service/` (= `barber_appointment_booking_flow/`) | Step 1 content: eyebrow + title header, service cards, selection indicator, sticky Continue | Its **card-framed shell** (800×600, sidebar with big logo/rating/address) is TASK-0012's; this task uses the steps-2–7 shell for step 1 too (D18). |
| `step_1_select_service_light/` | Not used | Light theme is out of scope; recorded so nobody "adds" it. |
| `step_2_select_barber/` | **The shell**: rail header, step list states (active / inactive / done), "Need Help?" slot, mobile top bar, progress bar placement, footer | Its content (staff cards, "Any Available") is dropped (D15). |
| `step_3_select_date/` | Step 2: calendar card, day states, timezone line, Back / Continue split | Rail shows ✓ on completed steps here — the canonical "done" treatment. |
| `step_4_select_time/` | Step 3: slot container, three time groups, pill buttons, selected pill, in-card actions, eyebrow under the card | The "14:30 PM" label is a mock typo; format from `Intl`. |
| `step_5_customer_info/` | Step 4: field anatomy (leading icon, label, optional tag), textarea, 1/3 + 2/3 actions | Field order is adapted for D10 (§9.11 step 4). |
| `step_6_review_booking/` | Step 5: bento summary cards with hover "Edit", total row, confirm CTA, fine print | "Professional" card dropped; fine print reworded (§9.11 step 5). |
| `step_7_booking_success/` | Step 6: ping-ring check, headline/subline, summary box, three actions | "Return to Website" is conditional on `successRedirectUrl`. |
| `kinetic_flow/DESIGN.md` (= `design/widget/DESIGN.md`) | Brand rules: "one decision per screen", 4 px grid, radii, elevation, calendar/slot/input/progress component rules | Its YAML frontmatter is the Material palette (`#adc6ff` primary); its **prose** says Primary is `#3B82F6`. Same contradiction `client/AGENTS.md` records for the client's own DESIGN.md, same resolution: **the prose wins**. |

### The patterns to mirror (read in full)

| File | Why |
|---|---|
| `server/.../service/slot/service/impl/SlotServiceImpl.java` | The engine you call; the `ZoneId.of(service.getSchedule().getTimezone())` you must replicate to compute the schedule-zone date; the `BLOCKING_STATUSES` you must not duplicate. |
| `server/.../service/workspace/service/impl/WorkspaceProvisioningServiceImpl.java` | Repositories used directly in one `@Transactional` unit with no principal; builders for `BookingEntity`/`BookingPolicyEntity`; where the reserved-slug fallback goes (`createOrganization`). |
| `server/.../integration/oauthstate/store/RedisOAuthStateStore.java` + its test | `StringRedisTemplate` usage, key prefix constant, the Mockito stubbing of `opsForValue()`. |
| `server/.../security/config/WebConfig.java`, `SecurityConfig.java`, `security/config/property/CorsProperties.java` | Where the interceptor, the matcher and the properties record go. |
| `server/.../global/handler/GlobalExceptionHandler.java` (`handleResourceNotFoundException`) | Handler shape. The 429 handler additionally sets `Retry-After`. |
| `server/.../core/booking/service/impl/BookingServiceImpl.java` (`createBooking`) | The builder fields — and the `userId`-from-request shape you are *not* reusing. |
| `server/src/test/.../core/service/BusinessServiceImplTest.java` | Mockito test shape. |
| `client/AGENTS.md` | All of it. Especially "Architecture: the BFF hop", "Folder conventions", "The public route group", "The services editor" (form rules carry over), "The widgets editor" (`SUPPORTED_WIDGET_TYPES`), "Design system". |
| `client/app/globals.css` | The tokens you map onto (§9.10). Note `--text-display` is 48 px — **not** the Stitch `display` (24 px). |
| `client/features/services/services/service-gateway.ts`, `service-api.ts`; `client/app/api/services/[serviceId]/route.ts` | Gateway / browser-API / Route Handler split. The public versions drop `withAccessToken` and add the forwarded IP. |
| `client/features/services/components/service-basics-panel.tsx` | How this codebase renders a labelled input, a textarea and an inline field error with react-hook-form + Base UI. |
| `client/features/auth/services/auth-errors.ts` | Synthesizing a message from a bare 400. |
| `client/components/dashboard/dashboard-sidebar.tsx`, `dashboard-nav-link.tsx` | The only existing left-rail; reuse its active/inactive link *tokens* (not its component — the booking rail is not navigation between routes). |
| `client/features/widgets/types/widget.ts`, `components/widget-type-picker.tsx`, `schemas/widget-schema.ts` | The enum cleanup. `z.enum(WIDGET_TYPES)` follows automatically. |

### Domain facts

| Source | Fact |
|---|---|
| `V3__create_enum_types.sql`, `V47__widgets.sql` | `widget.widget_type` has four values; `widgets.type NOT NULL DEFAULT 'EMBEDDED'`. V143 recreates it with three and restores the default. Only Java reference: the enum itself. |
| `core.attendees` (server `V32`) | `email VARCHAR(255)` nullable, `phone VARCHAR(50) NOT NULL`, `name VARCHAR(255) NOT NULL`, `locale` nullable enum, `timezone VARCHAR(100)` nullable, `no_show DEFAULT false`. → D10. |
| `core.bookings` | `title NOT NULL`, `description` nullable, `location` nullable, `status DEFAULT 'PENDING'`, `user_id NOT NULL` (host), `idempotency_key` nullable with **no** unique index. |
| `core/enums/BookingStatus` | `PENDING, ACCEPTED, REJECTED, AWAITING_HOST, CANCELLED`. The dashboard's Pending tab = `{PENDING, AWAITING_HOST}`; the slot engine blocks `{PENDING, ACCEPTED, AWAITING_HOST}`. `AWAITING_HOST` is the literal meaning of "needs host confirmation" → D7. |
| `BookingPolicyEntity` | `defaultDuration` (minutes; entity default **1**, provisioning sets 30), `autoConfirm` default `true`, `minimumNotice`/`maximumAdvanceBooking` minutes. |
| `OrganizationEntity` | `slug UNIQUE`, `isPrivate` default `false` but provisioning writes `true`, `timezone`, `weekStart` (`global/enums/WeekStart`: `SUNDAY`, `MONDAY`; default `MONDAY`), `logoUrl`/`bannerUrl`/`bio` nullable. |
| `ServiceEntity` | No `active`/`hidden` flag → D9. `slug` unique per organization. `timezone` is presentation; the engine uses `schedule.timezone`. `minPrice`/`maxPrice` nullable `DECIMAL(10,2)`, `currency` (`global/enums/Currency`: `USD, AUD, SGD, GBP, VND`, default `USD`), `location` nullable, `successRedirectUrl` nullable, `description` nullable. |
| `TenantEntity.status` | `ACTIVE, SUSPENDED, TRIAL, PENDING, ARCHIVED`. Provisioning writes `ACTIVE`. → D8 serves `ACTIVE`/`TRIAL` only. |
| `TenantRepository.findByOrganizationOrganizationId` → `Optional` | `V138` makes `tenants.organization_id` unique; V141 backfilled a tenant for every org. A missing tenant is a data fault → same 404. |
| `WorkspaceProvisioningServiceImpl.createOrganization` | `slug = name.toLowerCase().replace(" ", "") + "-organization"` — **not URL-normalised**. Not fixed here (§16); Escalation #6 if dev data hits it. |
| `JwtAuthenticationFilter` | No header → pass through. Bad Bearer → 401 on any path. |
| `SecurityConfig` | `permitAll` today: the seven `/api/v1/auth/*` POSTs, the Google callback GET, `/error`. Add one line. |
| `client/AGENTS.md` → "Architecture: the BFF hop" | "The browser never calls Spring Boot directly." `API_BASE_URL` is server-only. → D5. |
| `client/proxy.ts` | Matcher skips `api`; protected prefix `/dashboard`; guest-only `/login`, `/register`. `/{orgSlug}` passes through untouched. |
| Client root routes today | `/` (landing), `/login`, `/register`, `/google/complete`, `/me`, `/dashboard/**`, `/api/**`. → the reserved set in §9.6. |
| `client/package.json` | No date library, no test runner. `axios`, `zod` v4, `react-hook-form`, `@base-ui/react`, `lucide-react`, Tailwind v4, Geist via `next/font`. |
| `client/app/globals.css` | Tokens: `background #0b0d10`, `surface`/`card` `#12151a`, `surface-container`/`popover` `#1c1f26`, `surface-hover #2d333b`, `primary #3b82f6` / `primary-foreground #fff`, `muted-foreground #9ca3af`, `border`/`input #1f2329`, `ring #3b82f6`, `destructive #ffb4ab`, `success #4edea3`, `warning #ffb786`, `--radius 10px` (`rounded-lg`). Type scale: `display 48`, `headline-lg 32`, `headline-lg-mobile 24`, `headline-md 20/500`, `body-lg 16`, `body-md 14`, `label-md 12/500`, `mono-label 12`. Spacing: `base 4, xs 8, sm 12, md 16, lg 24, xl 32, 2xl 48, gutter 24`. |
| `ISSUE-0001` | "There is no consumer-side account: attendees book through a widget or public page without ever holding a platform account." This task is that page. |
| Spring Boot `server.forward-headers-strategy` | `native` → Tomcat `RemoteIpValve`, `internal-proxies` default `10/8, 192.168/16, 169.254/16, 127/8, 172.16/12, ::1`. `framework` → `ForwardedHeaderFilter`, trusts everyone. → Boundary. |

---

## 9. FUNCTIONAL REQUIREMENTS

### 9.1 Migration — V143 (done) and its mirror

V143 is authored. Phase A applies it by booting. The init mirror
`database/init/widget/V226__drop_widget_type_full_page.sql` carries the same
seven statements with comments explaining: the defensive `UPDATE` (zero rows
expected), why recreate (Postgres has no `DROP VALUE`), why `DROP DEFAULT`
must precede the type change, why the rename keeps the original type name.

After apply, `SELECT enum_range(NULL::widget.widget_type)` must return
`{INLINE,POPUP,EMBEDDED}` and `flyway_schema_history` must show `143` with
`success = true`.

### 9.2 Wire contract — the client types against this verbatim

```text
GET /api/v1/public/{orgSlug}
  200 PublicOrganizationResponse
      { name, slug, logoUrl, bannerUrl, bio, timezone,
        weekStart,                              ← "SUNDAY" | "MONDAY"; the calendar's first column
        services: [ PublicServiceSummary
          { slug, title, description, location, durationMinutes,
            minPrice, maxPrice, currency, autoConfirm } ] }   ← autoConfirm drives the "Instant booking" badge
  404 ApiErrorResponse   unknown/reserved slug · tenant missing · tenant not ACTIVE/TRIAL

GET /api/v1/public/{orgSlug}/service/{serviceSlug}
  200 PublicServiceResponse
      { organization: { name, slug, logoUrl, timezone, weekStart },
        slug, title, description, location,
        durationMinutes, minPrice, maxPrice, currency,
        scheduleTimezone,                       ← schedule.timezone, the engine's zone
        minimumNotice, maximumAdvanceBooking,   ← minutes, for the calendar bounds
        autoConfirm,                            ← "Instant confirmation" vs "Request"
        successRedirectUrl,                     ← nullable; enables "Return to website" on Done
        availableWeekdays: [1..7] }             ← union of availabilities.days, ISO
  404 as above · unknown service slug · service has no booking policy

GET /api/v1/public/{orgSlug}/service/{serviceSlug}/slots?date=YYYY-MM-DD
  200 AvailableSlotsResponse   { serviceId, date, timezone, slots: [{ slotStart, slotEnd }] }
      — the TASK-0009 record, unchanged; `date` is a calendar date in `timezone`
  400 malformed date (Spring default)
  404 as above

POST /api/v1/public/{orgSlug}/service/{serviceSlug}/bookings
  PublicBookingRequest
      { name       @NotBlank @Size(max = 255)
        phone      @NotBlank @Size(max = 50)
        email      @Size(max = 255)                  nullable
        timezone   @Size(max = 100)                  nullable, the visitor's display zone
        slotStart  @NotNull Instant                  must equal a generated slot start
        notes      (no annotation, TEXT)             nullable → bookings.description }
  201 PublicBookingResponse
      { bookingUid, status, startTime, endTime, timezone, serviceTitle, organizationName }
  400 validation (Spring default envelope)
  404 as above
  409 ApiErrorResponse   SlotUnavailableException — "That time is no longer available"
  429 ApiErrorResponse + Retry-After: <seconds>
```

Every `GET`/`POST` above is `permitAll`. Every response `Instant` serializes
as it does in `BookingResponse` (ISO-8601 `Z`); add no Jackson annotations.
`BigInteger`/`BigDecimal` serialize as JSON numbers (`client/AGENTS.md` →
"`oauthConnectionId` wire type"); type them `number`.

### 9.3 Rate limiter — `security/ratelimit/`

1. `RateLimiter` interface:
   `void check(String bucket, String key, int limit, Duration window)`.
2. `RedisRateLimiter` executes one Lua script through
   `StringRedisTemplate.execute(DefaultRedisScript<Long>, List.of(redisKey), windowSeconds)`:

   ```lua
   local current = redis.call('INCR', KEYS[1])
   if current == 1 then redis.call('EXPIRE', KEYS[1], ARGV[1]) end
   return current
   ```

   Key: `ratelimit:public:<bucket>:<key>`. If `current > limit`, read
   `getExpire(redisKey)` and throw `RateLimitExceededException(retryAfterSeconds)`
   (fallback to the window length if TTL is unavailable). Why Lua and not
   `increment` + `expire`: two commands leave a window where the key exists
   without a TTL, which turns a transient Redis failure into a permanent
   block for that IP.
3. `RateLimitProperties`:

   ```yaml
   app:
     rate-limit:
       public:
         read:  { limit: 120, window: PT1M }
         write: { limit: 5,   window: PT1M }
   ```

   `@Validated`, `@Positive` on limits, `@NotNull` on windows.
4. `PublicRateLimitInterceptor.preHandle`: return `true` for `OPTIONS`;
   bucket = `POST` → `write`, else `read`; key = `request.getRemoteAddr()`;
   call `check`. Registered in `WebConfig.addInterceptors(...)` with
   `.addPathPatterns("/api/v1/public/**")`.
5. `GlobalExceptionHandler` → 429, `ApiErrorResponse` shape, header
   `Retry-After: <seconds>`.
6. `application.yaml`: `server.forward-headers-strategy: native`. Nothing
   under `server.tomcat.remoteip` — the default `internal-proxies` covers the
   local BFF; production sets it to the real proxy range (document in
   `SERVER_AGENTS.md`).
7. **Fail-closed.** A `DataAccessException` from Redis propagates and surfaces
   as a generic 500. No try/catch, no "allow on error".

### 9.4 `PublicBookingServiceImpl`

Fields (`private final`, Lombok): `OrganizationRepository`,
`TenantRepository`, `ServiceRepository`, `BookingPolicyRepository`,
`AvailabilityRepository`, `UserRepository`, `BookingRepository`,
`AttendeeRepository`, `SlotService`.

Private helpers, each used by every public method:

```java
private OrganizationEntity resolveServedOrganization(String orgSlug) {
    if (ReservedOrganizationSlugs.isReserved(orgSlug)) throw notFound(orgSlug);
    OrganizationEntity organization = organizationRepository.findBySlug(orgSlug)
        .orElseThrow(() -> notFound(orgSlug));
    TenantEntity tenant = tenantRepository
        .findByOrganizationOrganizationId(organization.getOrganizationId())
        .orElseThrow(() -> notFound(orgSlug));
    if (!SERVED_TENANT_STATUSES.contains(tenant.getStatus())) throw notFound(orgSlug);
    return organization;
}
// SERVED_TENANT_STATUSES = Set.of(TenantStatus.ACTIVE, TenantStatus.TRIAL)
// notFound(slug) = new OrganizationNotFoundException("Organization not found: " + slug)
//   — one message for every reason, on purpose (D8).

private ServiceEntity resolveService(OrganizationEntity organization, String serviceSlug)
//   findByOrganizationOrganizationIdAndSlug(...).orElseThrow(ServiceNotFoundException)

private BookingPolicyEntity resolvePolicy(ServiceEntity service)
//   findByServiceServiceId(...).orElseThrow(() -> new ServiceNotFoundException(...))
//   — a service without a policy is "not bookable", reported as not found (D9),
//     NOT as BookingPolicyNotFoundException (that message would reveal internals).
```

**`getOrganization(orgSlug)`** — `@Transactional(readOnly = true)`. Resolve
organization; `serviceRepository.findByOrganizationOrganizationId`; collect
ids; `bookingPolicyRepository.findByServiceServiceIdIn(ids)` → map by service
id; build one `PublicServiceSummary` per service **that has a policy**
(`durationMinutes = policy.getDefaultDuration()`, `autoConfirm = policy.getAutoConfirm()`),
ordered by `title`. `weekStart = organization.getWeekStart()`.

**`getService(orgSlug, serviceSlug)`** — `@Transactional(readOnly = true)`.
Resolve organization, service, policy;
`availabilityRepository.findByScheduleScheduleId(service.getSchedule().getScheduleId())`
→ `availableWeekdays` = sorted distinct union of `days`;
`scheduleTimezone = service.getSchedule().getTimezone()`;
`successRedirectUrl = service.getSuccessRedirectUrl()`.

**`getSlots(orgSlug, serviceSlug, date)`** — `@Transactional(readOnly = true)`.
Resolve organization, service; return
`slotService.getAvailableSlots(service.getServiceId(), date)` unchanged.

**`createBooking(orgSlug, serviceSlug, request)`** — `@Transactional`. In
this order, and the order is the point:

```java
OrganizationEntity organization = resolveServedOrganization(orgSlug);
ServiceEntity service = resolveService(organization, serviceSlug);
BookingPolicyEntity policy = resolvePolicy(service);

// (captcha hook point — the follow-up task verifies the token here, before the lock)

// Lock the host row first: conflicts are host-wide (TASK-0009 D3), so two
// visitors booking two different services of the same host must serialize
// here, before either of them reads the engine.
UserEntity host = userRepository.findLockedByUserId(service.getUser().getUserId())
    .orElseThrow(() -> new OrganizationNotFoundException("Organization not found: " + orgSlug));

ZoneId zone = ZoneId.of(service.getSchedule().getTimezone());
LocalDate date = request.slotStart().atZone(zone).toLocalDate();
Instant slotEnd = request.slotStart().plus(policy.getDefaultDuration(), ChronoUnit.MINUTES);

AvailableSlotsResponse offered = slotService.getAvailableSlots(service.getServiceId(), date);
if (!offered.slots().contains(new TimeSlot(request.slotStart(), slotEnd))) {
    throw new SlotUnavailableException("That time is no longer available");
}

BookingEntity booking = bookingRepository.save(BookingEntity.builder()
    .user(host)
    .service(service)
    .title(service.getTitle() + " with " + request.name())
    .description(request.notes())
    .location(service.getLocation())
    .startTime(request.slotStart())
    .endTime(slotEnd)
    .status(Boolean.TRUE.equals(policy.getAutoConfirm()) ? BookingStatus.ACCEPTED : BookingStatus.AWAITING_HOST)
    .build());

attendeeRepository.save(AttendeeEntity.builder()
    .booking(booking)
    .name(request.name())
    .phone(request.phone())
    .email(request.email())
    .timezone(request.timezone())
    .build());

return new PublicBookingResponse(booking.getBookingUid(), booking.getStatus(),
    booking.getStartTime(), booking.getEndTime(), zone.getId(),
    service.getTitle(), organization.getName());
```

Format every multi-parameter call per
`.skills/technologies/spring-boot/method-declaration`; the sketch is compressed.

### 9.5 Repositories

```java
// OrganizationRepository
Optional<OrganizationEntity> findBySlug(String slug);

// ServiceRepository
Optional<ServiceEntity> findByOrganizationOrganizationIdAndSlug(BigInteger organizationId, String slug);

// BookingPolicyRepository
List<BookingPolicyEntity> findByServiceServiceIdIn(Collection<BigInteger> serviceIds);

// UserRepository
@Lock(LockModeType.PESSIMISTIC_WRITE)
Optional<UserEntity> findLockedByUserId(BigInteger userId);
```

`@Lock` is `org.springframework.data.jpa.repository.Lock`; `LockModeType` is
`jakarta.persistence`. Spring Data ignores the word `Locked` between `find`
and `By` — it is there so the call site reads as what it is. Boot once after
adding these (Phase C) so a derived-path failure is isolated (Escalation #5).

### 9.6 Reserved organization slugs

`organization/organizations/ReservedOrganizationSlugs`:

```java
public final class ReservedOrganizationSlugs {
    // Root paths the client app owns. An organization slugged "login" would
    // have an unreachable public page — Next.js resolves static routes first.
    // Add here when adding a top-level client route.
    private static final Set<String> RESERVED = Set.of(
        "api", "dashboard", "login", "register", "google", "me",
        "widget", "widgets", "admin", "settings", "public", "book",
        "_next", "favicon.ico", "robots.txt", "sitemap.xml"
    );
    public static boolean isReserved(String slug) { return slug != null && RESERVED.contains(slug.toLowerCase()); }
}
```

- `OrganizationServiceImpl.createOrganization` / `updateOrganization`: if
  reserved → `DuplicateOrganizationException("Slug is reserved: " + slug)`
  (409 — it conflicts with a platform route; no new exception type).
- `WorkspaceProvisioningServiceImpl.createOrganization`: treat reserved
  exactly like `existsBySlug` → fall back to the uid slug.
- `PublicBookingServiceImpl.resolveServedOrganization`: reserved → 404
  (belt and braces; the client never routes these here anyway).

### 9.7 Client architecture

**Routes** (`app/(booking)/`, own `layout.tsx` — renders `BookingShell`'s
static parts; no `MarketingHeader`; `GridBackground` is already in the root
layout and is the canvas):

- `[organizationSlug]/page.tsx` — Server Component. `const { organizationSlug } = await params`.
  Calls `fetchPublicOrganization(slug)`; `notFound()` on 404. Renders the
  shell with `activeStep = "service"` and `ServicePicker` as content (§9.11
  step 1). `generateMetadata`: `"{org.name} — Book an appointment"`.
- `[organizationSlug]/[serviceSlug]/page.tsx` — Server Component. Calls
  `fetchPublicService(org, service)`; `notFound()` on 404. Renders the shell
  with `BookingFlow` (`"use client"`, receives the `PublicService`), which
  owns steps 2–6. `generateMetadata`: `"{service.title} · {org.name}"`.
- `not-found.tsx` — the shell's main column with headline "This booking page
  isn't available." and body "Check the link you were given, or contact the
  business directly." No rail (there is no organization to show).

**BFF Route Handlers** (`app/api/public/[organizationSlug]/service/[serviceSlug]/`):

- `slots/route.ts` `GET`: validate `date` (`/^\d{4}-\d{2}-\d{2}$/`) with zod
  → 400 on failure; call `fetchPublicSlots(org, service, date, forwardedFor(request))`;
  return the platform body. Statuses pass through (400/404/429).
- `bookings/route.ts` `POST`: parse body with `bookingRequestSchema` (UX
  guard — the platform re-validates), call `createPublicBooking(...)`, return
  201 + body. Pass through 400/404/409/429 and copy `Retry-After` when present.
- Both: **no `cookies()`, no `withAccessToken`, no `bearer()`**.
  `forwardedFor(request)` returns `{ "X-Forwarded-For": value }` only when
  `request.headers.get("x-forwarded-for")` exists (set by the hosting proxy
  in production; absent in `next dev`).

**Gateway** (`services/public-booking-gateway.ts`, server-only): four
functions over `platformClient()`; 404 → `PublicPageNotFoundError`; 409 →
`SlotUnavailableError`; 429 → `RateLimitedError(retryAfterSeconds)`.
**Browser API** (`services/public-booking-api.ts`): `getSlots`,
`createBooking` over `bffClient`. **Errors**
(`services/public-booking-errors.ts`): `normalizePublicBookingError` mapping
the four statuses to copy (§9.11 step 5).

**Types** (`types/public-booking.ts`): `PublicOrganization`,
`PublicServiceSummary`, `PublicService`, `AvailableSlots`, `TimeSlot`,
`PublicBookingRequest`, `PublicBooking`, `WeekStart`, `Currency` — field for
field from §9.2.

**Widget enum cleanup**: `WIDGET_TYPES = ["INLINE", "POPUP", "EMBEDDED"]`,
labels and picker options follow; `SUPPORTED_WIDGET_TYPES` stays `["INLINE"]`.

### 9.8 The MVP v2 port — what each carried piece becomes

**`lib/date-math.ts`** — see §9.12 for the algorithms. Exports:
`toDateString(date)`, `zonedParts(instant, zone)`, `localDateInZone(instant, zone)`,
`startOfDayInZone(dateString, zone)`, `endOfDayInZone(dateString, zone)`,
`todayInZone(zone)`, `addDays(dateString, n)`, `compareDates(a, b)`,
`weekdayOf(dateString)` (ISO 1–7), `monthGrid(year, month, weekStartsOn)`,
`scheduleDatesFor(localDate, visitorZone, scheduleZone)`,
`formatSlotTime(iso, zone)`, `formatLongDate(dateString, zone)`,
`formatShortDate(dateString)`, `formatZoneLabel(zone)`, `weekStartsOnFrom(weekStart)`.

**`lib/format.ts`** —

```ts
export function formatDuration(minutes: number): string   // MVP verbatim: "45 min", "1 hr", "1 hr 30 min"
export function formatPriceRange(min: number | null, max: number | null, currency: Currency): string
// both null/0 → "Free"; equal → formatMoney(min); else `${formatMoney(min)} – ${formatMoney(max)}`
// formatMoney uses Intl.NumberFormat(undefined, { style: "currency", currency, maximumFractionDigits: currency === "VND" ? 0 : 2 })
```

The MVP hard-coded `"vi-VN"` and `maximumFractionDigits: 0`; the platform has
five currencies, so the locale is the browser's and only VND drops decimals.

**`hooks/use-available-slots.ts`** — the MVP `useSlots` shape with the D11
fan-out:

```ts
export function useAvailableSlots(input: {
  organizationSlug: string; serviceSlug: string;
  localDate: string | null;      // YYYY-MM-DD in visitorZone
  visitorZone: string; scheduleZone: string;
}): { slots: TimeSlot[]; loading: boolean; error: string | null; refresh: () => void }
```

- `scheduleDates = scheduleDatesFor(localDate, visitorZone, scheduleZone)` (≤ 2).
- `requestKey = localDate ? `${org}|${svc}|${scheduleDates.join(",")}|${localDate}|${visitorZone}` : null`.
- State `{ key, slots, error }`; effect keyed on `requestKey` with the MVP's
  `active` flag; `Promise.all(scheduleDates.map(d => getSlots(org, svc, d)))`
  → concat → filter `localDateInZone(slot.slotStart, visitorZone) === localDate`
  → sort by `slotStart` → dedupe by `slotStart`.
- `loading = state.key !== requestKey` (derived, as in the MVP — no separate
  boolean that can go stale). `refresh()` bumps a counter folded into the key.
- Error copy: "Unable to load available times." (MVP).

**`components/booking-flow.tsx`** — the MVP `BookingWidget` shape:

```ts
type Step = "date" | "time" | "details" | "review" | "done";
const [step, setStep] = useState<Step>("date");
const [visitorZone, setVisitorZone] = useState(() => Intl.DateTimeFormat().resolvedOptions().timeZone);
const [selectedDate, setSelectedDate] = useState<string | null>(null);      // YYYY-MM-DD in visitorZone
const [selectedSlot, setSelectedSlot] = useState<TimeSlot | null>(null);
const [details, setDetails] = useState<BookingDetails | null>(null);
const [submitting, setSubmitting] = useState(false);
const [submitError, setSubmitError] = useState<string | null>(null);
const [result, setResult] = useState<PublicBooking | null>(null);
```

Selecting a date clears the slot (MVP). Changing the zone keeps the date
string but the hook recomputes. `reset()` returns to `"date"` with everything
cleared — used by "Book another"? No: "Book another" navigates to
`/{orgSlug}` (step 1); `reset()` exists for the 409 path (§9.11 step 5).
Each step renders only when its prerequisites exist
(`step === "time" && selectedDate`, …), the MVP guard pattern.

**`schemas/booking-details-schema.ts`** — the MVP `customerSchema`, renamed:

```ts
const phoneRegex = /^(\+?\d{9,15})$/;   // MVP: VN mobile + E.164-ish
export const bookingDetailsSchema = z.object({
  name:  z.string().trim().min(1, { error: "Name is required" }).max(255, { error: "Name is too long" }),
  phone: z.string().trim().min(1, { error: "Phone number is required" }).max(50, { error: "Phone number is too long" }).regex(phoneRegex, { error: "Enter a valid phone number" }),
  email: z.union([z.literal(""), z.email({ error: "Enter a valid email" }).max(255)]).optional(),
  notes: z.string().trim().max(1000).optional(),
});
```

zod v4 syntax (`z.email()`, `{ error }`), per `client/AGENTS.md`. `""` email
→ `undefined` on the wire (MVP behaviour).

**`components/booking-details-form.tsx`** — react-hook-form + `zodResolver`
(the client's standard; the MVP used manual `safeParse` — the *rules* carry,
the mechanism follows the codebase). Submit → `setDetails(values)`; `setStep("review")`.

**`components/slot-list.tsx`** — MVP states (loading → `Spinner`, error →
`ErrorMessage`, empty → copy, list) plus grouping (§9.11 step 3). Selection
identity by `slotStart` (MVP).

**`components/spinner.tsx`, `error-message.tsx`** — MVP props; tokens:
spinner `border-border border-t-primary`, error `border-destructive/40
bg-destructive/10 text-destructive`.

### 9.9 The Stitch design — screen by screen, demo version

The mocks are the reference; the **Demo simplifications** below override
them wherever the two disagree. Class names quoted are the mock's; the
client equivalent is in §9.10.

**Demo simplifications (binding — D20).** Where a per-step paragraph below
still describes the richer Stitch treatment, this list wins:

| Area | Stitch | Demo |
|---|---|---|
| Motion | selection indicator scales in; progress bar animates; success check has a `animate-ping` ring; pills/cards transition | **`transition-colors` only.** No ping, no scale, no fade. `prefers-reduced-motion` handling is therefore moot. |
| Step 1 selection | select a card → check circle → sticky gradient "Continue" | **Clicking a card navigates** to `/{orgSlug}/{serviceSlug}` (MVP behaviour). No selection state, no check circle, no Continue button, no gradient. |
| Step 1 badge | "Popular", "Instant Booking" | Only the **"Instant booking"** chip for `autoConfirm` services; plain `bg-primary/15 text-primary` pill in the card's top-right. |
| Slot container overlay | gradient overlay, deep shadow | Plain `bg-surface border rounded-xl`. No overlay, standard `shadow-sm`. |
| Details inputs | leading icon inside each input | **No icons inside inputs.** Label above, plain input (`components/ui/input.tsx`), error below. |
| Review "Edit" links | hover-revealed | **Always visible** (`label-md text-primary`), top-right of each card. |
| Review watermark icon | 64 px icon at 10 % in the Service card | Omitted. |
| Done illustration | primary circle + glow + ping | Primary circle with the check, no glow, no ping. |
| Done "Return to Website" | always shown | Only when `successRedirectUrl` is set (unchanged). |
| Done "Add to Google Calendar" | button | Kept — it is a plain `<a target="_blank">` to the template URL (§9.7). |
| Rail "Need Help?", footer links | present | Omitted (unchanged). |
| Timezone control | globe + label line | The same line rendered as a **native `<select>`** (`components/form/native-select.tsx`) with the zone label as its visible value. No custom dropdown. |
| Focus management | title focus on step change | None. Native controls only (§6 #20). |
| Eyebrow placement | varies per mock | "STEP n OF 6" above the title on every step. |
| Content width | varies per mock (480/600/672) | **One width: `max-w-[600px]`** for every step. |
| Rail rounding | `rounded-r-xl` on some mocks | None. |

Everything not listed here follows the per-step paragraphs.

**The shell (steps 2–7 mocks, used for all six steps — D18).**

- Canvas: `bg-background`. Desktop (`md:`): a fixed left rail `w-64` (256 px),
  full height, `bg-surface-container` in most mocks (`surface-container-low`
  in step 4), `p-md gap-stack_gap`, right border `border-surface-container-low`
  / `outline-variant`, `rounded-r-xl` on some — **use no rounding**; the rail
  meets the viewport edge. Main column `md:ml-64`, `min-h-screen`,
  `overflow-y-auto`.
- Rail header: 40–48 px round avatar (business logo; `storefront` icon
  placeholder when there is none — step 7 mock) + name (`headline-md`) +
  tagline (`label-sm` muted). Tagline = `bio` on one line (`truncate`), or
  nothing.
- Rail step list: six rows `py-2 px-2 rounded-lg gap-md`, icon 20 px + label
  (`label-md`). States (step 3 mock is canonical): *inactive* muted text,
  hover `bg-surface-variant text-on-surface`; *active* `text-primary
  font-bold border-l-2 border-primary pl-2 bg-surface-variant/50`, icon
  filled; *done* inactive colours plus a 16 px primary `check` at the far
  right (`ml-auto`); *future* not clickable; "Done" additionally
  `opacity-50 cursor-not-allowed` until reached (steps 3, 6 mocks). Done
  steps are clickable and go back to that step (Service → `/{orgSlug}`).
- Rail bottom: the mocks put a "Need Help?" outlined button there — **omit**
  (no support surface exists); leave the space empty.
- Progress bar: `h-1` (4 px) full-width at the top of the main column,
  track `bg-surface-hover`, fill `bg-primary`, width = `step / 6 * 100%`.
  No transition (demo).
- Mobile (`< md`): rail hidden. A sticky top bar `px-lg py-md border-b`:
  the mocks show the "PleaseBookMe" wordmark + account/help icons; **ours**
  shows a back chevron (when a previous step exists) + the business name
  (`headline-md`). Progress bar directly under it. Content column
  `max-w-[480px] mx-auto px-container_padding` (20 px) `py-xl`.
- Footer: desktop mocks show "Powered by PleaseBookMe · Privacy · Terms ·
  Support" under the content (step 2) or fixed at the bottom (step 4);
  mobile step 3 shows a centered "Powered by PleaseBookMe". **Ours**: a
  single centered `label-sm` muted "Powered by PleaseBookMe" at the bottom of
  the main column on every step, no links.
- Step header (steps 2–7): optional `arrow_back` "Back" text link (`label-sm`
  muted, hover foreground) above; title (`display` 24 px, `md:` 32/40 in step
  4); one-line subtitle (`body-md` or `body-lg` muted, `mt-xs`). Step 1 uses
  an eyebrow above the title instead ("STEP 1 OF 7", `label-sm text-primary
  uppercase tracking-wider`) and no subtitle; step 4 puts the eyebrow *below*
  the card. **Ours**: eyebrow "STEP n OF 6" above the title on every step,
  subtitle where the mock has one.
- Content width: 480 px for Service/Time/Details, 600 px for Date/Done,
  `max-w-2xl` (672 px) for Review — as the mocks.

**Step 1 — Choose a Service (`step_1_select_service`).**

- Header: eyebrow + "Choose a Service". No subtitle.
- Cards: vertical list `gap-md`. Each card `bg-surface rounded-xl border
  border-border shadow-sm`, hover `border-muted-foreground/60 bg-surface-hover`.
  The mock's 128 px cover image block is **omitted** (no image column); the
  card is the text block only: `p-md flex justify-between items-start` —
  left: title (`headline-sm`) and a row `Clock` icon 16 px + "45 min"
  (`body-md` muted) and, ours, the `description` (`body-md` muted, 2-line
  clamp) when present; right: price (`headline-md`) or "Free".
- Badge: the mock's "Popular" has no data — omit. "Instant booking" (`Zap`)
  renders when `autoConfirm` is true, `bg-primary/15 text-primary`, in the
  card's top-right.
- Empty: "No services available for this business yet." (MVP copy) in a
  bordered panel.
- Action: **none** in the demo (see "Demo simplifications"). Each card is a
  Next.js `<Link href="/{orgSlug}/{serviceSlug}">` styled as the card;
  clicking it navigates. The mock's selection indicator and sticky gradient
  Continue are deferred (§16).

**Step 2 — Select a Date (`step_3_select_date`).**

- Header: "Select a Date" / "Choose when you'd like to visit."
- Under the header, ours: `SelectedServiceStrip` — the MVP's footer strip
  moved up: "{title} · {duration} · {price}" (`body-md` muted, `mt-sm`).
- Calendar card: `bg-surface rounded-xl border border-border p-md md:p-lg
  shadow-sm`.
  - Header row `flex justify-between items-center mb-lg`: two 40 px round
    icon buttons (`chevron_left` / `chevron_right`, muted, hover
    `bg-surface-variant text-on-surface`) and the month label centered
    (`headline-sm`, "October 2023"). Previous is disabled (30 % opacity) when
    the month is the current month in the visitor zone; next is disabled when
    the month's first day is past `today + maximumAdvanceBooking`.
  - Weekday row: seven single letters (`label-sm` muted, `py-2`), starting on
    `weekStart` (`S M T W T F S` for SUNDAY, `M T W T F S S` for MONDAY —
    the MVP's `weekStartsOnFromTeam`).
  - Day grid: `grid grid-cols-7 gap-y-sm gap-x-xs text-center`, always six
    rows (MVP `fixedWeeks`), outside-month days rendered (MVP
    `showOutsideDays`). Cells `h-10 w-full flex items-center justify-center
    body-md`. States, exactly as the mock:
    - *unavailable* (past, beyond the advance window, weekday not in
      `availableWeekdays`, or outside the month): `text-on-surface-variant
      opacity-30`, not focusable, no hover.
    - *available*: `text-on-surface cursor-pointer hover:bg-surface-variant
      rounded-full`.
    - *today*: available styling plus a `w-4 h-[2px] bg-primary rounded-full`
      bar at `bottom-1` (the "subtle underline" in DESIGN.md).
    - *selected*: `bg-primary text-on-primary rounded-full shadow-md
      font-semibold`.
  - Timezone line: `pt-4 border-t` centered, `Globe` icon 16 px + a native
    `<select>` (`components/form/native-select.tsx`) whose options are
    `Intl.supportedValuesOf("timeZone")` and whose visible value is
    `formatZoneLabel(zone)` = `"{zone} ({shortOffset})"` via
    `timeZoneName: "shortOffset"`. Styled as text (`label-sm` muted), not as
    a boxed control.
- Action bar `mt-xl flex gap-md`: "Back" (`flex-1`, outlined: `border
  border-outline-variant text-on-surface hover:bg-surface-variant py-3
  rounded-lg`) and "Continue to Time" (`flex-[2]`, primary, `shadow-md`).
  Back → `/{orgSlug}`. Continue is disabled until a date is selected.
- Mobile: "Powered by PleaseBookMe" below the actions (shell footer covers it).

**Step 3 — Select Time (`step_4_select_time`).**

- Header: "Select Time" (`display`, `md:text-[32px] md:leading-[40px]`) and
  a subtitle row `calendar_month` icon 18 px + the selected date as
  "Tuesday, Oct 24" (`body-lg` muted) — `formatLongDate` short form.
- Slot container: `bg-surface rounded-xl border border-border p-lg flex
  flex-col gap-xl shadow-sm`. No gradient overlay, no deep shadow (demo).
- Groups: "Morning" (`Sun`, `text-warning`), "Afternoon" (`CloudSun`,
  `text-warning`), "Evening" (`Moon`, `text-primary`). Each: header
  `headline-sm` with the icon, then `grid grid-cols-2 gap-md` of pills.
  Boundaries in the **visitor zone**: Morning `< 12:00`, Afternoon `12:00–16:59`,
  Evening `≥ 17:00`. A group with no slots is not rendered. All empty →
  "No available times for this day." (MVP copy) centered in the container.
- Pill: native `<button>`, `h-12 rounded-full border border-border bg-surface
  text-foreground text-body-md font-medium`, hover `border-primary
  text-primary`. *Selected*: `bg-primary text-primary-foreground
  border-primary` + trailing `Check` 16 px. No glow, no offset ring (demo);
  the browser's default focus ring is fine. Label from
  `formatSlotTime(slotStart, visitorZone)` — `Intl` with `hour: "numeric",
  minute: "2-digit"` (12- or 24-hour per the browser locale; never the mock's
  "14:30 PM").
- Loading: `Spinner` "Loading times…" in the container; error: `ErrorMessage`.
- In-card actions `mt-md pt-lg border-t flex justify-between`: text "← Back"
  (`ArrowLeft` 18 px, muted, hover foreground) and "Continue →" (primary,
  `px-lg py-3 rounded-lg min-w-[120px]`, `ArrowRight` 18 px), disabled
  until a slot is selected.
- Eyebrow stays in the header ("STEP 3 OF 6"); the mock's below-card
  placement is not used. The ambient blob is omitted — `GridBackground` is
  the canvas.

**Step 4 — Your Details (`step_5_customer_info`).**

- Header: "Your Details" / "Please provide your contact information to
  finalize the booking." (`body-lg` muted). Above it, ours: the selected
  slot as a chip — the MVP's "`{service} with {staff}`" banner becomes
  "{Weekday, Mon D} · {time} ({zone})" in `bg-primary/10 text-primary
  rounded-lg px-3 py-2 body-md`.
- Form `flex flex-col gap-lg`. Field anatomy (demo — no icons inside
  inputs): `components/ui/label.tsx` (`text-body-md font-medium`, `mb-xs`),
  then `components/ui/input.tsx` (the client's standard input — its own
  border/focus tokens apply; do not restyle it), then the inline error
  (`text-label-md text-destructive`, `mt-xs`). The error state adds
  `aria-invalid` and `border-destructive` on the input, the way
  `service-basics-panel.tsx` does it.
- Fields and order — **adapted for D10** (mock order is Name, Email, Phone,
  Notes): 1. "Full Name" (placeholder "Jane Doe"), 2. "Phone Number"
  (`type="tel"`, placeholder "0912 345 678"), 3. "Email Address" with a
  right-aligned muted "Optional" (`type="email"`, placeholder
  "jane@example.com"), 4. "Notes" with "Optional" (native `<textarea>`
  styled like the input, `rows=3 resize-none`, placeholder "Any special
  requests or details we should know?").
- Actions `flex gap-md pt-md mt-md border-t border-outline-variant/30`:
  "Back" (`w-1/3`, outlined) and "Continue to Review" (`w-2/3`, primary,
  `font-bold shadow-lg shadow-primary/20`).
- Submit validates with `bookingDetailsSchema`; on success stores `details`
  and moves to Review. No network call here (D15).

**Step 5 — Review Booking (`step_6_review_booking`).**

- Header: "Review Booking" / "Please review your appointment details before
  confirming."
- Bento grid `grid grid-cols-1 md:grid-cols-2 gap-md mb-xl`:
  - **Service** card (`bg-surface border rounded-xl p-md`): eyebrow "SERVICE"
    (`label-sm` muted uppercase tracking-wider), title (`headline-sm`), price
    (`body-lg text-primary mt-1`, "Free" when none). No watermark icon
    (demo). An always-visible "Edit" link (`Pencil` 14 px + text,
    `text-label-md text-primary`) top-right → `/{orgSlug}`. Every card below
    carries the same always-visible Edit link.
  - **Date & Time** card: eyebrow, "Oct 24, 2:30 PM" (`headline-sm`) in the
    visitor zone, then `schedule` 16 px + duration (`body-md` muted), then
    ours: the zone label (`label-sm` muted). Edit → step `date`.
  - **Professional** card (`md:col-span-2`, avatar + name): **dropped** —
    nothing on the platform identifies a person to the visitor. In its place
    ours renders a **Location** card (`md:col-span-2`, `location_on` +
    `service.location`) only when `location` is set.
  - **Your Details** card (`md:col-span-2`, `bg-surface-container-low`):
    eyebrow "YOUR DETAILS", name (`body-lg font-medium`), phone and email
    (`body-md` muted, one per line), notes (`body-md` muted, 2-line clamp)
    when given. Edit → step `details`.
- Action area `mt-auto pt-lg border-t flex flex-col gap-md`:
  - Row: "Total due today" / "$45.00" in the mock. **Ours**: label "Price"
    (`body-lg font-medium`) and the price range or "Free" (`headline-sm`) —
    no payment is collected, so "due today" would be a lie.
  - "Confirm Booking" (`w-full bg-primary text-white py-3 px-6 rounded-lg`,
    trailing `arrow_forward` 20 px, `shadow-lg shadow-primary/20`), label
    "Booking…" while `submitting`, disabled while submitting or throttled.
  - Fine print (`label-sm` muted centered, 70 % opacity): the mock's "By
    confirming, you agree to our cancellation policy." is **replaced** with
    the `autoConfirm`-aware line: "You'll get your confirmation right away."
    or "{org.name} will confirm your request."
- Submit → `POST` via `createBooking(orgSlug, serviceSlug, { name, phone,
  email, notes, timezone: visitorZone, slotStart: selectedSlot.slotStart })`.
  - 201 → `setResult`, step `done`.
  - 409 → `submitError` "That time was just taken — please pick another.",
    `refresh()` slots, step `time`, selection cleared.
  - 429 → `submitError` "Too many attempts. Try again in {n}s." with a
    countdown from `Retry-After`; button disabled until it reaches 0.
  - 400 → "Please check your details and try again." and step `details`.
  - 404 → `router.refresh()` (the page will `notFound()`).
  - Anything else → "Unable to complete the booking. Please try again." (MVP).

**Step 6 — Booking Confirmed (`step_7_booking_success`).**

- Centered column `max-w-[600px]`, `text-center`.
- Illustration: 96 px `bg-primary rounded-full` with a 48 px `Check` in
  `text-primary-foreground`. No glow, no ping ring (demo).
- Headline/subline by status: `ACCEPTED` → "Booking Confirmed!" / "Your
  appointment has been successfully scheduled."; `AWAITING_HOST` → "Request
  Sent" / "{org.name} will confirm your appointment shortly."
- Summary box (`bg-surface-container rounded-xl border border-border p-lg
  flex flex-col gap-md text-left shadow-sm`):
  - Row "Booking Reference" / "#PBM-9823" (`label-md`, value `text-primary
    font-bold`). Ours: `"#PBM-" + bookingUid.slice(0, 8).toUpperCase()`.
    Divider `border-b border-outline-variant/50 pb-md`.
  - Row: 48 px `bg-surface-variant rounded-lg` icon tile (`content_cut` →
    ours `calendar-check`), then service title (`headline-sm`) and "with
    {org.name}" (`body-md` muted). (The mock says "with Alex Johnson" — the
    *customer*; for the customer reading it, the business is the meaningful
    counterparty.)
  - Rows with 18 px muted icons: `calendar_today` + "Thursday, October 24,
    2024" (`formatLongDate` full form, visitor zone); `schedule` + "2:00 PM –
    3:00 PM (1 hour)" (start–end in the visitor zone + `formatDuration`);
    `location_on` + `service.location` when set.
- Actions `flex flex-col gap-md`:
  - "Return to Website" (`w-full h-12 bg-primary text-white rounded-lg`) —
    **only when `successRedirectUrl` is set**; navigates there
    (`window.location.assign`, it is the tenant's site).
  - Two-up grid: "Add to Google Calendar" (`event` 18 px, outlined `h-12`)
    → opens `googleCalendarUrl(...)` in a new tab; "Book Another" (`add_circle`
    18 px, outlined) → `/{orgSlug}`.
- Rail: "Done" active; every other step ticked; none clickable.

### 9.10 Token, type, icon and spacing mapping — Stitch → client

**Colours.** Stitch tokens are Material tonal names; the client's are
shadcn/Obsidian names. Map, never copy.

| Stitch class / hex | Meaning in the mock | Client utility |
|---|---|---|
| `bg-background` `#111317`, body `#0B0D10` | canvas | `bg-background` (`#0b0d10`) |
| `bg-surface` `#111317` | card, calendar card, slot pill body | `bg-surface` (`#12151a`) |
| `bg-surface-container-low` `#1a1c1f` | service card, slot container, "Your details" card | `bg-surface` — same level as above; the mock's two near-identical darks collapse to one |
| `bg-surface-container` `#1e2023` | rail, success summary box | `bg-surface-container` (`#1c1f26`) |
| `bg-surface-container-lowest` `#0c0e11` | input background | `bg-background` |
| `bg-surface-container-high` `#282a2d`, `bg-surface-variant` `#333538` | progress track, hover fills, icon tiles | `bg-surface-hover` (`#2d333b`) |
| `bg-surface-variant/30`, `/50` | active rail row | `bg-surface-hover/40` |
| `text-on-surface` `#e2e2e6`, `text-on-background` | primary text | `text-foreground` |
| `text-on-surface-variant` `#c2c6d6`, `text-outline` `#8c909f` | secondary text, icons | `text-muted-foreground` |
| `border-outline-variant` `#424754`, `border-surface-container-high/low` | every border | `border-border` (`#1f2329`) — the client's borders are subtler by design ("borders are the primary method of separation") |
| `border-outline` `#8c909f` (hover) | input hover | `hover:border-muted-foreground/60` |
| `bg-[#3B82F6]` / `text-white` | CTAs, progress fill in step 7 | `bg-primary text-primary-foreground` |
| `bg-primary` `#adc6ff` / `text-on-primary` `#002e6a` | selected day, selected pill, check circle, progress fill, "Any Available" | **also** `bg-primary text-primary-foreground` — DESIGN.md prose: one Primary, `#3B82F6` |
| `text-primary` `#adc6ff` | eyebrow, edit links, active rail row, reference number | `text-primary` |
| `bg-primary/5`, `/10`, `/20` | pill hover, chips, ping ring | `bg-primary/5`, `/10`, `/20` |
| `ring-primary` | focus | `ring-ring` |
| `bg-tertiary-container` `#df7412`, `text-tertiary` `#ffb786`, `text-tertiary-fixed-dim` | "Popular" badge, star, Morning icon | `text-warning` (`#ffb786`); badge `bg-warning/15 text-warning` (unused — no "Popular") |
| `bg-secondary-container` `#3131c0`, `text-on-secondary-container` `#b0b2ff`, `text-secondary-fixed-dim` | "Instant Booking" badge, Evening icon | `bg-primary/15 text-primary`; Evening icon `text-primary` |
| `text-error` `#ffb4ab`, `bg-error-container` | validation | `text-destructive`, `border-destructive`, `bg-destructive/10` |
| success (DESIGN.md "Green") | — (the mock uses primary for the check) | `text-success` reserved; the check circle stays `bg-primary` as drawn |

**Typography.** Stitch = Inter; client = Geist (`font-sans`). Keep Geist.

| Stitch | px / lh / wt | Client utility |
|---|---|---|
| `display` | 24 / 32 / 600 / −0.02em | `text-headline-lg-mobile` (24/32/600) |
| `display` at `md:text-[32px] leading-[40px]` (step 4) | 32 / 40 | `md:text-headline-lg` (32/40/600) |
| `display-mobile` | 20 / 28 / 600 | `text-headline-md font-semibold` |
| `headline-md` | 20 / 28 / 600 | `text-headline-md font-semibold` (client weight is 500 — add `font-semibold`) |
| `headline-sm` | 18 / 26 / 600 | **`text-headline-sm`** — new in `globals.css`: `--text-headline-sm: 18px; --text-headline-sm--line-height: 26px; --text-headline-sm--font-weight: 600;` |
| `body-lg` | 16 / 24 / 400 | `text-body-lg` |
| `body-md` | 14 / 20 / 400 | `text-body-md` |
| `label-md` | 14 / 20 / 500 | `text-body-md font-medium` |
| `label-sm` | 12 / 16 / 600 / +0.05em | `text-label-md font-semibold tracking-wider` (+ `uppercase` where the mock is upper) |
| `text-[10px] uppercase font-bold tracking-wider` (FASTEST pill) | — | unused |

**Radius.** Stitch: cards `rounded-xl` = 12 px, buttons `rounded-lg` = 8 px,
inputs 10 px, pills/avatars `rounded-full`. Client: `--radius` 10 px →
`rounded-lg` 10, `rounded-xl` 14, `rounded-md` 8.

| Element | Client |
|---|---|
| service card, calendar card, slot container, review cards, summary box | `rounded-xl` (14 px — closest to the mock's "squishy" 12) |
| inputs, textarea, every button (primary and outlined), chips | `rounded-lg` (10 px — the client's single control radius; the mock's 8/10 split collapses) |
| day cells, slot pills, avatars, check circle, icon buttons | `rounded-full` |
| icon tiles (48 px) | `rounded-lg` |

**Spacing.** Stitch `xs 4, sm 8, md 16, lg 24, xl 32, 2xl 48, stack_gap 12,
container_padding 20`. Client `base 4, xs 8, sm 12, md 16, lg 24, xl 32, 2xl 48`.

| Stitch | Client |
|---|---|
| `xs` (4) | `base` |
| `sm` (8) | `xs` |
| `stack_gap` (12) | `sm` |
| `md`, `lg`, `xl`, `2xl` | same names |
| `container_padding` (20) | `px-5` |
| `p-2` / `py-2` / `py-3` / `pl-12` / `h-10` / `h-12` / `w-64` / `w-6` / `w-24` | Tailwind defaults, unchanged |

**Icons.** Material Symbols → `lucide-react`, 20 px in the rail, 16–18 px
inline, 24 px in inputs, 48 px in the success check, 64 px for the review
card watermark.

| Material | lucide |
|---|---|
| `settings` (Service) | `Settings` |
| `person`, `person_outline` | `User`, `UserRound` |
| `calendar_today` | `Calendar` |
| `calendar_month` | `CalendarDays` |
| `schedule` | `Clock` |
| `checklist` | `ListChecks` |
| `check_circle` | `CircleCheck` |
| `check` | `Check` |
| `arrow_back`, `arrow_forward` | `ArrowLeft`, `ArrowRight` |
| `chevron_left`, `chevron_right` | `ChevronLeft`, `ChevronRight` |
| `public` | `Globe` |
| `wb_sunny`, `partly_cloudy_day`, `bedtime` | `Sun`, `CloudSun`, `Moon` |
| `mail`, `call` | `Mail`, `Phone` |
| `location_on` | `MapPin` |
| `content_cut` (barber-specific) | `CalendarCheck` (generic) |
| `event` | `CalendarPlus` |
| `add_circle` | `CirclePlus` |
| `storefront` | `Store` |
| `bolt` | `Zap` |
| `edit` | `Pencil` |
| `help`, `local_fire_department`, `star`, `account_circle`, `calendar_clock`, `event_available` | unused |

"Filled" icon states (`FILL 1` on the active rail icon) have no lucide
equivalent — use `text-primary` + `font-bold` on the row and leave the icon
outlined.

### 9.11 Step behaviour — the state machine

```text
/{orgSlug}                       step "service"   ServicePicker        rail: Service active
   └─ Continue ──▶ /{orgSlug}/{serviceSlug}
/{orgSlug}/{serviceSlug}         step "date"      BookingCalendar      rail: Service ✓, Date active
   date chosen, Continue ──▶     step "time"      SlotList             Date ✓
   slot chosen, Continue ──▶     step "details"   BookingDetailsForm   Time ✓
   valid submit ──▶              step "review"    BookingReview        Details ✓
   Confirm (POST) ──▶ 201        step "done"      BookingSuccess       Review ✓, Done active
                     409 ──▶     step "time"      (slots refreshed, selection cleared, error shown)
                     429 ──▶     step "review"    (countdown, button disabled)
                     400 ──▶     step "details"
```

- Back always goes to the previous step; from "date" it goes to `/{orgSlug}`.
- Rail rows for completed steps are links back to that step (Service →
  `/{orgSlug}`); future steps are inert; nothing is clickable on "done".
- On every step change: `window.scrollTo(0, 0)`. No focus management (demo,
  §6 #20).
- Selections persist across Back (the MVP keeps `selectedDate` when going
  back from time). Changing the date clears the slot; changing the zone
  keeps both the date string and the slot instant (the slot is an instant —
  it re-labels; if it no longer falls on the selected local date after a zone
  change, clear it).
- `visitorZone` defaults to the browser zone and persists only in memory.

### 9.12 The date math — `lib/date-math.ts`

All arithmetic is on `YYYY-MM-DD` strings and epoch milliseconds; `Date`
objects are never interpreted in browser-local time except inside
`toDateString` (carried for completeness) and `todayInZone`. Every zone
conversion goes through `Intl.DateTimeFormat(..., { timeZone }).formatToParts`
— the MVP's technique.

```ts
/** MVP verbatim. Local YYYY-MM-DD without toISOString()'s UTC shift. */
export function toDateString(date: Date): string

/** The MVP's toEventTypeLocalDateTimeString extractor, generalised. */
export function zonedParts(epochMs: number, zone: string):
  { year: number; month: number; day: number; hour: number; minute: number; second: number; weekday: number /* ISO 1–7 */ }
// Intl.DateTimeFormat("en-CA", { timeZone: zone, year:"numeric", month:"2-digit", day:"2-digit",
//   hour:"2-digit", minute:"2-digit", second:"2-digit", hour12:false, weekday:"short" }).formatToParts(...)
// "en-CA" is chosen because it yields YYYY-MM-DD ordering; hour "24" (some engines) is normalised to 0.

/** YYYY-MM-DD of an instant as seen in `zone`. */
export function localDateInZone(epochMs: number, zone: string): string

/** Epoch ms of 00:00:00 on `dateString` in `zone`. The DST-safe version of "midnight". */
export function startOfDayInZone(dateString: string, zone: string): number {
  const [y, m, d] = split(dateString);
  const guess = Date.UTC(y, m - 1, d, 0, 0, 0);            // midnight if the zone were UTC
  const seen = zonedParts(guess, zone);                    // what wall clock does `zone` show then?
  const seenAsUtc = Date.UTC(seen.year, seen.month - 1, seen.day, seen.hour, seen.minute, seen.second);
  let candidate = guess - (seenAsUtc - guess);             // shift by the offset observed at `guess`
  // The offset at `candidate` can differ from the offset at `guess` when a DST
  // transition sits between them; re-derive once. (A second pass is never
  // needed: transitions are ≥ 1 h apart and offsets change by ≤ 1 h.)
  const check = zonedParts(candidate, zone);
  if (check.year !== y || check.month !== m || check.day !== d || check.hour !== 0 || check.minute !== 0) {
    const checkAsUtc = Date.UTC(check.year, check.month - 1, check.day, check.hour, check.minute, check.second);
    candidate -= checkAsUtc - Date.UTC(y, m - 1, d, 0, 0, 0);
  }
  return candidate;
}

/** startOfDayInZone(addDays(dateString, 1), zone). */
export function endOfDayInZone(dateString: string, zone: string): number

/** localDateInZone(Date.now(), zone). */
export function todayInZone(zone: string): string

/** Pure string arithmetic via Date.UTC — no local time involved. */
export function addDays(dateString: string, n: number): string
export function compareDates(a: string, b: string): number         // string compare is enough for YYYY-MM-DD; keep explicit
export function weekdayOf(dateString: string): number              // ISO 1..7 via Date.UTC(...).getUTCDay(), Sunday → 7

/** The 42 cells of the month view, in display order. */
export function monthGrid(year: number, month: number, weekStartsOn: 0 | 1):
  { dateString: string; inMonth: boolean }[]

/** D11: the 1–2 schedule-zone dates that contain instants of `localDate` in `visitorZone`. */
export function scheduleDatesFor(localDate: string, visitorZone: string, scheduleZone: string): string[] {
  const first = localDateInZone(startOfDayInZone(localDate, visitorZone), scheduleZone);
  const last  = localDateInZone(endOfDayInZone(localDate, visitorZone) - 1, scheduleZone);
  return first === last ? [first] : [first, last];
}

/** MVP verbatim in spirit; `hour: "numeric"` so the locale decides 12/24 h. */
export function formatSlotTime(iso: string, zone: string): string
/** "Tuesday, Oct 24" (short) / "Thursday, October 24, 2024" (long), computed in `zone`. */
export function formatLongDate(dateString: string, zone: string, form: "short" | "long"): string
/** "Europe/London (GMT+1)". */
export function formatZoneLabel(zone: string): string             // timeZoneName: "shortOffset"
/** MVP verbatim: "SUNDAY" → 0, "MONDAY" → 1. */
export function weekStartsOnFrom(weekStart: WeekStart): 0 | 1
```

`date-math.test.ts` (vitest) must cover, at minimum:

1. `toDateString(new Date(2026, 9, 24, 0, 30))` → `"2026-10-24"` regardless
   of the test runner's zone (the MVP bug this guards).
2. `localDateInZone` for `2026-09-23T23:00:00Z` → `"2026-09-24"` in
   `Australia/Sydney`, `"2026-09-23"` in `America/New_York`.
3. `startOfDayInZone("2026-10-04", "Australia/Sydney")` (DST starts 02:00 →
   03:00 that day) equals `2026-10-03T13:00:00Z` (AEST midnight, before the
   jump); `startOfDayInZone("2026-10-05", …)` equals `2026-10-04T13:00:00Z`;
   the day is 23 h long.
4. `startOfDayInZone("2026-04-05", "Australia/Sydney")` (DST ends) → a 25 h day.
5. `startOfDayInZone("2026-03-08", "America/New_York")` (spring forward) and
   `"2026-11-01"` (fall back) → correct instants, no drift.
6. `scheduleDatesFor("2026-09-23", "America/New_York", "Australia/Sydney")`
   → `["2026-09-23", "2026-09-24"]`; same zones both `UTC` → one date.
7. `monthGrid(2026, 10, 0)` → 42 cells, first cell `"2026-09-27"` (Sunday);
   `monthGrid(2026, 10, 1)` → first cell `"2026-09-28"`.
8. `weekdayOf("2026-09-20")` → 7 (Sunday); `"2026-09-21"` → 1.
9. `formatSlotTime("2026-09-23T23:00:00Z", "Australia/Sydney")` contains
   `"9:00"`; `formatZoneLabel("Australia/Sydney")` matches `/GMT[+-]\d+/`.

---

## 10. NON-FUNCTIONAL REQUIREMENTS

### Must not exist after this task

1. Any `permitAll` matcher other than `/api/v1/public/**` added.
2. Any `CurrentPrincipalProvider`/`SecurityContextHolder` read under
   `service/publicbooking`.
3. Any new `AuthenticatedPrincipal` implementation, `IdentityLoader`, or
   switch branch.
4. Any change under `service/slot/`, `security/authorization/`,
   `security/token/`, `security/identity/`.
5. `FULL_PAGE` anywhere: Java, TypeScript, SQL (outside `V3`/`V143`/`V226`
   history), or documentation value lists.
6. A `widget.widgets` row, `WidgetEntity` reference, or `WidgetRepository`
   injection in the public path.
7. Bucket4j, Resilience4j, or any new server dependency.
8. `server.forward-headers-strategy: framework`.
9. A try/catch around Redis in the limiter.
10. A route under `app/(public)/` for the booking page.
11. `cookies()`, `withAccessToken`, or `bearer` in `app/api/public/**` or
    `features/public-booking/**`.
12. An org/user/tenant/schedule numeric id in any `Public*Response` (the
    reused `AvailableSlotsResponse.serviceId` excepted).
13. `react-day-picker`, `date-fns`, `dayjs`, `luxon`, `moment`, or a Material
    Symbols `<link>` in `client/`.
14. A six-digit hex literal, `gray-*`, `indigo-*`, `red-*`, `green-*`,
    `blue-*` Tailwind palette class, or `font-[Inter]` under
    `features/public-booking/` or `app/(booking)/`.
15. `new Date(isoString).getDate()` / `.getHours()` / `.setHours()` anywhere
    under `features/public-booking/` — every zone-dependent read goes through
    `date-math.ts`.
16. Text the API did not supply: ratings, review counts, "Popular", staff
    names, an address that is not `service.location`, a cancellation policy.

### Behavioural

17. A brand-new account (provisioned through `/register`) has a working
    `/{slug}` and `/{slug}/consultant-meeting` with no dashboard action.
18. Two concurrent POSTs for the same slot on the same host: exactly one
    201, one 409 (M9).
19. Two concurrent POSTs for the same time on **different services of the
    same host**: exactly one 201, one 409 (host-wide, M10).
20. The sixth POST from one IP within a minute is a 429 with `Retry-After`,
    and the count resets after the window (M11).
21. The public page renders correctly for a visitor whose zone differs from
    the schedule zone by enough to cross a date boundary — **proven by the
    `date-math` unit tests and by the author's B6, not by an agent**.
22. Every step renders without visual breakage at desktop width and in one
    390 px screenshot (V-series). Functional behaviour at 390 px and with
    the keyboard is the author's B10/B13.

---

## 11. ACCEPTANCE CRITERIA

### Database

1. After Phase A, `enum_range(NULL::widget.widget_type)` = `{INLINE,POPUP,EMBEDDED}`;
   `widgets.type` default is `'EMBEDDED'`; `flyway_schema_history` has `143`
   successful; the app boots.
2. `database/init/widget/V226__…` exists, commented, statement-identical to V143.

### Rate limiter

3. `RedisRateLimiterTest`: script executed once with key
   `ratelimit:public:write:1.2.3.4` and `ARGV = 60`; returns `1..limit` →
   no throw; returns `limit + 1` → `RateLimitExceededException` whose
   `retryAfterSeconds` equals the stubbed TTL; stubbed TTL `-1`/`null` →
   falls back to the window; a `DataAccessException` from `execute`
   propagates unchanged.
4. `PublicRateLimitInterceptorTest`: `OPTIONS` never calls the limiter;
   `GET` uses bucket `read`; `POST` uses bucket `write`; the key is
   `getRemoteAddr()`.
5. 429 responses carry `Retry-After` and the `ApiErrorResponse` body (M11).

### Public reads

6. `getOrganization`: unknown slug → 404 and **no** other repository called;
   reserved slug → 404 and **no** repository called; tenant `SUSPENDED` →
   404; tenant missing → 404; happy path → services without a policy
   omitted, `durationMinutes` and `autoConfirm` from the policy, `weekStart`
   from the organization, exactly one `findByServiceServiceIdIn` call.
7. `getService`: `scheduleTimezone` equals `schedule.timezone` (the test gives
   `service.timezone` a different value); `availableWeekdays` is the sorted
   distinct union of `days` across rows; `successRedirectUrl` passes through
   (null and non-null); service with no policy → 404
   `ServiceNotFoundException`, not `BookingPolicyNotFoundException`.
8. `getSlots`: delegates with `(service.serviceId, date)` and returns the
   engine's response instance unchanged.

### Public write

9. `createBooking` calls `findLockedByUserId(hostId)` **before**
   `slotService.getAvailableSlots` (Mockito `InOrder`).
10. `date` passed to the engine is `slotStart` in **`schedule.timezone`** —
    test with a `slotStart` of `T23:30:00Z` and zone `Australia/Sydney`,
    expecting the *next* calendar date.
11. Slot present → booking saved with `user = host`, `service`, `title =
    "<title> with <name>"`, `description = notes`, `location = service.location`,
    `endTime = slotStart + defaultDuration`; attendee saved with the four
    fields and `booking` set; response `timezone` = the schedule zone.
12. `autoConfirm = true` → `ACCEPTED`; `false` → `AWAITING_HOST`.
13. Slot absent → `SlotUnavailableException`, **no** `save` on either
    repository (`verifyNoInteractions`).
14. Any D8 failure → 404 before the lock is taken.
15. `@Valid` on the `@RequestBody`; `phone` blank → 400; `email` absent → 201.

### Reserved slugs

16. `createOrganization`/`updateOrganization` with `"login"` → 409; with
    `"LOGIN"` → 409; provisioning with a name that yields a reserved slug →
    uid slug used, no exception.

### Client — build and math

17. `npx tsc --noEmit`, `npx eslint .`, `npm run build`, `npx vitest run` — green.
18. `date-math.test.ts` covers the nine cases in §9.12 and passes with the
    runner's `TZ` set to `UTC`, `Australia/Sydney` and `America/New_York`
    (run three times; the results must not depend on it).
19. `WIDGET_TYPES` has three members; the picker shows three options;
    `z.enum(WIDGET_TYPES)` compiles; `/dashboard/widgets/new` renders (B8).

### Client — visual smoke (agents look, they do not test — D20)

20. **Every step renders** (V-series): `/o`, then `/o/consultant-meeting`
    walked once through Date → Time → Details → Review → Done with real
    data, one desktop screenshot per step and one 390 px screenshot of the
    Date step. "Renders" means: the rail, progress bar and step header are
    present; text is readable; nothing overflows horizontally; no
    unstyled/default-looking control; no console error originating from
    product code.
21. **Tokens only**: the diff under `features/public-booking/` and
    `app/(booking)/` contains no hex literal, no Tailwind palette class, no
    icon font, no date library (§10 #13–#14). This is the one design rule
    that is checked strictly.
22. **The simplifications hold**: no `animate-*`, no `transition-all`, no
    `group-hover:opacity`, no sticky gradient, no icons inside inputs, no
    ping ring — a grep, not a judgement call.
23. Elements that must be *present* (existence only, no behaviour asserted):
    six rail rows with the active/done treatment; "Instant booking" chip on
    `autoConfirm` services; weekday letters starting on `weekStart`; the
    timezone `<select>` under the calendar; Morning/Afternoon/Evening
    headers; the D10 field order with "Optional" on Email and Notes; Review
    cards each with an Edit link and the "Price" row; Done with the
    `#PBM-XXXXXXXX` reference, the three icon rows, "Add to Google Calendar"
    and "Book Another".
24. **Nothing in the B-series was executed by an agent.** The closing report
    says so in one line.

### Documentation

25. §3 #26–#30 exist. `SERVER_AGENTS.md` lets a reviewer predict M9–M11 from
    the text alone. `SECURITY.md` states, in these terms, that the public
    endpoints are unauthenticated by decision, what limits them, and that
    hCaptcha/blacklists are pending. `client/AGENTS.md` records the token
    mapping and why there is no date library. The ADR names the rejected
    guest-token option.

---

## 12. VALIDATION

### Automated (required — paste output)

| Test | Covers |
|---|---|
| `security/ratelimit/RedisRateLimiterTest` (Mockito; stub `redisTemplate.execute(any(RedisScript.class), anyList(), any())` and `getExpire`) | #3 |
| `security/ratelimit/PublicRateLimitInterceptorTest` (Mockito on `RateLimiter`; `MockHttpServletRequest`) | #4 |
| `service/publicbooking/PublicBookingServiceImplTest` (Mockito, shape of `BusinessServiceImplTest`) | #6–#14. `InOrder` for #9; `ArgumentCaptor<LocalDate>` for #10; `ArgumentCaptor<BookingEntity>`/`<AttendeeEntity>` for #11. |
| `organization/organizations/OrganizationServiceImplTest` (new, Mockito) | #16 |
| `ServerApplicationTests` | Context load — derived finders, `@Lock` finder, properties binding, interceptor registration. |
| `client/features/public-booking/lib/date-math.test.ts` (vitest) | #18 — the nine §9.12 cases, run under three `TZ` values. |

```text
cd server && ./gradlew compileJava && ./gradlew test
cd client && npx tsc --noEmit && npx eslint . && npm run build
cd client && TZ=UTC npx vitest run && TZ=Australia/Sydney npx vitest run && TZ=America/New_York npx vitest run
```

### Manual — server (curl against the live server; record commands and output)

Setup: register user `U` → org `O` (note the slug `o`), tenant `T`, service
`S` = `consultant-meeting` (30/30, notice 120, advance 43200, Mon–Fri 09–17
Sydney). Create `S2` on the same host via the dashboard with a price and a
`location`.

| # | Scenario | How | Expected |
|---|---|---|---|
| M1 | Org page | `GET /api/v1/public/o` | 200; two services; `durationMinutes = 30`; `weekStart = "MONDAY"`; no ids in the body |
| M2 | Service page | `GET /api/v1/public/o/service/consultant-meeting` | 200; `scheduleTimezone = Australia/Sydney`; `availableWeekdays = [1,2,3,4,5]`; `autoConfirm = true`; `successRedirectUrl = null` |
| M3 | Slots | `GET …/slots?date=<next Wednesday>` | 200; body identical to `GET /api/v1/slots?serviceId=S&date=…` with `U`'s JWT |
| M4 | No auth needed | M1–M3 with **no** `Authorization` header | 200 |
| M5 | Bad Bearer on a public route | M1 with `Authorization: Bearer garbage` | 401 — documented filter behaviour; the BFF never sends one |
| M6 | 404 family | unknown slug; `login`; unknown service; `S2` after deleting its policy row; set `T.status = SUSPENDED` (SQL) | 404 each, **identical** message shape; restore `T` |
| M7 | Create | `POST …/bookings` `{name, phone, slotStart = first M3 slot}` | 201; `status = ACCEPTED`; a `core.bookings` row with `user_id = U`, `title = "Consultant Meeting with <name>"`, and one `core.attendees` row |
| M8 | Gone | repeat M3 | the M7 slot (and its buffered neighbours) missing |
| M9 | Race, same slot | two M7 requests for the same fresh slot fired concurrently (`& wait` or `xargs -P2`) | one 201, one 409; exactly one row |
| M10 | Race, host-wide | same, one on `S`, one on `S2`, same time | one 201, one 409 |
| M11 | Write limit | six M7 POSTs within a minute from one IP (use fresh slots) | five responses that are 201/409, the sixth **429** with `Retry-After`; after the window, a POST succeeds again |
| M12 | Read limit | 121 × M3 in a minute | the 121st is 429 |
| M13 | Validation | `phone` missing; `slotStart` malformed; `slotStart` not on the grid (`…T23:07:00Z`) | 400; 400; 409 |
| M14 | Requires confirmation | set `S`'s policy `auto_confirm = false`; M7 | 201 with `AWAITING_HOST`; appears in the dashboard's Pending tab; restore |
| M15 | Redis down | stop the redis container; M1 | 500 (fail-closed); start it; M1 → 200 |
| M16 | Forwarded IP | M7 with `X-Forwarded-For: 203.0.113.9` from localhost | limiter key is `203.0.113.9` (check `redis-cli KEYS 'ratelimit:*'`). Then the same header sent from a non-loopback address, if you can — the key must be the real peer, not the header |
| M17 | Reserved slug | `PUT /api/v1/organizations/{O}` with `slug = "dashboard"` | 409 |

Paste the M1, M2, M3 and M7 JSON into the closing report.

### Visual walkthrough — agents (`run` the client against the same server, then `claude-in-chrome`)

This is the **only** browser work an agent does on this task (D20). Walk the
flow once with real data, look, screenshot, stop. Do not assert behaviour;
do not try error paths; do not open a second tab; do not change the zone
except to see that the control opens.

| # | Open | Screenshot | Look for |
|---|---|---|---|
| V1 | `/o` (desktop, ≥ 1280 px) | yes | rail with business name, Service row active, progress 1/6, service cards with duration/price, "Instant booking" chip |
| V2 | click a card → `/o/consultant-meeting` | yes | Service row ticked, Date active, calendar card, weekday letters, today underline, timezone select present |
| V3 | pick any enabled day → Continue | yes | Time step: date subtitle, Morning/Afternoon headers, 2-up pills |
| V4 | pick any pill → Continue | yes | Details step: slot chip, four fields in D10 order, "Optional" tags |
| V5 | fill name + phone → Continue to Review | yes | Review: cards with Edit links, Price row, Confirm button, fine print |
| V6 | Confirm | yes | Done: check circle, headline, `#PBM-…` reference, three rows, two/three action buttons |
| V7 | `resize_window` to 390 px, back on `/o/consultant-meeting` | yes | rail hidden, mobile bar with business name, calendar fits, no horizontal scroll |
| V8 | console after V1–V7 | no | no product-originated errors (extension-injected attribute warnings are noise) |

V6 creates one real booking; note its uid in the report so the author can
find it. That is the entire agent-side browser scope.

### Functional browser testing — **author only** (agents and reviewers must not run this)

The author tests the flow personally after the closing report. The B-series
below is the author's checklist and is retained here so it is not lost. An
agent that executes any B row, or a reviewer that requests one, is out of
scope for this contract.

| # | Scenario | Expected |
|---|---|---|
| B1 | `/o` on desktop | shell + rail (Service active) + two service cards + gradient Continue; no marketing header; no console errors |
| B2 | Select a card, Continue | check circle animates in; navigates to `/o/consultant-meeting`; rail shows Service ✓, Date active; progress 2/6 |
| B3 | Date step | weekends and past days at 30 %, today underlined; pick next Wednesday → solid primary circle; Continue enables |
| B4 | Time step | slots grouped under Morning/Afternoon with icons, no Evening group; labels in the browser zone; select one → primary pill with check |
| B5 | Change timezone on the Date step to `America/New_York`, revisit Time | same slots re-labelled; a Sydney 09:00 shows as the previous evening |
| B6 | Date-boundary case | in `America/New_York`, pick the local date on which Sydney's Wednesday morning slots fall (Tuesday NY). They must appear under **Tuesday**, not Wednesday — this is D11 working. Screenshot required. |
| B7 | Details → Review → Confirm | chip shows the slot; validation errors inline on empty phone; Review cards show the right data; Edit on "Your Details" returns to the form with values intact; Confirm → Done with "Booking Confirmed!"; row visible in `/dashboard/bookings` |
| B8 | `/dashboard/widgets/new` | type picker shows three options, `INLINE` enabled |
| B9 | `/login` (a reserved word) | still the login page, not a 404 booking page |
| B10 | `resize_window` to 390 px: B1, B3, B4, B7 | rail hidden, mobile bar with business name and back chevron, single column, 20 px gutters, no horizontal scroll; pills still 2-up |
| B11 | Network tab during B7 | requests go to `/api/public/...` only; no request carries `Authorization`; no cookie is set |
| B12 | Race in the browser | book a slot in one tab; in a second tab already on Review for that slot, Confirm → error copy, returns to Time, the slot is gone |
| B13 | Keyboard only, B1–B7 | Tab reaches every card/day/pill/field/button; arrow keys move within the service radio group and the day grid; Enter/Space activate; focus lands on the title after each step change; focus rings visible |
| B14 | `S2` (priced, with location) end to end | price on the card, in Review's "Price" row and on Done; Location card on Review; location row on Done; Google Calendar link URL contains `dates=` in `YYYYMMDDTHHmmssZ/…` form |
| B15 | Requires-confirmation service (after M14) | Done shows "Request Sent" and the org-will-confirm copy; Review fine print said so beforehand |
| B16 | `prefers-reduced-motion: reduce` (DevTools rendering emulation) on Done | no ping animation; check circle static |

For the author: B6, B11 and B13 are the three that matter most.

---

## 13. ESCALATION

Stop and report to the orchestrator when:

1. The baseline is not green.
2. V143 fails to apply, or `flyway_schema_history` shows it already applied by
   hand (the ledger row would need the right checksum — do not fabricate one).
3. Any code path other than the enum references `FULL_PAGE` (a `switch`, a
   seed, a client filter default).
4. `SlotService.getAvailableSlots` cannot answer "is this exact slot offered"
   — for example if the engine starts returning slots not aligned to
   `defaultDuration`. Do not add a method to the engine.
5. Spring Data rejects `findLockedByUserId` or `findByServiceServiceIdIn` at
   context load. The `@Query` fallback needs approval.
6. Dev data contains an organization whose provisioned slug does not survive
   a URL path segment (non-ASCII, `/`, `?`, `#`). Do not add normalisation in
   this task.
7. The nested `@Transactional(readOnly = true)` call to the engine inside the
   read-write booking transaction causes the inserts to be skipped or
   flushed late (M7 returns 201 but no row exists). The fix is architectural
   (extract a non-transactional engine entry point), not a flag.
8. `RemoteIpValve` does not rewrite `getRemoteAddr()` for the local BFF
   (M16) — the default `internal-proxies` may differ on this Tomcat version.
   Report the observed default; do not widen it to `.*`.
9. You need any client runtime dependency, or any server dependency at all.
10. You find yourself wanting to read a principal, add a `WidgetEntity`
    lookup, mint a token, or add a second `permitAll` pattern.
11. The author has not confirmed D10 (phone required, email optional) by the
    time Phase E starts.
12. A test green at baseline fails and the fix would be to edit that test.
13. `startOfDayInZone` needs more than one correction pass for any IANA zone
    you test — the algorithm's premise is wrong for that zone; report it
    rather than looping.
14. A Stitch element you cannot map to an existing client token (§9.10) or
    to data the API returns (§9.2): **omit it and record it** in the closing
    report — this is a demo (D20), do not stop for it. Escalate only if
    omitting it leaves a step unusable.
15. `Intl.supportedValuesOf("timeZone")` is unavailable in a target browser
    you are asked to support — a static zone list is a product decision.
16. You are asked — by anyone, including a reviewer — to run a B-series
    scenario, test an error path in the browser, or "make sure the flow
    works end to end". That is the author's job (D20). Decline and point
    here.

Do not silently resolve architectural or security ambiguity. Design
ambiguity you *do* resolve yourself, toward the simpler option, and record.

---

## 14. IMPLEMENTATION PLAN

### Phase A — ground truth and the migration (Steps 1–3)

1. Clean tree, baseline (§7). Read §8 "MVP v2 widget" — every file — and
   read every Stitch `code.html` (glance at each `screen.png` once) before
   writing a line of client code. Read §8 "patterns to mirror".
2. `./gradlew bootRun` once. Confirm in the log that Flyway applied `143`.
   Run the two SQL checks in §9.1. Stop the server.
3. Write `database/init/widget/V226__drop_widget_type_full_page.sql` with
   comments. Diff it statement-for-statement against V143.

### Phase B — enum cleanup, both sides (Steps 4–6)

4. `WidgetType` → three values. `./gradlew compileJava`. Grep the server for
   `FULL_PAGE` — only V3/V143 may remain.
5. Client: `widget.ts`, `widget-type-picker.tsx` (remove the option and the
   `Maximize2` import). `npx tsc --noEmit`. Grep `client/` and `widget/` for
   `FULL_PAGE` — nothing.
6. Docs value lists: `Table Widgets.md`, `DB.md`, `List widgets.md`,
   `SERVER_AGENTS.md` widget notes, `client/AGENTS.md` "four `WidgetType`s".
   Populate `Widget Types.md`.

### Phase C — repositories (Step 7)

7. The four finders in §9.5. Boot once (`ServerApplicationTests`). Escalation #5.

### Phase D — rate limiting and security config (Steps 8–12)

8. `RateLimitProperties` + `application.yaml` block + `server.forward-headers-strategy: native`.
   Register the properties on `WebConfig` next to `CorsProperties`.
9. `RateLimiter` + `RedisRateLimiter` (Lua script as a `static final DefaultRedisScript<Long>`).
   `RedisRateLimiterTest`.
10. `RateLimitExceededException` (carries `retryAfterSeconds`) + 429 handler
    with `Retry-After`.
11. `PublicRateLimitInterceptor` + `WebConfig.addInterceptors`.
    `PublicRateLimitInterceptorTest`.
12. `SecurityConfig` matcher. Boot; `curl -i /api/v1/public/anything` → 404
    (not 401) proves the matcher; `redis-cli KEYS 'ratelimit:*'` shows the
    read key.

### Phase E — public reads (Steps 13–15)

13. DTO records per §9.2 (including `weekStart`, `autoConfirm` on the
    summary, `successRedirectUrl`). `ReservedOrganizationSlugs`.
14. `PublicBookingService` (four methods) + impl: the three helpers,
    `getOrganization`, `getService`, `getSlots`.
15. Controller `GET` handlers. M1–M6. Start `PublicBookingServiceImplTest`
    (#6–#8).

### Phase F — public write (Steps 16–18)

16. `SlotUnavailableException` + 409 handler.
17. `createBooking` per §9.4 — lock, date-in-zone, engine, membership, two
    saves. The captcha hook comment.
18. Controller `POST` handler with `@Valid`. M7–M15. Finish the service
    test (#9–#14). M9/M10 are the ones that prove the lock; run them with two
    genuinely concurrent processes, not sequentially.

### Phase G — reserved slugs (Step 19)

19. §9.6 in the three places. `OrganizationServiceImplTest`. M17.

### Phase H — client foundation: math, contract layer, BFF (Steps 20–24)

20. `vitest` devDependency + `npm test` script. `lib/date-math.ts` — port
    `toDateString`/`formatSlotTime`/`weekStartsOnFrom` verbatim, then build
    `zonedParts` from the MVP's `formatToParts` extractor, then
    `localDateInZone`, `startOfDayInZone`, the rest. Write
    `date-math.test.ts` **before** `startOfDayInZone` — the DST cases (#3–#5)
    are the ones that catch a wrong sign.
21. `lib/format.ts` (port), `lib/booking-steps.ts` (the six steps: id, label,
    lucide icon, copy from §9.9, progress fraction), `lib/calendar-link.ts`.
22. `types/public-booking.ts` from the M1/M2/M3/M7 JSON.
    `schemas/booking-details-schema.ts` (port).
23. Gateway, browser API, errors. `forwardedFor()` helper lives in the
    gateway module.
24. The two Route Handlers. `curl` them through the Next dev server; confirm
    with `redis-cli` that the Spring-side key is the BFF's loopback address
    (expected locally).

### Phase I — client shell and screens (Steps 25–33)

25. `app/globals.css`: add `--text-headline-sm`. `app/(booking)/layout.tsx`,
    `not-found.tsx`. Verify `/login` still resolves to the login page (B9)
    before building anything else.
26. `booking-shell.tsx`, `booking-rail.tsx`, `booking-mobile-bar.tsx`,
    `booking-footer.tsx`, `step-header.tsx`, `spinner.tsx`,
    `error-message.tsx` — per §9.9 "The shell". Render once with placeholder
    content to see it holds together; do not iterate against the mocks.
27. Org page + `service-picker.tsx` + `service-card.tsx` (`step_1`, demo
    version: cards are links, no selection state, no Continue).
28. Service page + `booking-flow.tsx` shell with the step machine and
    `selected-service-strip.tsx`.
29. `booking-calendar.tsx` on `monthGrid` + `timezone-select.tsx` (`step_3`).
    Build the disabled/today/selected states first, then the month
    navigation bounds.
30. `use-available-slots.ts` (the D11 fan-out — write the two-date case
    first) + `slot-list.tsx` + `slot-group.tsx` (`step_4`).
31. `booking-details-form.tsx` (`step_5`) with the D10 field order.
32. `booking-review.tsx` (`step_6`) with the four error paths wired.
33. `booking-success.tsx` (`step_7`) including the calendar link and the
    conditional return button.

### Phase J — validation (Steps 34–35)

34. Full automated run, both repos, the three `TZ` vitest runs. Record.
35. **V1–V8 only.** One pass, screenshots, stop. Do not run B1–B16 — the
    author does (D20). Write the "no B-series executed" line for the report.

### Phase K — documentation and report (Steps 36–38)

36. `SERVER_AGENTS.md`, `SECURITY.md`, `client/AGENTS.md` per §3.
37. Obsidian service + API pages from the templates; ADR-0001.
38. Closing report per §3 #31.

---

## 15. REVIEW STRATEGY

### What to review

1. **The trust boundary** — `SecurityConfig` diff is one line;
   `application.yaml` says `native`, not `framework`; nothing under
   `service/publicbooking` imports anything from `security/identity`,
   `security/authorization`, or `SecurityContextHolder`; the BFF routes
   import neither `cookies` nor `withAccessToken`.
2. **Lock-then-engine** — read `createBooking` top to bottom: resolve →
   lock → date-in-zone → engine → membership → saves. Any reordering is
   blocking. The `InOrder` test must exist and assert lock before engine.
3. **Date in the right zone, server** — `slotStart.atZone(ZoneId.of(schedule.getTimezone()))`,
   not `service.getTimezone()`, not `ZoneOffset.UTC`. The #10 test must use a
   `slotStart` that crosses a date boundary.
4. **Date in the right zone, client** — every zone-dependent read in
   `features/public-booking` goes through `date-math.ts`; grep for
   `getDate(`, `getHours(`, `setHours(`, `toISOString().slice` — any hit
   outside `date-math.ts` is a finding. `startOfDayInZone` has the single
   re-derivation and a comment saying why one pass suffices.
5. **Same 404 for every reason** — grep the impl for exception messages;
   exactly one message per exception type; no "suspended", "no policy",
   "reserved" in any message.
6. **Response hygiene** — every `Public*Response` field against §6 #9.
7. **Limiter atomicity and failure mode** — Lua script, not
   `increment`+`expire`; no catch of `DataAccessException`; `Retry-After`
   present on 429; `OPTIONS` skipped.
8. **The client mapping (D11)** — `useAvailableSlots` computes ≤ 2 schedule
   dates and filters by the *local* date; B6 recorded with a real screenshot;
   `loading` is derived from the key, not a separate flag.
9. **Visual sanity, not fidelity** — look at the V1–V7 screenshots once
   and check §11 #20–#24: everything renders, tokens only, the demo
   simplifications hold (grep for `animate-`, `transition-all`,
   `group-hover:opacity`, gradients), the listed elements exist. Then grep
   the client diff for hex literals, palette classes, `react-day-picker`,
   `date-fns`, Material Symbols — §10 #13–#14. **Do not open the app to
   test the flow** — the author does (D20). Do not compare against
   `screen.png`.
10. **MVP fidelity** — `date-math.ts` still contains `toDateString` with its
    original comment; `formatDuration` is byte-identical; the zod rules
    (regex, lengths, `""` email union) match `customerSchema.ts`; the hook's
    `active` flag and derived `loading` are present.
11. **Enum removal completeness** — `grep -r FULL_PAGE` across `server/`,
    `client/`, `widget/`, `obsidian/` returns only V3, V143, V226, and the
    history line in `Widget Types.md`.
12. **Reserved slugs** — the three call sites; case-insensitive; `login` in
    the set; B9 recorded.
13. **Query counts** — §6 #13–#16 by reading the impl; any repository call
    inside a `for`/`stream` over services is blocking.
14. **Docs** — `SECURITY.md` section exists and says what is *not* defended;
    ADR names the rejected alternative; `SERVER_AGENTS.md` schema version is
    143; `client/AGENTS.md` carries the token mapping.

### How to review

- Run both test suites yourself, including the three-`TZ` vitest run.
- Replay M6 (all five 404 reasons), M9, M10, M11, M16.
- Send `Authorization: Bearer <valid user JWT>` to a public GET — it must
  still be 200 (the filter sets a principal; the service ignores it).
- Send `X-Forwarded-For: 1.1.1.1` **directly** to Spring from a non-loopback
  address if your setup allows it; the limiter key must not be `1.1.1.1`.
- Confirm from the *code* (not the browser) that the public BFF routes and
  `features/public-booking/**` import neither `cookies` nor
  `withAccessToken` nor `bearer` — the network-tab check (B11) is the
  author's.
- Do not drive the browser yourself. Read the V-series screenshots the
  implementer attached; if one is missing, that is the finding.

### Core components to review

`PublicBookingServiceImpl`, `RedisRateLimiter`, `PublicRateLimitInterceptor`,
`SecurityConfig`/`WebConfig`/`application.yaml`, `UserRepository.findLockedByUserId`,
`ReservedOrganizationSlugs` (+ 3 call sites), `lib/date-math.ts`,
`hooks/use-available-slots.ts`, `components/booking-calendar.tsx`,
`components/booking-flow.tsx`, the two Route Handlers.

Reviewer roles: **security** (#1, #5, #6, #7, and the direct-XFF probe),
**implementation** (#2, #3, #4, #8, #10–#13), **design** (#9 — a visual
sanity pass over the attached screenshots and a grep; no browser driving,
no fidelity scoring), **architecture** (ADR, package placement, that
D1/D2/D17/D18/D20 are honoured), **testing** (every AC has a test, an M row
or a V row; the M9/M10 runs were concurrent; the vitest run was repeated
under three `TZ` values; **no B row was executed**).

---

## 16. NOT IN THIS TASK — recorded so it is not rediscovered

| Item | Why not here | Where it goes |
|---|---|---|
| hCaptcha on the POST | Author excluded it to focus on rate limiting + Redis. Hook point: after `resolvePolicy`, before the lock (§9.4). Needs `app.hcaptcha.{enabled,secret}`, a `RestClient` `siteverify` client shaped like `GoogleTokenClient`, `NEXT_PUBLIC_HCAPTCHA_SITE_KEY`, `@hcaptcha/react-hcaptcha`, and a slot in the Review step above Confirm | Next public-booking task |
| IP / email / phone blacklist | Author's idea, deferred. Note for then: it wants both a platform-wide list (abuse) and a per-tenant list ("block this customer"), which changes the table design | After captcha |
| Fail-open limiter with alerting | D4 chose fail-closed; revisit when ops has alerting on Redis | Ops task |
| Idempotency on the public POST | `idempotency_key` has no unique index and no finder; a network retry after a successful POST currently yields a confusing 409 | Booking-write hardening |
| Attendee cancel / reschedule links, confirmation email/SMS | No notification pipeline exists (ISSUE-0002); the attendee has no session | Notification task |
| `customer.customers` upsert per (tenant, email/phone) | D12 — CRM feed is a product decision with matching rules to settle | CRM task |
| "Your booking page" link + copy button in the dashboard | Needs the org slug in the dashboard's data layer, which this task does not touch | Dashboard task |
| An org-level "public page enabled" toggle / `is_private` semantics | D8 ignores `is_private` because provisioning sets it `true`; deciding what it means is a product call | Organization settings task |
| Light theme (`step_1_select_service_light`) / tenant branding (logo colours, custom domain via `tenant_domains`) | Client is dark-only by rule; the light mock exists and is recorded here so it is not "discovered" | Branding task |
| Stitch "Staff" step, "Any Available", ratings, reviews, "Popular", cover images, "Next available" | No staff/resource choice on the public path; no rating, image or popularity data on the platform | Resource-scheduling task (staff); service media task (images) |
| "Need Help?", Privacy / Terms / Support links | No support surface, no legal pages exist | Marketing/legal task |
| "Add to Google Calendar" via the Calendar API / ICS download | The template URL covers the mock's button without a dependency; ICS is a small follow-up | Notification task |
| `?step=` deep links / browser-back between steps | D19 — in-memory as the MVP; the editors' query-param rule was about unmount safety, which a single client component does not suffer | UX task |
| Month/range availability endpoint ("which days have slots") | TASK-0009 §16; `availableWeekdays` is the cheap stand-in | Slot range task |
| `maxActiveBookingPerBooker`, `capacity`, `allowOverlap`, `allowMultipleAttendee` | Engine ignores them; enforcing at write time without the engine agreeing would offer slots it then refuses | Capacity task |
| Re-validation in the dashboard's `BookingServiceImpl.createBooking` | Different DTO shape and actor; TASK-0009 §16 already parks it | Booking-creation task |
| URL-safe slug normalisation in provisioning | Existing behaviour; Escalation #6 if it bites dev data | Provisioning task |
| `MethodArgumentNotValidException` handler | Global 400 shape change; the client compensates today | Error-shape task |
| Per-tenant or per-service rate limits | IP is enough for v1; tenant-keyed limits need the resolved org before the interceptor runs | After captcha |
| `react-day-picker` (the MVP's calendar) | D17 — browser-local `Date` semantics fight a visitor-chosen display zone; the Stitch grid is 42 cells | — |
| **Design polish** — selection indicator + sticky gradient Continue on step 1, animations (progress transition, check scale-in, success ping), hover-revealed Edit links, icons inside inputs, slot-container gradient/deep shadow, glow on the selected pill, focus management on step change, per-step content widths, pixel fidelity to `screen.png` | D20 — the Stitch mocks are the demo design; the author iterates after a functional pass | Design-polish task, after the author's B-series |
| **Functional browser testing of the flow** (B1–B16) | D20 — the author does this personally; agents only look | Author |
| Guest JWT / `GuestPrincipal` | Rejected (D2, ADR-0001) | — |

---

## 17. CLOSING REPORT

Status of this contract stays **ACTIVE** until
`.agents/reviews/TASK-0011-full-page-widget-review.md` exists.
