# Task Contract

## 1. IDENTITY

Title: Atomic Service + BookingPolicy Creation with Server-Derived Ownership
Domain: Server / `core.service` + `core.bookingpolicy` (Spring Boot) — with required client and documentation follow-through
Priority: High
Risk: Medium
Status: Active

Resolves: `.agents/issues/open/ISSUE-0004-service-booking-policy-flow.md`

---

## 2. INTENT

Close ISSUE-0004 by making Service + BookingPolicy creation one atomic
server-side operation with server-derived ownership, **without introducing
a new orchestration bean or a new endpoint**. The atomicity and the
ownership derivation are implemented directly inside the existing
`BusinessServiceImpl.createService`/`updateService` methods (`core/service/services/impl/`),
reusing the existing `POST /api/v1/services` / `PUT /api/v1/services/{id}`
endpoints extended with an optional nested `bookingPolicy` field.

This is a **deliberate, explicit deviation** from `AGENTS.md`'s
cross-domain-orchestration convention (which would put this in a new
`service/core/ServiceCatalogService` bean) and from ISSUE-0004's own
"Suggested Follow-up" §2. The user who authored this task contract has
chosen to keep the change local to `BusinessServiceImpl` rather than add a
new bean. Do not silently "fix" this back to the `service/` pattern — if
you believe the deviation is a mistake, escalate per Section 13, don't
override it.

Three things must happen together, not just the code:

1. The atomic create/update behavior itself.
2. The documentation describing it (`obsidian/PleaseBookMe/API/Core/Services/*`,
   `obsidian/PleaseBookMe/API/Core/Booking Policies/*`, `SERVER_AGENTS.md`,
   `SECURITY.md` if the ownership-derivation pattern is referenced there) —
   this is not optional cleanup, it is a required deliverable (Section 3,
   Section 11).
3. Closing out `ISSUE-0004` itself (moving it to `.agents/issues/closed/`
   or annotating it resolved, per whatever convention `issue-generating`
   `SKILL.md` establishes — read it before touching the issue file).

---

## 3. DELIVERABLES

1. `BusinessServiceImpl.createService` builds and persists a `ServiceEntity`
   and (when the request supplies one) its `BookingPolicyEntity` in a single
   `@Transactional` unit — either both commit or neither does.
2. `userId`/`profileId`/`organizationId` are derived server-side from
   `CurrentPrincipalProvider.requireUser()` + `ProfileRepository.findByUserUserId(...)`
   on **both** `createService` and `updateService` — never trusted from the
   request body again. `ServiceRequest` no longer carries these three fields.
3. A new nested request shape carries the optional booking policy payload
   without a redundant/inapplicable `serviceId` field (see Section 14, Step 3).
4. `BookingPolicyRepository` gets `existsByServiceServiceId`, and
   `BookingPolicyServiceImpl.createBookingPolicy` (the **standalone**
   `POST /api/v1/booking-policies` path) gets the missing duplicate-check →
   `DuplicateBookingPolicyException`, wired into `GlobalExceptionHandler` →
   `409`. This closes ISSUE-0004's smaller, previously-optional gap (its
   "Suggested Follow-up" §1) as part of the same pass, since the new nested
   flow in `BusinessServiceImpl` needs the same exception type anyway.
5. Client (`client/`) updated to match the new contract: `service-gateway.ts`'s
   `createServiceWithPolicyOnPlatform` becomes a single `POST /services` call;
   `resolveCurrentPlatformUser`'s `userId`/`profileId`/`organizationId` are no
   longer sourced for this flow; `features/services/types/service.ts` types
   updated to match the new `ServiceRequest` shape.
6. Documentation updated (Section 11) — not deferred, not "left as a TODO".
7. `ISSUE-0004` closed out (moved/annotated per `issue-generating` `SKILL.md`).
8. A short summary to the orchestrator: files touched, validation run, and
   any residual known gaps (e.g. the multi-organization ambiguity flagged in
   Section 6/13).

---

## 4. SCOPE

### In Scope

- `server/src/main/java/com/pleasebookme/server/core/service/` — `ServiceRequest`,
  `BusinessServiceImpl`, `BusinessServiceService` (interface, if the method
  signature needs a `@Transactional` boundary marker or the nested DTO changes
  its shape).
- `server/src/main/java/com/pleasebookme/server/core/bookingpolicy/` —
  `BookingPolicyRepository` (new `existsBy*`), `BookingPolicyServiceImpl`
  (duplicate-check only — do not change its existing standalone CRUD shape
  otherwise), new `DuplicateBookingPolicyException`.
- `server/src/main/java/com/pleasebookme/server/global/handler/GlobalExceptionHandler.java` —
  one new `@ExceptionHandler` method, following the existing copy-paste pattern
  exactly (see Section 14, Step 6).
- A new nested DTO for the booking-policy payload embedded in `ServiceRequest`
  (naming/placement decided in Section 14, Step 3).
- `client/features/services/services/service-gateway.ts`,
  `client/features/services/types/service.ts`, and (only the parts of)
  `client/lib/platform-user.ts` that are no longer needed for this specific
  flow — do not delete `resolveCurrentPlatformUser`/`resolveUserId` outright,
  they are still used elsewhere (`listMyServiceCatalogOnPlatform`'s
  organization-scoping filter; grep for other callers before removing anything).
- `obsidian/PleaseBookMe/API/Core/Services/*.md`,
  `obsidian/PleaseBookMe/API/Core/Booking Policies/*.md`,
  `SERVER_AGENTS.md` (the `ServiceEntity CRUD` bullet under "Service &
  Controller conventions"), and `.agents/issues/open/ISSUE-0004-service-booking-policy-flow.md`.

### Out of Scope

- Any change to `POST /api/v1/booking-policies`'s standalone request/response
  shape beyond the duplicate-check (it keeps accepting/requiring `serviceId`
  as today — only the *new nested* path inside `ServiceRequest` avoids that
  field).
- A dedicated `service/core/ServiceCatalogService` bean, or any new endpoint
  (`POST /api/v1/services/with-policy` or similar) — explicitly rejected for
  this task, see Section 2.
- Resolving the pre-existing, unrelated authorization gap that
  `GET /api/v1/services` / `GET /api/v1/booking-policies` return every
  organization's rows with no scoping (`service-gateway.ts`'s own documented
  workaround, `listMyServiceCatalogOnPlatform`). Do not fix it opportunistically.
- Any change to `ScheduleServiceImpl`/`AvailabilityServiceImpl` or
  `ISSUE-0003` — same shape of bug, different issue, not this task.
- Any Flyway migration. `V126__add_booking_policy_service_unique.sql`
  already provides the DB-level guard; no schema change is required here.
- Adding `profileId`/`organizationId` to `UserPrincipal` — `SECURITY.md`
  documents this as a deliberate absence; do not revisit that decision here.

---

## 5. BOUNDARIES

- Do not touch `auth.users`, `auth.accounts`, or any Google OAuth
  (`security/oauth/google/`, `service/integration/`, `service/auth/`) code —
  unrelated bounded context.
- Do not modify `core/schedule/`, `core/availability/`, or any other
  `core/*` subdomain besides `service/` and `bookingpolicy/`.
- Do not introduce a second authorization/ownership-checking mechanism —
  reuse `CurrentPrincipalProvider`/`UserPrincipal` exactly as `SECURITY.md`
  describes; do not add a parallel "is this mine?" check elsewhere.
- Do not weaken or remove the existing `existsByOrganizationOrganizationIdAndSlug`
  duplicate-check on `ServiceRepository` — the new ownership derivation is
  additive to it, not a replacement.
- Do not silently drop the `V126` unique constraint's role as the data-layer
  backstop — the new application-level duplicate-check is defense in depth,
  not a substitute for it.

---

## 6. CONSTRAINTS

### Architectural

- Keep the orchestration **inside `BusinessServiceImpl`**, per Section 2 —
  this is the one explicit override of `AGENTS.md`'s "cross-domain flow →
  `service/<area>/`" rule for this task. Document the override at the call
  site with a short comment (see the `quality-code-comments` skill,
  Section 14 Step 8) so a future reader doesn't assume it was an oversight.
- `@Transactional` must be on a method reachable through the Spring-proxied
  bean from **outside** (i.e. on the public `createService`/`updateService`
  methods, invoked by `ServiceController` through the `BusinessServiceService`
  interface reference) — this is not a self-invocation case like
  `finalizeOnboarding` in `SECURITY.md` §14 (no private-method self-call is
  being introduced), but confirm this explicitly before implementing; if the
  design ends up needing an internal self-call, that self-invocation proxy
  pitfall applies and must be escalated (Section 13), not worked around
  silently.
- `ProfileRepository.findByUserUserId(BigInteger)` returns a single
  `Optional<ProfileEntity>`. If a user holds membership/profiles in **more
  than one organization**, this call is ambiguous (Spring Data will either
  throw `IncorrectResultSizeDataAccessException` on a non-unique result or
  — check the actual query derivation — silently return one arbitrary row).
  This is a known, pre-existing limitation carried over verbatim from
  ISSUE-0004's own suggested approach — it is not something this task is
  expected to solve (that would require request-scoped organization context,
  a larger change `SECURITY.md`'s "What's Deliberately Absent" section
  explicitly defers). **Do not silently paper over it** — confirm the actual
  runtime behavior of `findByUserUserId` against a multi-profile user during
  implementation, and document whatever you find as a known limitation in
  both the doc updates (Section 11) and your final report (Section 3, item 8).
  If it throws instead of degrading gracefully, that is worth flagging loudly,
  not fixing outside this task's scope — escalate per Section 13 if it's
  unclear whether a fix belongs here.

### Security

- The core defect being fixed **is** a security defect (cross-organization
  write via unvalidated FK ids) — treat this as security-sensitive and follow
  `SECURITY.md`'s stated architecture: only `CurrentPrincipalProvider` reads
  "who is calling," business code never touches Spring Security types
  directly.
- Do not accept `userId`/`profileId`/`organizationId` from the request body
  under any field name, including inside the new nested booking-policy
  payload — the whole point of this task is removing client-supplied
  ownership.
- `DuplicateBookingPolicyException`/`BookingPolicyNotFoundException` must
  never leak internal detail beyond the existing `ApiErrorResponse` shape
  (`status`/`message`/`data`/`client`/`timestamp`/`path`) — same as every
  other typed exception in `GlobalExceptionHandler`.

### Compatibility

- `ServiceRequest` dropping `userId`/`profileId`/`organizationId` is a
  **breaking API contract change** for `POST /api/v1/services` and
  `PUT /api/v1/services/{serviceId}`. There is no versioning scheme in this
  codebase to soften this — the client must be updated in the same change
  (Section 3, item 5), and the documentation must reflect the new contract
  exactly (Section 11), not describe both old and new shapes.
- Follow `CODING_CONVENTIONS.md` exactly for the new `DuplicateBookingPolicyException`
  (single `String message` constructor, no shared base class) and for
  `BookingPolicyRepository.existsByServiceServiceId` (standard Spring Data
  nested-path traversal through the `service` association into its PK,
  same shape as `ServiceRepository.existsByOrganizationOrganizationIdAndSlug`).

---

## 7. DEPENDENCIES

### Required Components

- `security/identity/context/CurrentPrincipalProvider` (`requireUser()`).
- `security/identity/principal/UserPrincipal` (`userId()` field — already
  present, see `obsidian/PleaseBookMe/Security/Identity/UserPrincipal.md`).
- `organization/profile/repository/ProfileRepository` (`findByUserUserId`).
- `core/service/repository/ServiceRepository`
  (`existsByOrganizationOrganizationIdAndSlug`, already present).
- `core/bookingpolicy/repository/BookingPolicyRepository` (needs the new
  `existsByServiceServiceId`).
- `global/handler/GlobalExceptionHandler`.

### Related Policies

- `SECURITY.md` — Authentication Identity & Principal Architecture (in
  particular the `UserPrincipal` section and "What's Deliberately Absent").

### Required Infrastructure

- None beyond what's already running (Postgres via docker-compose). No new
  migration, no Redis interaction.

### Required Services

- None new. Reuses existing Spring beans only.

---

## 8. INPUT CONTEXT

Read, in this order, before writing any code:

1. `.agents/issues/open/ISSUE-0004-service-booking-policy-flow.md` — the
   defect this task closes. Read in full; this task contract summarizes but
   does not replace it.
2. `.skills/workflows/issue-generating/SKILL.md` — for the correct convention
   to close out the issue file once the fix lands (Section 3, item 7).
3. `SECURITY.md` — sections on `UserPrincipal`, especially "What's
   Deliberately Absent" (why `organizationId`/`profileId` aren't on the
   principal) and the "Not Yet Consumed" note under `UserPrincipal`
   (`obsidian/PleaseBookMe/Security/Identity/UserPrincipal.md`) naming this
   exact gap.
4. `CODING_CONVENTIONS.md` — `create`/`update` standard method shape,
   duplicate-check-before-create rule, exception-handling pattern.
5. `SERVER_AGENTS.md` — the `ServiceEntity CRUD` and `BookingPolicyEntity CRUD`
   bullets under "Service & Controller conventions" (current documented
   behavior you are about to change) and the note on `TenantServiceImpl`'s
   "upgrade once a real repository exists" precedent (same category of
   incremental-hardening change).
6. Current source, already inspected for this task and safe to treat as
   accurate:
   - `server/.../core/service/services/impl/BusinessServiceImpl.java`
   - `server/.../core/service/services/BusinessServiceService.java`
   - `server/.../core/service/dto/ServiceRequest.java` / `ServiceResponse.java`
   - `server/.../core/service/controller/ServiceController.java`
   - `server/.../core/bookingpolicy/service/impl/BookingPolicyServiceImpl.java`
   - `server/.../core/bookingpolicy/repository/BookingPolicyRepository.java`
   - `server/.../core/bookingpolicy/dto/BookingPolicyRequest.java` / `BookingPolicyResponse.java`
   - `server/.../organization/profile/entity/ProfileEntity.java` /
     `repository/ProfileRepository.java`
   - `server/.../security/identity/principal/UserPrincipal.java`
   - `server/.../security/identity/context/CurrentPrincipalProvider.java`
   - `server/.../global/handler/GlobalExceptionHandler.java` (existing
     `ServiceNotFoundException`/`DuplicateServiceException`/`BookingPolicyNotFoundException`
     handlers — copy the pattern for the new one)
   - `client/features/services/services/service-gateway.ts`
   - `client/features/services/types/service.ts`
   - `client/lib/platform-user.ts`
7. `obsidian/PleaseBookMe/API/Core/Services/Create a service.md`,
   `Update a service.md`, `Service API Summary.md`, and
   `obsidian/PleaseBookMe/API/Core/Booking Policies/Create a booking policy.md`,
   `Booking Policy API Summary.md` — the documentation this task must bring
   back in sync with reality.
8. **Skills to load and actually apply, not just cite** (Section 15 covers
   how these fit into the review):
   - `.skills/workflows/quality-code-comments/SKILL.md` — apply this to every
     comment you write in this pass, especially the one documenting the
     deliberate `AGENTS.md` deviation (Section 6) and anything explaining the
     multi-profile ambiguity (Section 6). Do not restate what the code does.
   - `.skills/technologies/spring-boot/services/SKILL.md` — service-layer
     conventions for this codebase.
   - `.skills/technologies/spring-boot/repositories/SKILL.md` — for the new
     `existsByServiceServiceId` method.
   - `.skills/technologies/spring-boot/controller-declaration/SKILL.md` — in
     case the controller's `@Valid`/DTO wiring needs to change for the nested
     field.
   - `.skills/domains/authorization/identity-access-engineering/SKILL.md` —
     for the ownership-derivation/authorization pattern itself.
   - `.skills/workflows/code-review/SKILL.md` — for the review pass in
     Section 15.

---

## 9. FUNCTIONAL REQUIREMENTS

### Atomicity

1. `POST /api/v1/services` with a `bookingPolicy` field present creates both
   the `ServiceEntity` and its `BookingPolicyEntity` in one transaction: if
   the booking policy fails to persist (validation, duplicate, or otherwise),
   the service row is not committed either.
2. `POST /api/v1/services` with no `bookingPolicy` field (or an explicit
   `null`) behaves exactly as today — creates a Service with no policy, no
   behavior change for that path.

### Ownership Derivation

3. `userId` is always `CurrentPrincipalProvider.requireUser().userId()` —
   never read from the request body, on both `create` and `update`.
4. `profileId`/`organizationId` are always derived via
   `ProfileRepository.findByUserUserId(userId)` — never read from the request
   body, on both `create` and `update`.
5. `ServiceRequest` no longer has `userId`, `profileId`, or `organizationId`
   fields. Any remaining reference to them anywhere in `core/service/` is
   removed.

### Duplicate Handling

6. Creating a second `BookingPolicy` for a service that already has one —
   whether via the nested `ServiceRequest.bookingPolicy` path or the
   standalone `POST /api/v1/booking-policies` — returns a `409` with the
   standard `ApiErrorResponse` shape, not an uncaught `500`.

### Client

7. `createServiceWithPolicyOnPlatform` in `service-gateway.ts` issues exactly
   one HTTP call to `POST /api/v1/services`.
8. The client no longer sends `userId`, `profileId`, or `organizationId` in
   any `ServiceRequest` payload.

### Documentation

9. `obsidian/PleaseBookMe/API/Core/Services/Create a service.md` and
   `Update a service.md` show the new request/response body (no ownership
   fields, new nested `bookingPolicy` shape) and the new `409` error case.
10. `obsidian/PleaseBookMe/API/Core/Booking Policies/Create a booking policy.md`
    reflects that the `500` gap is closed (documents the real `409` now).
11. `ISSUE-0004` is closed out per the `issue-generating` skill's convention.

---

## 10. NON-FUNCTIONAL REQUIREMENTS

### Architecture

1. Must not introduce a new Spring bean/service class for this orchestration
   (Section 2) — the entire change lives in `BusinessServiceImpl` plus the
   small, scoped `BookingPolicyServiceImpl`/`BookingPolicyRepository`
   duplicate-check addition.
2. Must not change `BookingPolicyController`'s or `ServiceController`'s
   `@RequestMapping` paths or HTTP verbs.
3. Must not introduce a mapper/builder class — follow the existing pattern
   of building entities directly via Lombok builders inside the service impl.

### Security

4. Must not make ownership derivation optional/bypassable via any request
   field, header, or query parameter.
5. Must not expose `DataIntegrityViolationException` internals (SQL state,
   constraint name) in any API response.

### Maintainability

6. Comments added must follow `quality-code-comments/SKILL.md` — explain
   *why* (the deliberate architectural deviation, the multi-profile
   ambiguity), never *what*.
7. Documentation changes must leave no stale/contradictory trace of the old
   client-supplied-ownership contract anywhere in the touched `obsidian/`
   files.

---

## 11. ACCEPTANCE CRITERIA

### 1. Atomic Success

Given a valid `POST /api/v1/services` request with a nested `bookingPolicy`,
when the request is submitted by an authenticated user with exactly one
organization profile,
then both a `ServiceEntity` and a `BookingPolicyEntity` row exist afterward,
correctly linked, and the response reflects both.

### 2. Atomic Rollback

Given a `POST /api/v1/services` request with a nested `bookingPolicy` that
would violate a constraint on the policy side (e.g. a second policy for a
pre-existing `serviceId` — not reachable via `create` normally, but exercise
this through the `update` path or a direct duplicate-check unit test),
when the request is submitted,
then no `ServiceEntity` row is left behind either — the whole transaction
rolls back.

### 3. Ownership Cannot Be Spoofed

Given an authenticated caller who does not belong to organization X,
when they submit a `POST /api/v1/services` request (the request no longer
even has an `organizationId` field to populate — confirm this by attempting
to compile/send one and observing it is ignored or rejected as an unknown
field, not silently accepted),
then the created Service belongs to the caller's own organization, never X.

### 4. Duplicate Policy Returns 409

Given a service that already has a `BookingPolicy`,
when a second is created for the same `serviceId` via either the standalone
endpoint or the nested path,
then the response is `409 CONFLICT` with the standard `ApiErrorResponse`
body, not a `500`.

### 5. Update Path Consistency

Given `PUT /api/v1/services/{serviceId}`,
when submitted by the service's own organization member,
then `userId`/`profileId`/`organizationId` are re-derived from the caller's
principal exactly as in `create`, never taken from the request body.

### 6. Client Single Call

Given the browser network tab while creating a service with a policy through
the client UI,
when the create flow completes,
then exactly one request to `/api/v1/services` is observed — no separate
`/api/v1/booking-policies` call.

### 7. Documentation Matches Reality

Given the updated `obsidian/PleaseBookMe/API/Core/Services/*.md` and
`Booking Policies/*.md` files,
when compared against the actual request/response DTOs and error behavior,
they match exactly — no leftover `userId`/`profileId`/`organizationId` in
example bodies, and the `409` duplicate case is documented.

### 8. Issue Closed

Given `.agents/issues/`,
`ISSUE-0004-service-booking-policy-flow.md` is closed out per the
`issue-generating` skill's convention, with a note pointing at this task
contract as the resolving change.

---

## 12. VALIDATION

### Automated

- Run the server's existing test suite (`./gradlew test` from `server/`) —
  must pass with no regressions.
- Add/extend unit tests for `BusinessServiceServiceImplTest` (or create one
  if none exists — check first) covering: ownership derivation from a mocked
  `CurrentPrincipalProvider`, atomic rollback when the nested policy save
  fails, and the duplicate-check path.
- Add/extend a test for `BookingPolicyServiceImplTest` covering the new
  `existsByServiceServiceId` duplicate-check → `DuplicateBookingPolicyException`.

### Manual / Integration

- Exercise `POST /api/v1/services` with and without a nested `bookingPolicy`
  against a running local stack (docker-compose Postgres), confirming via
  direct SQL (`SELECT * FROM core.services`, `core.booking_policies`) that
  atomicity holds on both success and induced failure.
- Confirm a request carrying a spoofed `organizationId` field is either
  rejected by Jackson (unknown property, if strict binding is configured) or
  silently ignored with the server-derived value used instead — determine
  which behavior the codebase's existing Jackson config produces and document
  it; do not assume without checking.
- Run the client (`npm run dev` in `client/`) and create a service with a
  policy through the actual UI flow, confirming via the Network tab that only
  one request fires.

### Documentation Validation

- Re-read the four/five updated `obsidian/` files end-to-end after editing
  and confirm every example JSON body in them is something the current DTOs
  would actually accept/return.

### Explicitly Not Required

- No new Flyway migration or migration test — `V126` already covers the
  data-layer constraint.
- No load/performance testing — this is a correctness/security fix, not a
  performance-sensitive path.

---

## 13. ESCALATION

Stop and escalate to the orchestrator/reviewer rather than guessing when:

1. `ProfileRepository.findByUserUserId` behaves ambiguously or throws for a
   user with more than one organization profile, and it's unclear whether
   this task should (a) document it as a known limitation and proceed
   (the default expectation, per Section 6), or (b) block on a larger
   request-scoped-organization-context redesign. Do not invent a
   "pick the first one" workaround silently — that is exactly the kind of
   architectural ambiguity `AGENTS.md` says not to resolve unilaterally.
2. Making `ServiceRequest` drop `userId`/`profileId`/`organizationId` turns
   out to require touching `ServiceResponse` in a way that changes its
   external shape beyond what's already documented in Section 9/11 (the
   response shape should be unaffected — it flattens FKs off the persisted
   entity, not off the request — but confirm and escalate if that
   assumption breaks).
3. `BookingPolicyRepository.existsByServiceServiceId` collides in naming or
   behavior with any existing method — it should not, but verify against the
   real file before assuming.
4. Any other caller of `ServiceRequest`/`ServiceController.createService`/
   `updateService` exists beyond `service-gateway.ts` (e.g. a test fixture, a
   seed script, another client feature) that would also need updating — grep
   for `ServiceRequest(` and `ServiceController` usages across the whole repo
   before declaring the client change complete.
5. The `issue-generating` skill's convention for closing an issue is unclear
   or conflicts with this task's own numbering/status conventions.

Do not silently weaken the ownership-derivation requirement (Section 9,
items 3–5) to make a test pass or unblock implementation — that is the exact
security defect this task exists to close.

---

## 14. IMPLEMENTATION PLAN

1. **Read context.** Work through Section 8 in order. Confirm current
   behavior of `ProfileRepository.findByUserUserId` by reading its Spring
   Data method-derivation semantics (does a non-unique result throw or
   silently return one row?) — this determines how much you need to caveat
   in Section 6's constraint.

2. **Add the duplicate-check to the standalone BookingPolicy path first**
   (smallest, most isolated change, and a prerequisite for the nested path
   reusing the same exception type):
   - `core/bookingpolicy/exception/DuplicateBookingPolicyException.java` —
     plain `RuntimeException` subclass, single `String message` constructor,
     matching `DuplicateServiceException`'s exact shape.
   - `BookingPolicyRepository`: add
     `boolean existsByServiceServiceId(BigInteger serviceId);`.
   - `BookingPolicyServiceImpl.createBookingPolicy`: call the new
     `existsBy*` before resolving the `service` FK, throwing
     `DuplicateBookingPolicyException` on a hit — mirror
     `BusinessServiceImpl.createService`'s existing duplicate-check ordering
     exactly.
   - `GlobalExceptionHandler`: add a `@ExceptionHandler(DuplicateBookingPolicyException.class)`
     method immediately after the existing `BookingPolicyNotFoundException`
     handler, copying the `DuplicateServiceException` handler's body
     verbatim except for the type and `HttpStatus.CONFLICT` (already the
     right status).

3. **Design the nested request shape.** `BookingPolicyRequest` currently
   requires `serviceId` (`@NotNull`) — inappropriate for a payload nested
   inside `ServiceRequest`, where the service doesn't exist yet at
   validation time. Introduce a new record — suggested name
   `ServiceBookingPolicyRequest` in `core/bookingpolicy/dto/` — with the
   exact same fields as `BookingPolicyRequest` **minus** `serviceId`, same
   validation annotations otherwise. Add `ServiceRequest.bookingPolicy` as
   an optional (`nullable`, no `@NotNull`) field of this new type, annotated
   `@Valid` so nested validation actually runs. Decide whether
   `BookingPolicyRequest` should be refactored to compose this new record
   (e.g. `BookingPolicyRequest(BigInteger serviceId, ServiceBookingPolicyRequest policy)`
   via delegation) or just duplicate the field list — prefer avoiding
   duplication if it doesn't fight the existing record-based DTO convention,
   but don't over-engineer; a flat duplicate record is acceptable if
   composition would require unwinding `@Valid`/Jakarta validation in an
   awkward way. Use your judgment per `CODING_CONVENTIONS.md`'s "don't
   introduce abstractions without an architectural reason" — if duplication
   is simpler and this is the only place the shape is reused, duplicate it.

4. **Strip ownership fields from `ServiceRequest`.** Remove `userId`,
   `profileId`, `organizationId`. Add the new `bookingPolicy` field from
   step 3.

5. **Update `BusinessServiceImpl`:**
   - Inject `CurrentPrincipalProvider` and `BookingPolicyRepository` as new
     constructor dependencies (Lombok `@RequiredArgsConstructor` picks them
     up automatically once declared as `final` fields).
   - In `createService`: after the existing slug duplicate-check, derive
     `user`/`profile`/`organization` via
     `currentPrincipalProvider.requireUser().userId()` →
     `userRepository.findById(userId).orElseThrow(...)` →
     `profileRepository.findByUserUserId(userId).orElseThrow(ProfileNotFoundException::new)`
     → `profile.getOrganization()`. Remove the old `request.userId()`/
     `request.profileId()`/`request.organizationId()` reads entirely.
   - Build and `save()` the `ServiceEntity` exactly as today (unchanged
     field mapping otherwise).
   - If `request.bookingPolicy() != null`: check
     `bookingPolicyRepository.existsByServiceServiceId(service.getServiceId())`
     (defensive — always false for a brand-new service, but keeps the guard
     consistent with the standalone path per Section 6's "defense in depth"
     constraint) then build and `save()` a `BookingPolicyEntity` referencing
     the just-saved `service`, mapping fields from the nested DTO exactly as
     `BookingPolicyServiceImpl.createBookingPolicy` does today.
   - Add `@Transactional` (from `org.springframework.transaction.annotation.Transactional`)
     to `createService`. Confirm it is not calling itself/another
     `@Transactional` method on `this` — it isn't, per the current design,
     but re-verify once the method body is final.
   - Apply the identical ownership-derivation change to `updateService`
     (drop trust in `request.userId()`/etc., re-derive from principal).
     `updateService` does not touch booking policies — leave that entirely
     to the standalone `BookingPolicyController`, unchanged.
   - Write the one deliberate-deviation comment here (per
     `quality-code-comments/SKILL.md` — why, not what): something explaining
     that this orchestration deliberately lives here instead of a
     `service/core/` bean, per this task's explicit instruction, and pointing
     at `ISSUE-0004`/this task file for the reasoning — not a restatement of
     "this creates a service and a booking policy."

6. **Update `ServiceResponse`** only if the nested booking policy should be
   echoed back in the create response (recommended — a client just created
   both, it should see both without a second `GET`). If added, use the
   existing `BookingPolicyResponse.from(...)` helper, populated only when a
   policy was actually created (`null` otherwise). Confirm this doesn't
   violate `ServiceResponse`'s existing "flattened FK, not nested object"
   convention — a *newly created* nested resource in a *response* is
   different from an *input* FK, so this is not the same rule; use judgment,
   and if genuinely unsure whether to add it, escalate (Section 13) rather
   than guess.

7. **Server-side tests.** Add/extend unit tests per Section 12. Mock
   `CurrentPrincipalProvider.requireUser()` to return a fixed `UserPrincipal`.
   Verify the transactional rollback behavior — e.g. by forcing the
   `BookingPolicyRepository.save()` mock/stub to throw and asserting the
   `ServiceRepository.save()` result is not committed (via
   `@SpringBootTest` + `@Transactional` test rollback, or a targeted
   integration test against the real Postgres test container if one is
   already set up — check existing test infrastructure conventions first).

8. **Client changes:**
   - `client/features/services/types/service.ts`: remove `userId`,
     `profileId`, `organizationId` from `ServiceRequest`; add an optional
     `bookingPolicy` field mirroring the new nested shape (without
     `serviceId`).
   - `client/features/services/services/service-gateway.ts`:
     `createServiceWithPolicyOnPlatform` becomes a single
     `createServiceOnPlatform(accessToken, { ...service fields, bookingPolicy: {...} })`
     call; delete the `createBookingPolicyOnPlatform` call from this
     function specifically (leave the standalone
     `createBookingPolicyOnPlatform` export alone — it's still needed for
     the plain `POST /booking-policies` endpoint elsewhere) and update/remove
     the stale "Not atomic" doc comment on this function per
     `quality-code-comments/SKILL.md`.
   - Remove `resolveCurrentPlatformUser` usage from
     `createServiceWithPolicyOnPlatform` specifically — grep first for any
     other caller of this function/of `resolveCurrentPlatformUser` before
     touching `platform-user.ts` itself (per Escalation item 4); do not
     delete `resolveCurrentPlatformUser`/`resolveUserId` if
     `listMyServiceCatalogOnPlatform` (or anything else) still depends on
     them for the unrelated organization-scoping filter.

9. **Documentation pass** (do this after the code is working and tests pass,
   so the docs describe the real, verified behavior, not the intended one):
   - `obsidian/PleaseBookMe/API/Core/Services/Create a service.md` and
     `Update a service.md`: rewrite the `Body`/`Successful Response`/
     `Example Request` blocks to drop `userId`/`profileId`/`organizationId`
     and add the nested `bookingPolicy` object; update `Possible Errors` to
     drop `404 PROFILE_NOT_FOUND`/`404 ORGANIZATION_NOT_FOUND` if they're no
     longer client-triggerable the same way (they can still occur if the
     caller's own profile/org lookup fails — keep them, but reconsider the
     wording of `Description` to explain ownership is now server-derived)
     and add `409 BOOKING_POLICY_ALREADY_EXISTS`-equivalent if that's the
     `code` value the new handler actually produces (check
     `ApiErrorResponse`'s actual `message`/`code` convention before
     inventing a code string — copy how `DuplicateServiceException`'s
     message currently surfaces).
   - `Service API Summary.md`: update the one-line description if it still
     implies the client supplies ownership.
   - `obsidian/PleaseBookMe/API/Core/Booking Policies/Create a booking policy.md`:
     remove the paragraph noting the `500` gap; replace with the real `409`
     behavior and example error body.
   - `SERVER_AGENTS.md`: update the `ServiceEntity CRUD (core/service/)`
     bullet to reflect that `userId`/`profileId`/`organizationId` are no
     longer request fields, and add a short note (following the file's own
     dense-bullet style) describing the new atomic nested-policy behavior
     and pointing at this task file, mirroring how other entries in that
     file reference their own originating change.
   - `.agents/issues/open/ISSUE-0004-service-booking-policy-flow.md`: close
     out per `.skills/workflows/issue-generating/SKILL.md`'s convention —
     read that skill file first, it dictates the actual mechanics (may be a
     move to `.agents/issues/closed/`, may be an in-place status edit).

10. **Final verification.** Re-run the full validation checklist (Section 12)
    end to end. Produce the closing report described in Section 3, item 8.

---

## 15. REVIEW STRATEGY

### What to Review

1. **Correctness of the atomic boundary** — does `@Transactional` actually
   cover both saves, and does a forced failure on the policy side actually
   roll back the service row? This is the single most important thing to
   verify empirically, not just read.
2. **Ownership derivation** — no code path in `createService`/`updateService`
   reads `request.userId()`/`request.profileId()`/`request.organizationId()`
   anymore (they shouldn't compile, since the fields are removed — confirm
   the compiler is doing this enforcement for you, don't just trust a visual
   scan).
3. **Duplicate-check placement and exception wiring** — matches
   `CODING_CONVENTIONS.md`'s standard `create` step ordering; the new
   `GlobalExceptionHandler` method matches the copy-paste pattern of its
   neighbors exactly (same `ApiErrorResponse` field order, same
   `HttpStatus`).
4. **The deliberate architectural deviation** — confirm the comment
   documenting why this lives in `BusinessServiceImpl` instead of
   `service/core/` is present, accurate, and follows
   `quality-code-comments/SKILL.md` (explains *why*, cites `ISSUE-0004`/this
   task, doesn't restate the code).
5. **Documentation accuracy** — every touched `obsidian/` file's example
   JSON must be re-checked against the actual final DTO shapes, not the plan
   in this task file (the plan may have shifted during implementation,
   e.g. the DTO-composition decision in Step 3).
6. **Client/server contract match** — `service.ts` types match the final
   `ServiceRequest`/`ServiceResponse` shape exactly.
7. **No scope creep** — nothing in `core/schedule/`, `core/availability/`,
   `ISSUE-0003`, or the `GET` list-scoping gap was touched.

### How to Review

- Run `.skills/workflows/code-review/SKILL.md` (`/code-review`) against the
  diff at `high` effort, given this touches an authorization/ownership
  boundary — security-sensitive changes warrant broader coverage per that
  skill's own guidance.
- Independently re-derive: for a test user belonging to organization A,
  attempt (via a raw HTTP client, not the updated frontend, to bypass any
  client-side field stripping) to submit a `POST /api/v1/services` body that
  used to contain `organizationId: <org B's id>` and confirm the created row
  still belongs to organization A regardless of what extra JSON fields were
  sent.
- Confirm test coverage actually exercises the rollback path (Section 12) —
  a passing test suite that never forces the failure branch does not prove
  atomicity.

### Core Components to Review

- `BusinessServiceImpl` (`core/service/services/impl/`) — the center of this
  change.
- `BookingPolicyServiceImpl` / `BookingPolicyRepository` — the smaller,
  standalone-path fix.
- `GlobalExceptionHandler` — the one new handler method.
- `ServiceRequest` / the new nested booking-policy DTO — the contract shape.
- `service-gateway.ts` — the client's half of the contract change.
- The four/five updated `obsidian/` documentation files.

### Required Reviews (per `AGENTS.md` "Delegation" → "Review")

- **Security review is required** — this task closes a cross-organization
  authorization defect. Do not mark this task complete without one.
- **Architecture review is recommended, not strictly required** — the
  deliberate `AGENTS.md` deviation (Section 2/6) is small and explicitly
  authorized by this task contract, but an architecture reviewer should
  still confirm it was applied exactly as scoped and didn't expand into a
  larger restructuring.
