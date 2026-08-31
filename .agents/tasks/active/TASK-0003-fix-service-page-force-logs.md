# Task Contract

## 1. IDENTITY

Title: Org-Scoped Service List Endpoint + Service-Scoped Booking Policy Lookup
Domain: Server / `core.service` + `core.bookingpolicy` (Spring Boot) — with required client follow-through
Priority: High
Risk: Medium

Resolves: `.agents/issues/open/ISSUE-0005-sign-out-on-service.md`

Status: Active

---

## 2. INTENT

Close ISSUE-0005 by giving the client a real, organization-scoped way to
list services, instead of the unscoped `GET /api/v1/services` that this
session deleted (per "Drop the entire getAllServices endpoint") and that
`listMyServiceCatalogOnPlatform` never stopped calling — the resulting `401`
is what forces the user back to `/login?session=expired` on every visit to
`/dashboard/services` (see ISSUE-0005 for the full reproduction).

The fix has two coordinated halves, both requested by the user in the same
breath because they serve the same page and the same underlying problem:

1. **Primary (bug fix): `GET /api/v1/services?organizationId=`.** Add back a
   list endpoint, this time properly scoped, and repoint
   `listMyServiceCatalogOnPlatform` at it.
2. **Additive (new capability): `GET /api/v1/booking-policies?serviceId=`.**
   A service-scoped lookup for a single service's booking policy. The user
   was explicit this matters because it's what lets the frontend show a
   service's real settings (duration, notice, capacity) on the catalog UI.

These two are not independent conveniences — `listMyServiceCatalogOnPlatform`
today assembles the catalog by bulk-fetching **every** organization's
services (fixed by #1) and **every** organization's booking policies (fixed
by #2) and filtering client-side. `service-gateway.ts`'s own doc comment
already names this as a "TEMPORARY WORKAROUND" for exactly this reason:

> `GET /api/v1/services` and `GET /api/v1/booking-policies` are generic CRUD
> endpoints with no owner/organization scoping at all — they return every
> service and every booking policy for every organization on the platform.

Item #1 is forced by ISSUE-0005 (the old unscoped endpoint is already gone).
Item #2 is the user's own explicit ask, and finishing it in the same task
retires the *entire* workaround comment above, not just half of it — the
catalog page ends up properly scoped end to end, not scoped-for-services-but-
still-globally-exposed-for-policies.

**Do not reintroduce an unscoped list on either endpoint.** The whole reason
the old `GET /api/v1/services` was deleted was that it leaked every
organization's rows to any authenticated caller — see the closed
`ISSUE-0004`/`TASK-0002` precedent, where the equivalent gap on `Service`
*create/update* ownership was treated as a real security defect, not a nice-
to-have. Building the replacement list endpoint without real scoping would
re-open the identical class of bug this same session already closed once.

---

## 3. DELIVERABLES

### Backend

1. `GET /api/v1/services?organizationId={id}` — returns every `ServiceEntity`
   belonging to that organization, **only if the caller actually belongs to
   it** (Section 6 has the exact validation rule — this is not optional).
2. `GET /api/v1/booking-policies?serviceId={id}` — returns the booking policy
   for that service (0 or 1 result, `core.booking_policies` is 1:1 with
   `core.services` per `uq_booking_policies_service`).
3. `ServiceRepository`/`BookingPolicyRepository` gain the query-derivation
   methods backing both endpoints.

### Frontend

4. `client/features/services/services/service-gateway.ts`:
   `listMyServiceCatalogOnPlatform` calls the new org-scoped services
   endpoint and the new service-scoped booking-policy endpoint instead of
   the two unscoped bulk fetches — the "TEMPORARY WORKAROUND" doc comment on
   that function is removed (it's no longer a workaround once both sides are
   properly scoped) or rewritten to reflect the real remaining shape.
5. A new exported gateway function wrapping
   `GET /api/v1/booking-policies?serviceId=` (e.g.
   `getBookingPolicyForServiceOnPlatform`), usable independently of the
   catalog-list assembly for any future single-service settings view.

### Documentation

6. A new Obsidian page per new endpoint, each written against
   `obsidian/PleaseBookMe/API/API Template.md` (the universal per-endpoint
   template) — not a summary paragraph, not a diff-style note, an actual
   filled-in copy of that template with every section populated. Section 6
   ("Documentation Naming Convention") below has the exact filenames.
7. Both affected `API Summary.md` files updated: a new row in the endpoints
   table plus a new `[[Page Name]]` wikilink to the corresponding new page.

### Validation

8. `/dashboard/services` loads successfully end to end for a signed-in user
   with a valid session — no redirect to `/login?session=expired` (this is
   the literal repro from ISSUE-0005; re-run it as the final check).
9. A short closing report: files touched, tests run, and confirmation the
   ISSUE-0005 repro no longer reproduces.

---

## 4. SCOPE

### In Scope

- `server/src/main/java/com/pleasebookme/server/core/service/` —
  `ServiceController`, `BusinessServiceService`, `BusinessServiceImpl`,
  `ServiceRepository`.
- `server/src/main/java/com/pleasebookme/server/core/bookingpolicy/` —
  `BookingPolicyController`, `BookingPolicyService`, `BookingPolicyServiceImpl`,
  `BookingPolicyRepository`.
- `client/features/services/services/service-gateway.ts`.
- `client/features/services/types/service.ts` if the new gateway function
  needs a new response shape (it shouldn't — reuses existing `Service`/
  `BookingPolicy` types).
- `obsidian/PleaseBookMe/API/Core/Services/*.md` and
  `obsidian/PleaseBookMe/API/Core/Booking Policies/*.md` — both endpoints are
  new/changed public contract and must be documented using the universal
  templates (Section 6, Section 14 Step 9) — not deferred, not summarized in
  prose. `CLAUDE.md`/`AGENTS.md` require this, and the last task's docs-drift
  was already caught once this session — don't repeat it.
- `.agents/issues/open/ISSUE-0005-sign-out-on-service.md` — close out once
  verified fixed, per `.skills/workflows/issue-generating/SKILL.md`.

### Out of Scope

- `client/lib/platform-user.ts` — `resolveCurrentPlatformUser` still resolves
  `organizationId` from a full `GET /api/v1/profiles` scan; that scan is a
  separate, already-known gap (no `/me`-style profile lookup exists) and is
  **not** what this task is fixing. Do not touch it unless a specific change
  here requires it — if it does, that's an escalation (Section 13), not a
  silent scope expansion.
- Changing `GET /api/v1/services/{id}` or `GET /api/v1/booking-policies/{id}`
  (single-resource reads) — unaffected, already correct.
- Removing the existing unscoped `GET /api/v1/booking-policies` (no query
  param) list-all endpoint. It has the same unscoped-exposure shape as the
  old `GET /api/v1/services` did, but closing it is not requested here and
  is not what's breaking the Services page. Adding the `serviceId` filter to
  the *same* `@GetMapping` (Section 14) is in scope; deleting or otherwise
  changing the no-param behavior is not.
- Any change to `POST`/`PUT`/`DELETE` on either controller.
- Reworking `withAccessToken`/`isAuthFailure` in
  `client/lib/authenticated-platform-request.ts` — ISSUE-0005 flagged this as
  a second, independent hardening opportunity (a route-level `401` shouldn't
  be indistinguishable from a dead session). Worth its own task; do not fold
  it into this one.
- Any Flyway migration — no schema change is required for either endpoint.

---

## 5. BOUNDARIES

- Do not touch `auth/`, `security/oauth/google/`, or any Google integration
  code — unrelated bounded context.
- Do not modify `core/schedule/`, `core/availability/`, or any other
  `core/*` subdomain besides `service/` and `bookingpolicy/`.
- Do not introduce a new orchestration bean under `service/` for this — both
  changes are plain, single-repository query additions on existing
  controllers/services, not cross-domain flows.
- Do not add a generic "list with arbitrary filters" mechanism (e.g. a
  filter-object query builder). Add exactly the two narrow, named query
  parameters requested — `organizationId` and `serviceId` — nothing more
  speculative.

---

## 6. CONSTRAINTS

### Security (read this before writing the Service endpoint)

- **`organizationId` is client-supplied and must not be trusted blindly.**
  This is the one place in this task where getting it wrong recreates the
  exact bug this session already spent a task fixing (`TASK-0002`/
  `ISSUE-0004`: never let a request-supplied ownership id determine what a
  caller can see or do). The endpoint must derive the caller's **own**
  organization the same way `BusinessServiceImpl.resolveCurrentOwner()`
  already does (`CurrentPrincipalProvider.requireUser().userId()` →
  `ProfileRepository` → `profile.getOrganization()`), and compare it against
  the requested `organizationId`.
  - **On a mismatch, return an empty list — not a `403`/`404`.** This
    mirrors the existing `disconnect()`/`updateService` precedent
    (`SECURITY.md` §12, this session's `updateService` fix): the response
    for "an org you don't belong to" must be indistinguishable from "an org
    with zero services," so the endpoint can never be used to probe which
    organization ids exist or how many services they have.
  - This means the query parameter is honored for shape/convenience (the
    client already knows and sends its own `organizationId`), but it is
    **never** the source of truth for what gets returned — the caller's own
    derived organization is.
  - Apply the same known limitation this session already documented for
    `resolveCurrentOwner()`: a caller with profiles in more than one
    organization gets `AmbiguousServiceOwnerException` (`409`) from that
    helper today. Confirm whether that's the right behavior for a **list**
    endpoint too (a create/update throwing 409 makes sense; a list endpoint
    throwing 409 instead of, say, unioning across the caller's orgs is a
    product decision) — if it feels wrong, escalate (Section 13) rather than
    silently changing `resolveCurrentOwner()`'s contract for every other
    caller.
- **`serviceId` on the booking-policy lookup does not need the same
  treatment.** The existing unscoped `GET /api/v1/booking-policies` (no
  param) already exposes every policy on the platform to any authenticated
  caller — adding a narrower, service-scoped filter on the *same* mapping
  does not make that pre-existing exposure worse; it's strictly narrower.
  Per Section 4's "Out of Scope," closing that pre-existing gap is a
  separate decision. Do not add ownership validation here that the sibling
  no-param path doesn't already have — that would be an inconsistent,
  half-applied fix. Note this asymmetry in your closing report so it's not
  mistaken for an oversight.

### Documentation Naming Convention (analyzed from the existing `obsidian/PleaseBookMe/API/` tree — apply exactly, do not improvise)

Two templates govern every page in this tree, both under
`obsidian/PleaseBookMe/API/`:

- `API Template.md` — one page per endpoint (verb + resource action).
- `API Summary Template.md` — one index page per resource, whose own header
  note says explicitly: *"Name the resulting file `<Resource> API
  Summary.md`"*.

Surveying every existing filename under `obsidian/PleaseBookMe/API/`
(150+ pages) gives these rules, applied consistently with zero exceptions
found:

1. **Location mirrors the backend subdomain.** Pages live at
   `API/<Domain>/<Subdomain>/`, matching the Spring package structure
   exactly — e.g. `core/service/` → `API/Core/Services/`,
   `core/bookingpolicy/` → `API/Core/Booking Policies/`. Both new pages for
   this task go into these two **existing** folders — no new folder.
2. **CRUD verbs use a fixed sentence shape, singular resource, with an
   article:**
   - `Create a <resource>.md` / `Create an <resource>.md`
   - `Get a <resource>.md` / `Get an <resource>.md` (single-resource read by
     path variable)
   - `Update a <resource>.md` / `Update an <resource>.md`
   - `Delete a <resource>.md` / `Delete an <resource>.md`
   - Article choice (`a` vs `an`) follows ordinary English phonetics on the
     resource noun (`a service`, `an account`, `an API key`, `an ecosystem`,
     `an out of office record`) — not a fixed rule, just read it aloud.
3. **Unscoped list reads use `Get all <resource-plural>.md`** — no article,
   plural noun (`Get all services` — note: currently absent for `Services`
   precisely because ISSUE-0005 deleted the endpoint it documented; this
   task's new endpoint is *not* this shape, see below).
4. **Filtered/alternate-key single-resource reads drop the article and use
   `Get <resource-singular> by <field>.md`.** This is the exact, already-
   established precedent for what this task is building — three real
   examples exist today: `Get role by id.md`, `Get user by email.md`,
   `Get user by phone.md`. Note the pattern: singular resource noun, no
   `a`/`an`, lowercase `by`, then the filter field in plain English (not the
   raw query-param/camelCase name).
5. **No existing precedent combines "by `<field>`" with a list result** —
   every current `by` page returns a single resource. This task introduces
   the first one (`GET /api/v1/services?organizationId=` returns a list).
   Resolve this by combining rules 3 and 4 exactly as they'd predict: keep
   the `by <field>` suffix from rule 4, but pluralize the resource noun the
   way rule 3 already does for lists. This gives, for this task specifically:
   - `GET /api/v1/services?organizationId=` → **`Get services by
     organization.md`** (plural, no article, `by organization` — not `by
     organizationId`, matching rule 4's plain-English-field-name precedent).
   - `GET /api/v1/booking-policies?serviceId=` → **`Get booking policy by
     service.md`** (singular, no article, `by service` — this one *does*
     match an existing single-resource `by` page exactly, no extrapolation
     needed).
6. **`API Summary.md` files get a new table row, not a rewrite.** Add
   `| GET | /api/v1/services?organizationId= | [[Get services by
   organization]] |` to `Service API Summary.md`, and the equivalent row to
   `Booking Policy API Summary.md`. Wikilinks (`[[Page Name]]`) match the new
   filename exactly, minus `.md`.

### Architectural

- Follow `CODING_CONVENTIONS.md`'s repository convention: `findBy<Column>`
  only when a lookup is actually needed (both are, here) — use Spring Data
  method derivation (`findByOrganizationOrganizationId`,
  `findByServiceServiceId` — the latter **already exists** on
  `BookingPolicyRepository`, added in the prior task; do not duplicate it).
- Keep both endpoints on their existing `@RequestMapping` base paths and
  existing `@GetMapping` methods where possible (Section 14 has the exact
  shape) rather than adding new sibling endpoints — a query parameter on the
  current list mapping, not a new route.

### Compatibility

- Adding a required `organizationId` query parameter to
  `GET /api/v1/services` is not a breaking change to any *existing* caller,
  because no working caller of that exact route currently exists (it 401s
  today, per ISSUE-0005). Treat this as introducing new behavior, not
  modifying a live contract.
- Adding an optional `serviceId` query parameter to
  `GET /api/v1/booking-policies` **is** additive and must not change the
  existing no-param response shape (`List<BookingPolicyResponse>`,
  unfiltered) at all.

---

## 7. DEPENDENCIES

### Required Components

- `security/identity/context/CurrentPrincipalProvider` (`requireUser()`).
- `organization/profile/repository/ProfileRepository`
  (`findAllByUserUserId` — already exists, added in the prior task).
- `core/service/repository/ServiceRepository` (needs the new
  `findByOrganizationOrganizationId`).
- `core/bookingpolicy/repository/BookingPolicyRepository`
  (`findByServiceServiceId` — already exists).

### Related Policies

- `SECURITY.md` — the disconnect()/ownership-check precedent this task's
  Service endpoint must follow.
- `.agents/issues/open/ISSUE-0005-sign-out-on-service.md` — the defect this
  task closes.
- `.agents/tasks/completed/TASK-0002-service-booking-policy-atomic-orchestration.md` —
  the prior task that introduced `resolveCurrentOwner()` and the
  `AmbiguousServiceOwnerException` behavior this task must reuse, not
  reinvent.

### Required Infrastructure

- None beyond what's already running. No migration, no Redis interaction.

---

## 8. INPUT CONTEXT

Read, in this order, before writing any code:

1. `.agents/issues/open/ISSUE-0005-sign-out-on-service.md` — full repro,
   root cause, and the two suggested-follow-up items this task partially
   implements.
2. `.agents/tasks/completed/TASK-0002-service-booking-policy-atomic-orchestration.md` —
   for the `resolveCurrentOwner()`/`AmbiguousServiceOwnerException` pattern
   this task must reuse on the Service endpoint, and for this file's own
   format precedent.
3. `SECURITY.md` — the `disconnect()` "indistinguishable from not found"
   precedent (§12) that governs how the Service endpoint must respond to a
   mismatched `organizationId`.
4. `CODING_CONVENTIONS.md` — repository `findBy<Column>` convention.
5. Current source, already inspected for this task and safe to treat as
   accurate:
   - `server/.../core/service/controller/ServiceController.java`
   - `server/.../core/service/service/BusinessServiceService.java` /
     `service/impl/BusinessServiceImpl.java` (in particular the private
     `resolveCurrentOwner()`/`OwnerContext` helpers — reuse, don't duplicate)
   - `server/.../core/service/repository/ServiceRepository.java`
   - `server/.../core/bookingpolicy/controller/BookingPolicyController.java`
   - `server/.../core/bookingpolicy/service/BookingPolicyService.java` /
     `service/impl/BookingPolicyServiceImpl.java`
   - `server/.../core/bookingpolicy/repository/BookingPolicyRepository.java`
   - `client/features/services/services/service-gateway.ts` (the
     `listMyServiceCatalogOnPlatform` function and its own doc comment)
   - `client/lib/platform-user.ts` (`resolveCurrentPlatformUser` — read-only,
     do not modify per Section 4)
   - `client/features/services/components/service-card.tsx` /
     `service-catalog-list.tsx` — confirms `ServiceCatalogEntry.bookingPolicy`
     is what already drives the "Duration"/"Policy" fields on each card; this
     is the concrete UI surface the user meant by "the user can directly see
     the service setting on the UI."
6. `obsidian/PleaseBookMe/API/API Template.md` and
   `obsidian/PleaseBookMe/API/API Summary Template.md` — the two universal
   templates every new documentation page in this task must be built from.
   Read them in full before writing either new page; do not freehand the
   section structure from memory of other pages.
7. `obsidian/PleaseBookMe/API/Core/Services/Service API Summary.md`,
   `obsidian/PleaseBookMe/API/Core/Booking Policies/Get all booking policies.md`,
   and `Booking Policy API Summary.md` — the documentation this task adds a
   new endpoint row/page to (see Section 6's naming analysis and Section 14).
   Also skim `Auth/Users/Get user by email.md` and `Auth/Roles/Get role by
   id.md` as the closest existing precedent for the new pages' shape.
8. **Skills to load and apply:**
   - `.skills/technologies/spring-boot/repositories/SKILL.md` — for the two
     new `findBy*` methods.
   - `.skills/technologies/spring-boot/controller-declaration/SKILL.md` —
     for `@RequestParam` usage (first query-param-filtered list endpoint in
     this codebase — check the file for `@RequestParam` conventions, e.g.
     `GoogleIntegrationController`, before deciding required-vs-optional).
   - `.skills/domains/authorization/identity-access-engineering/SKILL.md` —
     for the ownership-scoping rule on the Service endpoint.
   - `.skills/workflows/quality-code-comments/SKILL.md` — apply to the
     comment explaining *why* a mismatched `organizationId` returns an empty
     list instead of an error (non-obvious, security-motivated, exactly the
     kind of thing that skill exists for).
   - `.skills/workflows/code-review/SKILL.md` — for the review pass
     (Section 15).

---

## 9. FUNCTIONAL REQUIREMENTS

### Service List Endpoint

1. `GET /api/v1/services?organizationId={id}` returns `200` with a JSON
   array of `ServiceResponse` for every service in that organization, when
   `{id}` matches the caller's own derived organization.
2. The same request with an `{id}` that does **not** match the caller's own
   organization returns `200` with an empty array — never `403`/`404`, never
   the other organization's rows.
3. Omitting `organizationId` entirely returns `400` (standard Spring
   `@RequestParam` binding failure — no custom handling required, consistent
   with the codebase's existing "no global validation-failure handler"
   precedent).
4. A caller with profiles in more than one organization gets the same
   `409 AMBIGUOUS_SERVICE_OWNER` that `create`/`updateService` already
   produce, unless Section 6's escalation surfaces a reason this should
   differ for a list endpoint.

### Booking Policy Lookup

5. `GET /api/v1/booking-policies?serviceId={id}` returns `200` with a JSON
   array containing the one `BookingPolicyResponse` for that service, or an
   empty array if the service has no policy yet.
6. `GET /api/v1/booking-policies` (no query param) is completely unchanged —
   still returns every policy, unfiltered.

### Client

7. `listMyServiceCatalogOnPlatform` calls
   `GET /api/v1/services?organizationId=<derived id>` and, for each returned
   service, `GET /api/v1/booking-policies?serviceId=<that service's id>` —
   no remaining call to the unscoped `GET /api/v1/services` or the no-param
   `GET /api/v1/booking-policies`.
8. `/dashboard/services` renders the catalog for a real signed-in user
   without redirecting to `/login?session=expired`.

---

## 10. NON-FUNCTIONAL REQUIREMENTS

### Security

1. No code path returns another organization's services or reveals whether
   an organization id exists, per Section 6.
2. `organizationId` mismatch handling must be covered by a test — this is
   the one behavior in this task that would silently regress into a real
   IDOR if a future edit "simplified" it back to a raw repository call.

### Performance

3. The client now issues one list call plus N per-service booking-policy
   calls (N = services in the caller's organization) instead of two bulk
   calls. This is an accepted, explicit trade-off for a typical small
   per-organization service count — do not attempt to design a batch/`IN`-
   style endpoint in this task to avoid it; if N ever becomes a real problem,
   that is a follow-up, not a blocker here.

### Maintainability

4. `service-gateway.ts`'s "TEMPORARY WORKAROUND" doc comment must not survive
   unchanged if the workaround it describes no longer exists — update or
   remove it to match the real, post-fix behavior.
5. Comments explaining the empty-list-on-mismatch behavior must follow
   `quality-code-comments/SKILL.md` (why, not what).

---

## 11. ACCEPTANCE CRITERIA

### 1. Bug Fixed

Given a signed-in user with a valid session, when they navigate to
`/dashboard/services`, then the page renders the catalog and does **not**
redirect to `/login?session=expired`. This is the literal ISSUE-0005 repro —
re-run it exactly as documented there (browser navigation, then a `curl`
cookie-jar replay confirming no `307` to the login page) to confirm.

### 2. Service Scoping Is Real

Given two organizations A and B, each with at least one service, when a
caller belonging to A requests `GET /api/v1/services?organizationId=<B's id>`,
then the response is `200` with an empty array — never B's services, never
an error revealing B's existence.

### 3. Booking Policy Lookup Works

Given a service with a booking policy, when
`GET /api/v1/booking-policies?serviceId=<that id>` is called, then the
response contains exactly that one policy. Given a service with no policy,
the response is an empty array, not a `404`.

### 4. No-Param Booking Policy List Unaffected

Given the existing `GET /api/v1/booking-policies` (no query param), the
response is unchanged from today's behavior — every policy, unfiltered.

### 5. Documentation Matches Reality and Follows Convention

Two new pages exist, each a fully filled-in copy of `API Template.md`, named
exactly per Section 6's analysis:
`obsidian/PleaseBookMe/API/Core/Services/Get services by organization.md`
and `obsidian/PleaseBookMe/API/Core/Booking Policies/Get booking policy by
service.md`. Both `API Summary.md` files link to their new page via a
`[[Page Name]]` wikilink in the endpoints table. The Services page
documents the empty-list-on-mismatch behavior for `organizationId` as a
real, described case — not just the happy path.

### 6. Issue Closed

`ISSUE-0005-sign-out-on-service.md` is closed out per the `issue-generating`
skill's convention, pointing at this task contract as the resolving change.

---

## 12. VALIDATION

### Automated

- Run the server's existing test suite (`./gradlew test` from `server/`) —
  must pass with no regressions.
- Add/extend `BusinessServiceImplTest` (or the equivalent service test file)
  covering: matching `organizationId` returns the org's services; mismatched
  `organizationId` returns an empty list, not an exception; the
  multi-profile `AmbiguousServiceOwnerException` path if Section 6's
  escalation resolves in favor of keeping it.
- Add/extend `BookingPolicyServiceImplTest` covering: `serviceId` lookup
  returns the one matching policy; a service with no policy returns empty;
  the no-param path is unaffected.

### Manual / Integration

- Direct `curl` against the running backend (mirroring ISSUE-0005's own
  verification method): confirm `GET /api/v1/services?organizationId=`
  with a valid token for the caller's own org returns `200` with data, and
  with a foreign org id returns `200` with `[]`.
- Full browser repro: register/sign in a test user, create a service,
  navigate to `/dashboard/services`, confirm the catalog renders with the
  correct duration/price/policy fields sourced from the new endpoints, and
  confirm no redirect to login occurs on repeated visits.
- Cookie-jar `curl` replay (same technique used in ISSUE-0005): login →
  `/dashboard` → `/dashboard/services` → `/dashboard` again, confirming all
  three return `200`/render successfully with the same session cookies.

### Explicitly Not Required

- No new Flyway migration or migration test.
- No load/performance testing of the N-calls trade-off (Section 10).

---

## 13. ESCALATION

Stop and escalate to the orchestrator/reviewer rather than guessing when:

1. It's unclear whether `AmbiguousServiceOwnerException` (`409`) is the
   right response for a **list** endpoint given a multi-profile caller, or
   whether it should instead union services across all of the caller's
   organizations, or accept the ambiguity and require an explicit
   `organizationId` the caller confirms they belong to (which is arguably
   what this endpoint already does). Do not silently change
   `resolveCurrentOwner()`'s contract to resolve this — every other caller
   of that helper depends on its current behavior.
2. Any other caller of `ServiceRepository`/`BookingPolicyRepository` beyond
   what's listed in Section 8 needs updating — grep for existing usages
   before declaring either repository change complete.
3. The `service-catalog-list.tsx`/`service-card.tsx` rendering contract
   turns out to need a change beyond what `service-gateway.ts` already
   supplies today (`ServiceCatalogEntry.bookingPolicy`) — this task should
   not need to touch either component file; if it seems to, stop and
   confirm before proceeding.
4. Spring's default `400` for a missing required `@RequestParam` conflicts
   with any existing test or documented client expectation — verify the
   actual response body/shape before assuming.

Do not weaken the empty-list-on-mismatch requirement (Section 6, Section 9
item 2) to make a test pass or simplify the query — that is the exact
security property this task exists to preserve while re-adding a list
endpoint.

---

## 14. IMPLEMENTATION PLAN

1. **Read context** per Section 8, in order. Confirm `resolveCurrentOwner()`
   and `OwnerContext` in `BusinessServiceImpl` are still private helpers with
   the exact shape documented in `TASK-0002`'s output — reuse them directly
   rather than re-deriving ownership logic.

2. **Service repository.** Add to `ServiceRepository`:
   ```java
   List<ServiceEntity> findByOrganizationOrganizationId(BigInteger organizationId);
   ```

3. **Service interface + impl.** Add to `BusinessServiceService`:
   ```java
   List<ServiceEntity> getServicesByOrganizationId(BigInteger organizationId);
   ```
   Implement in `BusinessServiceImpl`:
   - Call `resolveCurrentOwner()` (existing private helper) to get the
     caller's own `OwnerContext`.
   - If `organizationId` doesn't equal
     `owner.organization().getOrganizationId()`, return `List.of()`
     immediately — do not query the repository at all for a foreign org id.
   - Otherwise, `serviceRepository.findByOrganizationOrganizationId(organizationId)`.
   - Write the one comment here (per `quality-code-comments/SKILL.md`)
     explaining *why* a mismatch returns empty rather than `403`/`404` —
     cite `SECURITY.md`'s disconnect() precedent and this task file.

4. **Service controller.** Add to `ServiceController`:
   ```java
   @GetMapping
   public List<ServiceResponse> getServices(@RequestParam BigInteger organizationId) {
       return businessService.getServicesByOrganizationId(organizationId).stream()
           .map(ServiceResponse::from)
           .toList();
   }
   ```
   Confirm this doesn't collide with the existing `@GetMapping("/{serviceId}")`
   — it shouldn't, Spring disambiguates by path shape, but verify by
   compiling and hitting both routes.

5. **Booking policy interface + impl.** Add to `BookingPolicyService`:
   ```java
   Optional<BookingPolicyEntity> getBookingPolicyByServiceId(BigInteger serviceId);
   ```
   Implement in `BookingPolicyServiceImpl` as a direct passthrough to the
   already-existing `bookingPolicyRepository.findByServiceServiceId(serviceId)`
   — no new repository method needed here, it was added in the prior task.

6. **Booking policy controller.** Extend the existing no-param
   `getBookingPolicies()` method (do not add a second `@GetMapping` on the
   same path — Spring would reject the ambiguous mapping):
   ```java
   @GetMapping
   public List<BookingPolicyResponse> getBookingPolicies(
       @RequestParam(required = false) BigInteger serviceId
   ) {
       if (serviceId != null) {
           return bookingPolicyService.getBookingPolicyByServiceId(serviceId)
               .map(BookingPolicyResponse::from)
               .map(List::of)
               .orElse(List.of());
       }
       return bookingPolicyService.getAllBookingPolicies().stream()
           .map(BookingPolicyResponse::from)
           .toList();
   }
   ```

7. **Client gateway.** In `service-gateway.ts`:
   - Add `getServicesByOrganizationOnPlatform(accessToken, organizationId)`
     wrapping `GET /api/v1/services?organizationId=`.
   - Add `getBookingPolicyForServiceOnPlatform(accessToken, serviceId)`
     wrapping `GET /api/v1/booking-policies?serviceId=`, returning
     `BookingPolicy | null` (unwrap the 0-or-1 array).
   - Rewrite `listMyServiceCatalogOnPlatform` to: resolve `organizationId`
     via `resolveCurrentPlatformUser` (unchanged), fetch services via the
     new org-scoped call, then `Promise.all` a per-service booking-policy
     fetch via the new function, assembling `ServiceCatalogEntry[]` exactly
     as today (same sort order, same shape) — but with two properly-scoped
     network calls per service (parallelized) instead of two unscoped bulk
     calls.
   - Update or remove the function's stale "TEMPORARY WORKAROUND" doc
     comment to reflect the new, properly-scoped reality.

8. **Server-side tests.** Add per Section 12 — mock `CurrentPrincipalProvider`
   for the matching-org and mismatched-org cases on the Service test file;
   straightforward repository-mock tests for the booking-policy lookup.

9. **Documentation pass** — use Section 6's naming analysis exactly, do not
   deviate from the filenames it derives:
   - Create `obsidian/PleaseBookMe/API/Core/Services/Get services by
     organization.md`, filled in from `API Template.md` end to end (every
     section: Description, Endpoint, Authentication, Headers, Success
     Response, Possible Errors — including the empty-array-on-mismatch case
     as a documented behavior, not just the happy path — Error Message,
     Example Request, Event Produced if applicable).
   - Create `obsidian/PleaseBookMe/API/Core/Booking Policies/Get booking
     policy by service.md`, same template, documenting both the 0-result
     and 1-result cases.
   - `obsidian/PleaseBookMe/API/Core/Services/Service API Summary.md` — add
     `| GET | /api/v1/services?organizationId= | [[Get services by
     organization]] |` to the endpoints table.
   - `obsidian/PleaseBookMe/API/Core/Booking Policies/Booking Policy API
     Summary.md` — add `| GET | /api/v1/booking-policies?serviceId= |
     [[Get booking policy by service]] |` to the endpoints table. Leave
     `Get all booking policies.md` itself untouched (Section 4 — its no-param
     behavior doesn't change; do not fold the new query param into that page,
     it gets its own page per Section 6 rule 4/5).
   - `.agents/issues/open/ISSUE-0005-sign-out-on-service.md` — close out per
     `.skills/workflows/issue-generating/SKILL.md`'s convention once the fix
     is verified.

10. **Final verification.** Re-run the full validation checklist
    (Section 12) end to end, including the exact ISSUE-0005 browser/`curl`
    repro. Produce the closing report described in Section 3.

---

## 15. REVIEW STRATEGY

### What to Review

1. **The `organizationId` mismatch behavior on the Service endpoint** — this
   is the single most important thing in this task. Confirm empirically
   (not just by reading) that a foreign `organizationId` returns an empty
   array, never the other organization's data and never an error that
   confirms/denies the other org's existence.
2. **No collision** between the new `@GetMapping` (with `@RequestParam`) and
   the existing `@GetMapping("/{serviceId}")` on `ServiceController` — both
   routes must resolve correctly.
3. **`GET /api/v1/booking-policies`'s no-param behavior is byte-for-byte
   unchanged** — the extension must be additive only.
4. **Client call count** — confirm via the Network tab that
   `listMyServiceCatalogOnPlatform` no longer calls the unscoped
   `GET /api/v1/services` or the no-param `GET /api/v1/booking-policies`.
5. **Documentation accuracy** — the new query parameters and the empty-list-
   on-mismatch behavior are both documented, not just the happy path.
6. **No scope creep** — `platform-user.ts`, `service-card.tsx`,
   `service-catalog-list.tsx` untouched (Section 4).

### How to Review

- Run `.skills/workflows/code-review/SKILL.md` (`/code-review`) against the
  diff — this touches an authorization boundary (the Service list endpoint),
  so treat it with the same seriousness as `TASK-0002`'s review.
- Independently re-derive the mismatch case: create two organizations via
  raw HTTP calls (not the frontend), then request the first organization's
  services using a token authenticated as a member of the second, and
  confirm the response is an empty array.
- Re-run the exact ISSUE-0005 browser + `curl` cookie-jar repro end to end
  and confirm it no longer reproduces.

### Core Components to Review

- `BusinessServiceImpl.getServicesByOrganizationId` and the
  `resolveCurrentOwner()` reuse — the security-critical path.
- `ServiceController.getServices` — the new mapping.
- `BookingPolicyController.getBookingPolicies` — the extended mapping.
- `service-gateway.ts` — the client's rewritten catalog assembly.
- The two new `obsidian/` pages (`Get services by organization.md`,
  `Get booking policy by service.md`) and the two updated `API Summary.md`
  files — confirm filenames and wikilinks match Section 6 exactly.

### Required Reviews (per `AGENTS.md` "Delegation" → "Review")

- **Security review is required** for the Service endpoint — it is a new
  authorization boundary, and getting the mismatch behavior wrong recreates
  a defect this session already fixed once elsewhere.
- **Architecture review is not required** — both changes are narrow,
  additive query extensions on existing controllers, no new bean, no schema
  change.
