# Task Contract

## 1. IDENTITY

Title: Atomic Availability Ruleset Endpoint + Server-Side Ownership Enforcement
Domain: Backend / `core` bounded context (Schedule + Availability orchestration)
Priority: High
Risk: Medium-High
Status: COMPLETED

---

## 2. INTENT

Implement the fix described in `.agents/issues/open/ISSUE-0003-schedule-availability-flow.md`.

Today, creating a Schedule together with its Availability windows is done
entirely client-side as `1 + N` independent, non-transactional HTTP calls
(`POST /schedules` then one `POST /availabilities` per window), and both
`ScheduleServiceImpl.createSchedule`/`updateSchedule` and
`AvailabilityServiceImpl.createAvailability`/`updateAvailability` trust a
client-supplied `userId` field with **no validation against the
authenticated caller** — an unauthenticated-object-reference-style bug.

This task:

1. Introduces a single atomic orchestration endpoint,
   `POST /api/v1/availability-rulesets`, that creates one Schedule and its
   N Availability rows as one transactional unit and derives ownership
   from `CurrentPrincipalProvider`, never from the request body.
2. Closes the same ownership gap on the pre-existing single-row CRUD
   endpoints (`POST/PUT /schedules`, `POST/PUT /availabilities`) by
   removing the client-supplied `userId` field entirely and deriving it
   server-side there too, so the fix is not limited to the new batch path.
3. Documents the new service in Obsidian.
4. Applies the project's code-comment discipline
   (`.skills/workflows/quality-code-comments/SKILL.md`) to every new/
   modified file — comments explain *why*, not *what*, and are otherwise
   omitted.

---

## 3. DELIVERABLES

1. New package `service/availabilityruleset/` under
   `server/src/main/java/com/pleasebookme/server/service/availabilityruleset/`
   (the pre-scaffolded empty folder already present in the tree),
   following the interface/impl convention from `CODING_CONVENTIONS.md`:
   - `AvailabilityRulesetService` (interface)
   - `impl/AvailabilityRulesetServiceImpl` — `@Service`,
     `@RequiredArgsConstructor`, `@Transactional` on its own bean (not a
     private method — see Constraints)
   - `dto/AvailabilityRulesetRequest`, `dto/AvailabilityWindowRequest`
     (no `userId` field on either)
   - `dto/AvailabilityRulesetResponse` — a composite response wrapping
     the created `ScheduleResponse` and the list of created
     `AvailabilityResponse`s, built by reusing the existing
     `ScheduleResponse.from(...)` / `AvailabilityResponse.from(...)`
     factory methods (no new entity→DTO mapping logic)
   - `controller/AvailabilityRulesetController` — `@RestController`,
     `@RequestMapping("/api/v1/availability-rulesets")`, single
     `POST` endpoint, `@ResponseStatus(HttpStatus.CREATED)`,
     `@Valid @RequestBody`
2. Modified `ScheduleRequest`/`AvailabilityRequest`: `userId` field
   removed entirely.
3. Modified `ScheduleServiceImpl`/`AvailabilityServiceImpl`:
   `createSchedule`/`updateSchedule` and
   `createAvailability`/`updateAvailability` derive the owning user via
   `currentPrincipalProvider.requireUser().userId()` (a direct
   `BigInteger` field read, then a normal
   `userRepository.findById(...).orElseThrow(UserNotFoundException)`
   lookup — same shape as every other FK resolution in these classes,
   just fed from the principal instead of the request body).
4. Both classes gain a constructor-injected `CurrentPrincipalProvider`.
5. Obsidian documentation under
   `obsidian/PleaseBookMe/Services/Availability Ruleset/` (the
   pre-scaffolded empty folder already present) — see Section 9 for
   required content.
6. Unit tests:
   - New tests for `AvailabilityRulesetServiceImpl` under
     `server/src/test/java/com/pleasebookme/server/service/availabilityruleset/`
     (mirroring the impl package, consistent with every other test
     package in the tree — e.g.
     `service/auth/service/DefaultGoogleOnboardingServiceTest.java`
     mirrors `service/auth/service/impl/DefaultGoogleOnboardingService.java`).
   - `ScheduleServiceImplTest`/`AvailabilityServiceImplTest` (currently
     absent — first tests for these two classes) covering both the happy
     path and the ownership-derivation behavior.
7. A short summary of every file created/modified, plus test and build
   command output, reported back to the orchestrator.

---

## 4. SCOPE

### In Scope

- The new `service/availabilityruleset/` orchestration stack (service,
  impl, DTOs, controller, exception if needed).
- Removing `userId` from `ScheduleRequest`/`AvailabilityRequest` and
  updating `ScheduleServiceImpl`/`AvailabilityServiceImpl` to derive it
  from `CurrentPrincipalProvider`.
- Obsidian documentation for the new service.
- Unit tests for all new/modified service classes.
- Comment quality pass on touched files per
  `quality-code-comments/SKILL.md`.

### Out of Scope

- The client-side changes described in ISSUE-0003's Suggested Follow-up
  #6 (`availability-gateway.ts`, `lib/platform-user.ts`). This task is
  server-only; a follow-up client task consumes the new endpoint once
  this contract is complete.
- ISSUE-0004 (referenced in ISSUE-0003 as separate, unrelated
  server-side identity work) — do not investigate or fix it here.
- Any change to `core.schedules`/`core.availabilities` migrations or
  table shape — this is an application-layer fix, not a schema change.
- Adding a `Duplicate*Exception` to Schedule/Availability/the new
  ruleset domain — neither `V26__core_schedules.sql` nor
  `V27__core_availabilities.sql` declares a unique constraint beyond
  each table's own PK (per the existing `SERVER_AGENTS.md` precedent for
  these two entities), and the ruleset is a pure orchestration wrapper
  with nothing of its own to deduplicate.
- Rate limiting, pagination, or bulk-update/bulk-delete variants of the
  ruleset concept — creation only, matching the issue's scope.
- Widget-actor support for this endpoint — `WidgetPrincipal` has no
  concept of owning a Schedule; `CurrentPrincipalProvider.requireUser()`
  is the correct call here specifically because this flow is
  user-only, and it already throws `ForbiddenActorException` for any
  non-`UserPrincipal` actor without extra code.

---

## 5. BOUNDARIES

Work must stay within:

- `server/src/main/java/com/pleasebookme/server/service/availabilityruleset/`
  (new)
- `server/src/main/java/com/pleasebookme/server/core/schedule/dto/ScheduleRequest.java`
- `server/src/main/java/com/pleasebookme/server/core/schedule/service/impl/ScheduleServiceImpl.java`
- `server/src/main/java/com/pleasebookme/server/core/availability/dto/AvailabilityRequest.java`
- `server/src/main/java/com/pleasebookme/server/core/availability/service/impl/AvailabilityServiceImpl.java`
- `server/src/test/java/com/pleasebookme/server/service/availabilityruleset/` (new)
- `server/src/test/java/com/pleasebookme/server/core/schedule/` (new)
- `server/src/test/java/com/pleasebookme/server/core/availability/` (new)
- `obsidian/PleaseBookMe/Services/Availability Ruleset/`

### Excluded Boundaries

- Do not modify `ScheduleController`/`AvailabilityController` — dropping
  `userId` from the request DTOs requires no controller change, since
  controllers only translate DTO↔service.
- Do not modify `ScheduleEntity`/`AvailabilityEntity` — the FK/column
  shape is unchanged, only *who supplies* the owning user id changes.
- Do not modify `UserPrincipal`, `CurrentPrincipalProvider`, or any
  other authentication-pipeline class — this task consumes
  `requireUser().userId()`, it does not change how principals are built.
- Do not introduce a second orchestration pattern for atomic multi-entity
  writes — reuse the `@Transactional`-own-bean shape already established
  by `DefaultGoogleOnboardingService.finalizeOnboarding` (see
  `SECURITY.md` §14) rather than inventing a new one.
- Do not touch `GlobalExceptionHandler`'s existing
  `ScheduleNotFoundException`/`AvailabilityNotFoundException` handlers
  unless a genuinely new exception type is introduced for the ruleset
  domain (see Section 14 for when that is/isn't needed).
- Do not delete or rewrite `.agents/issues/open/ISSUE-0003-...` — move it
  to `.agents/issues/closed/` only after this task is verified complete
  (see Section 15), not as part of implementation.

---

## 6. CONSTRAINTS

### Architectural

- This is a cross-domain flow (coordinates `ScheduleRepository`,
  `AvailabilityRepository`, `UserRepository`) — per `AGENTS.md`'s package
  convention, it belongs under `service/<area>/`, not inside
  `core/schedule/service/` or `core/availability/service/`. The
  pre-scaffolded `service/availabilityruleset/` folder is the correct
  home; do not build it inside either bounded-context package instead.
- Follow `CODING_CONVENTIONS.md` exactly for the new DTOs: `Request`
  types are records, validation annotations map directly off the
  migration columns they ultimately populate
  (`ScheduleEntity.title`→`@NotBlank @Size(max = 255)`,
  `AvailabilityEntity.days`→`@NotNull Integer[]` becomes
  `@NotNull List<Integer>` per the issue's explicit guidance — see
  below —, `startTime`/`endTime`→`@NotNull LocalTime`).
- `AvailabilityWindowRequest` must use `List<Integer> days`, not
  `Integer[] days`, even though `AvailabilityEntity.days` is an array
  column — array-typed record fields get broken `equals`/`hashCode`,
  and this DTO has no direct entity mapping obligation the way
  `AvailabilityResponse.days` does. Convert `List<Integer>` →
  `Integer[]` only at the point each `AvailabilityEntity` is built.
- `AvailabilityRulesetRequest`'s nested `List<AvailabilityWindowRequest>
  windows` field must be annotated so nested validation actually fires
  — a bare `List<...>` without cascading validation leaves nested
  `@NotNull`/`@NotEmpty` inert. Use `@Valid` on the list field itself
  (Jakarta Validation cascades into each list element when the
  container field carries `@Valid`), and `@NotEmpty` on the list to
  reject a ruleset with zero windows.
- `finalizeOnboarding`'s two load-bearing facts both apply here
  verbatim: the create method must be `@Transactional` (a Schedule +
  its Availability rows must commit or roll back together), and that
  method must live on its own `@Service` bean, never as a private
  method invoked from within the same class — Spring does not proxy
  self-invocation, so a private `@Transactional` method silently runs
  with no transaction at all.
- No new `Duplicate*Exception` for the ruleset domain (see Scope/Out of
  Scope) — only a `*NotFoundException`, if one is genuinely needed
  beyond the `UserNotFoundException` already thrown by the FK
  resolution. In practice, a ruleset create only reads the caller's own
  `UserEntity` (already validated by authentication) and writes new
  rows — evaluate whether any new exception type is needed at all
  before adding one for symmetry.

### Security

- `AvailabilityRulesetRequest`/`AvailabilityWindowRequest` must not carry
  a `userId` field under any name — the entire point of this task is
  that ownership is never client-supplied.
- `ScheduleRequest`/`AvailabilityRequest` lose `userId` as a breaking
  DTO change. Confirm no other caller in the codebase (search for
  `.userId(` on both entities' builders and for `ScheduleRequest`/
  `AvailabilityRequest` constructions) depends on `userId` being
  present before removing it — see Escalation if any such caller
  exists that this task did not anticipate.
- `currentPrincipalProvider.requireUser()` throws
  `ForbiddenActorException` for a non-`UserPrincipal` actor (e.g. a
  future Widget caller) and `UnauthenticatedException` for no
  principal at all — both already mapped in
  `GlobalExceptionHandler`. Do not add redundant null-checks or a new
  exception path for "no principal" in the new/modified service code;
  rely on the existing provider contract.
- Do not weaken or bypass Spring Security's existing bearer-token
  requirement on `/api/v1/schedules`, `/api/v1/availabilities`, or the
  new `/api/v1/availability-rulesets` to make testing easier.

### Compatibility

- `ScheduleResponse`/`AvailabilityResponse` are unchanged — both already
  flatten `userId` from the persisted entity's `user` association, so
  removing `userId` from the *Request* side has no effect on API
  response shape for existing single-row CRUD callers.
- Existing `GET`/`DELETE` verbs on both domains are unaffected.

---

## 7. DEPENDENCIES

### Required Components

- `security/identity/context/CurrentPrincipalProvider` (interface) /
  `DefaultCurrentPrincipalProvider` (impl) — already implemented, used
  as-is via `requireUser().userId()`.
- `security/identity/principal/UserPrincipal` — already carries
  `userId: BigInteger`, no changes needed.
- `core/schedule/repository/ScheduleRepository`,
  `core/availability/repository/AvailabilityRepository`,
  `auth/user/repository/UserRepository` — all already exist as plain
  `JpaRepository`s, no new repository methods required.
- `core/schedule/dto/ScheduleResponse`, `core/availability/dto/AvailabilityResponse`
  — reused as-is for the composite ruleset response.

### Related Policies

- `SECURITY.md` §14 (Google One-Shot Registration) — source of the
  `@Transactional`-own-bean precedent this task must follow.
- `CODING_CONVENTIONS.md` — DTO field selection, validation mapping,
  service method conventions.
- `.skills/workflows/quality-code-comments/SKILL.md` — comment
  discipline for every new/modified file in this task.

### Required Infrastructure

- None beyond the existing local Postgres (via
  `infrastructure/docker/compose.yml`) needed to run
  `ScheduleServiceImplTest`/`AvailabilityServiceImplTest`/
  `AvailabilityRulesetServiceImplTest` if any are written as
  Spring-context/integration tests rather than pure Mockito unit tests
  — prefer pure Mockito unit tests (mocking the three repositories and
  `CurrentPrincipalProvider`) consistent with the existing
  `DefaultGoogleOnboardingServiceTest` style, since nothing here
  requires a real database to verify.

---

## 8. INPUT CONTEXT

Read before implementing, in this order:

1. `.agents/issues/open/ISSUE-0003-schedule-availability-flow.md` — the
   defect this task fixes; read in full, it is the authoritative
   problem statement.
2. `CODING_CONVENTIONS.md` (repo root) — DTO field selection,
   validation-annotation-per-column-type table, Service `create`/
   `update` method shape, exception-handling conventions.
3. `AGENTS.md` (repo root) — package-structure rule for cross-domain
   `service/<area>/` flows vs. bounded-context `service/`.
4. `SECURITY.md` §14 ("Google One-Shot Registration" → "Transaction
   Boundary") — the `@Transactional`-own-bean precedent and *why* it
   matters (Spring self-invocation proxying).
5. `SERVER_AGENTS.md` — search for "ScheduleEntity CRUD" and
   "AvailabilityEntity CRUD" bullets for the existing conventions on
   these two domains (no `Duplicate*Exception`, `days` as `Integer[]`,
   etc.) and for the general `BookingServiceImpl`/`WidgetOriginServiceImpl`
   "resolve-or-null helper" precedent (not directly applicable here
   since this flow has no nullable FK, but useful pattern context).
6. Current source, already inspected during context-gathering for this
   contract:
   - `core/schedule/service/impl/ScheduleServiceImpl.java`
   - `core/schedule/dto/ScheduleRequest.java`
   - `core/availability/service/impl/AvailabilityServiceImpl.java`
   - `core/availability/dto/AvailabilityRequest.java`
   - `security/identity/context/CurrentPrincipalProvider.java` +
     `DefaultCurrentPrincipalProvider.java`
   - `service/auth/service/GoogleOnboardingService.java` +
     `impl/DefaultGoogleOnboardingService.java` (the reference
     `@Transactional`-own-bean pattern)
7. `.skills/workflows/quality-code-comments/SKILL.md` — apply this to
   every file touched, not only newly created ones.
8. Existing Obsidian docs for structure/tone reference:
   `obsidian/PleaseBookMe/Security/OAuth2/FinalizeOnboarding Explained.md`
   (a service-flow explainer doc of comparable scope to what's expected
   for the ruleset service) and
   `obsidian/PleaseBookMe/API/Core/Schedules/`,
   `obsidian/PleaseBookMe/API/Core/Availabilities/` (existing API-level
   docs for the two domains this orchestrates — do not duplicate their
   content, link/reference instead).

---

## 9. FUNCTIONAL REQUIREMENTS

### 1. Atomic Ruleset Creation

1.1. `POST /api/v1/availability-rulesets`, authenticated (Bearer), accepts
`AvailabilityRulesetRequest { String title, String timezone (optional),
List<AvailabilityWindowRequest> windows }` where each window is
`{ List<Integer> days, LocalTime startTime, LocalTime endTime }`.

1.2. The endpoint creates exactly one `ScheduleEntity` and one
`AvailabilityEntity` per window element, all owned by the authenticated
caller, in a single database transaction.

1.3. If any part of the write fails (e.g. a constraint violation on one
window), no Schedule and no Availability row from this request persists
— full rollback, not partial commit.

1.4. The response is `201 Created` with a body containing the created
Schedule (via `ScheduleResponse.from(...)`) and the list of created
Availabilities (via `AvailabilityResponse.from(...)` per row).

### 2. Server-Derived Ownership (New Endpoint)

2.1. The ruleset endpoint never reads a `userId` from the request body —
there is no such field on `AvailabilityRulesetRequest`/
`AvailabilityWindowRequest`.

2.2. The owning user is resolved via
`currentPrincipalProvider.requireUser().userId()`, then looked up via
`userRepository.findById(...)` exactly like any other FK resolution.

### 3. Server-Derived Ownership (Existing CRUD)

3.1. `ScheduleRequest`/`AvailabilityRequest` no longer have a `userId`
field.

3.2. `ScheduleServiceImpl.createSchedule`/`updateSchedule` and
`AvailabilityServiceImpl.createAvailability`/`updateAvailability` derive
the owning/associated user the same way as 2.2 — a caller can no longer
attribute a Schedule or Availability row to another user's id via either
the batch or the single-row endpoints.

### 4. Documentation

4.1. `obsidian/PleaseBookMe/Services/Availability Ruleset/` contains at
least one Markdown file describing: what problem this service solves
(link/reference ISSUE-0003), the request/response shape, the
transaction boundary and why it is on its own bean (mirroring the style
of `FinalizeOnboarding Explained.md`), and the ownership-derivation
behavior (why `userId` is never accepted from the client).

---

## 10. NON-FUNCTIONAL REQUIREMENTS

### Architecture

- Must not introduce a second `@Transactional`-orchestration pattern
  distinct from the `finalizeOnboarding` precedent.
- Must not place the new service inside `core/schedule/` or
  `core/availability/` — it is cross-domain orchestration and belongs
  under `service/availabilityruleset/`.
- Must not add fields to `UserPrincipal`, `AuthenticatedPrincipal`, or
  any authentication-pipeline type.

### Security

- Must not accept a client-supplied ownership identifier anywhere in
  this task's new or modified request DTOs.
- Must not leak whether a given numeric user id exists as a
  side-channel (not applicable here since `userId` is no longer
  client-input, but keep `UserNotFoundException` messages consistent
  with the existing style used elsewhere in these two service classes —
  do not add a message format that echoes back untrusted input
  differently than the existing convention).

### Maintainability / Code Quality

- Every new or modified file must comply with
  `.skills/workflows/quality-code-comments/SKILL.md`: no comments
  restating what the code does; comments only where a business
  decision, non-obvious constraint, or architectural precedent
  (e.g. the self-invocation trap) needs explaining. Grep the diff for
  comment density before reporting completion — a comment-per-line
  pattern is a signal to remove, not tune.
- DTOs, service, and controller must match the exact structural
  conventions in `CODING_CONVENTIONS.md` (records for DTOs, interface +
  `Impl` naming, constructor injection via `@RequiredArgsConstructor`).

---

## 11. ACCEPTANCE CRITERIA

### 1. Atomicity

Given a valid `AvailabilityRulesetRequest` with 3 windows where the 3rd
window's data is malformed such that Hibernate throws on flush, when
`POST /api/v1/availability-rulesets` is called, then no `ScheduleEntity`
and no `AvailabilityEntity` row from that request exists afterward
(verified by a unit test that asserts `saveAll`/`save` is invoked inside
one transactional boundary and a simulated failure produces no partial
persistence — a Mockito-level assertion on the service, not necessarily
a live-DB integration test).

### 2. Ownership Cannot Be Spoofed (New Endpoint)

Given an authenticated caller with `userId = 42`, when
`POST /api/v1/availability-rulesets` is called with any request body,
then the created Schedule's `user` association is `42` regardless of
any other field in the request — because no field in the request can
express a different user id at all.

### 3. Ownership Cannot Be Spoofed (Existing CRUD)

Given an authenticated caller with `userId = 42`, when
`POST /api/v1/schedules` or `POST /api/v1/availabilities` is called,
then the created row's `user` association is `42`, derived from
`CurrentPrincipalProvider`, not from any request field (since `userId`
no longer exists on either `Request` DTO, this is enforced by the
compiler/schema, not by runtime validation).

### 4. Response Shape

Given a successful ruleset creation with 2 windows, when the response is
inspected, then it contains one `ScheduleResponse` and exactly 2
`AvailabilityResponse` entries, each produced via the existing
`from(...)` factory methods (no new mapping logic diverges from what
`ScheduleController`/`AvailabilityController` already produce for a
single row).

### 5. Validation

Given an `AvailabilityRulesetRequest` with an empty `windows` list, when
`POST /api/v1/availability-rulesets` is called, then the request is
rejected before reaching the service layer (`@NotEmpty` + `@Valid`
cascading validation), consistent with existing `@Valid @RequestBody`
behavior elsewhere in the codebase.

### 6. Transaction Boundary Placement

Given a code review of `AvailabilityRulesetServiceImpl`, the
`@Transactional` method that performs the Schedule + Availability writes
is confirmed to live on the service's own `@Service` bean (not a
private method called from within another public method of the same
class).

### 7. Test Coverage

Given the standard test command (Section 12), `AvailabilityRulesetServiceImplTest`,
`ScheduleServiceImplTest`, and `AvailabilityServiceImplTest` all exist,
compile, and pass — each covering at minimum: a successful create
deriving `userId` from a mocked `CurrentPrincipalProvider`, and a
not-found path for the resolved user.

### 8. Documentation Exists

Given `obsidian/PleaseBookMe/Services/Availability Ruleset/`, at least
one Markdown file exists there satisfying Functional Requirement 4.

---

## 12. VALIDATION

### Build / Test

Run from `server/`:

- `./gradlew compileJava compileTestJava` — confirm the DTO field
  removal doesn't break any other compilation unit that referenced
  `ScheduleRequest.userId()`/`AvailabilityRequest.userId()` (search the
  codebase for such references before removal, per Section 6's
  Security constraint).
- `./gradlew test --tests "*ScheduleServiceImplTest*" --tests "*AvailabilityServiceImplTest*" --tests "*AvailabilityRulesetServiceImplTest*"`
- Full `./gradlew test` to confirm no regression elsewhere (e.g. any
  existing test fixture that constructs a `ScheduleRequest`/
  `AvailabilityRequest` with the now-removed `userId` argument).

### Manual / Integration (optional but recommended)

- With the local Docker-composed Postgres running
  (`spring.docker.compose` auto-start), exercise
  `POST /api/v1/availability-rulesets` via an authenticated request
  (e.g. `curl` with a valid Bearer token from `/api/v1/auth/login`) and
  confirm: a 201 response with the expected composite body, and that
  the `user_id` column on both the resulting `core.schedules` row and
  every `core.availabilities` row matches the authenticated caller's
  numeric id, not any value that could be placed in the request body.
- Attempt the same call with a deliberately malformed final window
  (e.g. `endTime` before `startTime` — note: no such check exists today
  per the migration, so this may instead be tested by forcing a
  simulated repository exception in a unit test rather than relying on
  a real constraint) to confirm rollback.

### Explicitly Required

- Do not claim tests passed without having executed them — per
  `AGENTS.md`'s Prohibited Behavior list. Record actual command output.

---

## 13. ESCALATION

Stop and report to the orchestrator, rather than resolving silently,
when:

- Removing `userId` from `ScheduleRequest`/`AvailabilityRequest` breaks
  a caller this contract did not anticipate (e.g. a seed script, an
  integration test fixture, or a client contract test asserting the
  field's presence). Report the exact reference found rather than
  silently keeping the field "just in case."
- Any ambiguity arises about whether `AvailabilityWindowRequest.days`
  should validate window overlap or day-value range (e.g. rejecting
  `8` when only `0–6` is meaningful) — the migration and existing
  `AvailabilityRequest` impose no such check today, and inventing one
  would violate `CODING_CONVENTIONS.md`'s "don't invent constraints the
  migration doesn't declare" rule. Escalate rather than deciding
  unilaterally to add or skip such validation.
- The atomicity requirement seems to require anything beyond a single
  `@Transactional` method with `saveAll` (e.g. explicit
  `EntityManager.flush()` calls, manual rollback handling, or a saga/
  outbox pattern) — that would indicate a misunderstanding of Spring's
  declarative transaction model and must be checked before proceeding.
- Any change to `SecurityConfig`, CORS, or the authentication filter
  chain seems necessary to make the new endpoint reachable — it should
  not be; `/api/v1/availability-rulesets` requires the same Bearer-token
  handling every other authenticated `/api/v1/*` endpoint already gets
  for free. If it doesn't, escalate before touching security
  configuration.
- Any doubt about whether this task's DTO change constitutes a
  "silently changed public API" under `AGENTS.md`'s Prohibited
  Behavior — it is a deliberate, documented breaking change authorized
  by this contract, but if a consumer beyond the ones already
  identified in Boundaries/Scope turns up, stop and confirm before
  proceeding.

Do not silently resolve architectural or security ambiguity. When in
doubt, produce the smallest coherent change and ask.

---

## 14. IMPLEMENTATION PLAN

1. **Confirm no other caller depends on the fields being removed.**
   Search the full server tree (`grep -rn "ScheduleRequest(" `,
   `grep -rn "AvailabilityRequest(" `, `grep -rn "\.userId()" ` scoped to
   files importing either DTO) to build a complete list of construction
   sites. Expect zero pre-existing tests (confirmed during context
   discovery — no `ScheduleServiceImplTest`/`AvailabilityServiceImplTest`
   exist yet) but verify no seed/migration-adjacent Java code or
   `ServerApplicationTests` constructs either DTO.

2. **Remove `userId` from `ScheduleRequest`.** Delete the field and its
   `@NotNull` annotation; keep `title`/`timezone` unchanged.

3. **Update `ScheduleServiceImpl`.**
   - Inject `CurrentPrincipalProvider` via the constructor
     (`@RequiredArgsConstructor` picks it up automatically once the
     field is added).
   - In `createSchedule`, replace
     `userRepository.findById(request.userId())` with
     `userRepository.findById(currentPrincipalProvider.requireUser().userId())`.
   - In `updateSchedule`, apply the same replacement. Consider whether
     `update` should even re-resolve the user at all — full-replace
     semantics normally re-resolve every FK from the request, but here
     the FK is no longer client-suppliable, so re-resolving it from the
     *current* caller on every update would let a second caller silently
     reassign an existing Schedule's ownership merely by calling
     `PUT`. Decide and document: the safer default is to **not**
     overwrite `schedule.user` on update at all (leave the original
     owner untouched, since there is no legitimate reason for a `PUT`
     to change who owns the row) — only mutate `title`/`timezone`. If
     this diverges from a strict full-replace reading of
     `CODING_CONVENTIONS.md`, treat it as intentional per this
     contract's Security constraints, not an oversight.

4. **Repeat steps 2–3 for `AvailabilityRequest`/`AvailabilityServiceImpl`**,
   noting `AvailabilityServiceImpl` resolves both `user` and `schedule`
   — only `user` resolution changes; `schedule` resolution from
   `request.scheduleId()` is untouched (a Schedule id is not an
   ownership assertion by itself, it is validated implicitly by the
   fact that a caller can only meaningfully use their own Schedule ids
   returned to them — no additional cross-check is required by this
   contract, but note it in the Obsidian doc as a known scope boundary
   if it stands out during implementation).

5. **Design the new DTOs.**
   - `AvailabilityWindowRequest(List<Integer> days, @NotNull LocalTime startTime, @NotNull LocalTime endTime)`
     — mirror `AvailabilityRequest`'s existing annotation choices for
     `days`/`startTime`/`endTime` exactly, adjusted only for the
     `Integer[]` → `List<Integer>` change mandated in Section 6.
   - `AvailabilityRulesetRequest(@NotBlank @Size(max = 255) String title, @Size(max = 100) String timezone, @NotEmpty @Valid List<AvailabilityWindowRequest> windows)`
     — `timezone` stays optional/unannotated-but-guarded exactly like
     `ScheduleRequest.timezone` today (the entity default is
     `"Australia/Sydney"` via `@Builder.Default`).
   - `AvailabilityRulesetResponse(ScheduleResponse schedule, List<AvailabilityResponse> availabilities)`.

6. **Implement `AvailabilityRulesetService` (interface)** with one
   method: `AvailabilityRulesetResponse createRuleset(AvailabilityRulesetRequest request)`.
   No `getById`/`getAll`/`update`/`delete` — per
   `CODING_CONVENTIONS.md`'s "omit a verb the entity's actual shape
   doesn't support" rule, this is a pure creation orchestrator with no
   entity of its own to read back, update, or delete (the created rows
   are read/updated/deleted through the existing Schedule/Availability
   CRUD endpoints, not through this one).

7. **Implement `AvailabilityRulesetServiceImpl`.**
   - Constructor-inject `ScheduleRepository`, `AvailabilityRepository`,
     `UserRepository`, `CurrentPrincipalProvider`.
   - `@Transactional` on `createRuleset`, on this class directly (this
     bean has no other bean wrapping it, so the self-invocation trap
     does not require a *separate* bean the way `finalizeOnboarding`
     needed one distinct from `DefaultGoogleConnectService` — it only
     requires that the annotation sits on a method invoked from
     *outside* the class, e.g. from the controller, which it is).
   - Resolve `UserEntity user = userRepository.findById(currentPrincipalProvider.requireUser().userId()).orElseThrow(...)`
     once.
   - Build and `save()` the `ScheduleEntity` first (Availability rows
     need its generated id).
   - Map each `AvailabilityWindowRequest` to an `AvailabilityEntity`
     (`.user(user).schedule(schedule).days(window.days().toArray(new Integer[0])).startTime(...).endTime(...)`)
     and persist via `availabilityRepository.saveAll(...)`.
   - Return `new AvailabilityRulesetResponse(ScheduleResponse.from(schedule), availabilities.stream().map(AvailabilityResponse::from).toList())`.

8. **Implement `AvailabilityRulesetController`.** Single `POST`
   endpoint per Section 3 deliverables; `@Valid @RequestBody
   AvailabilityRulesetRequest`; delegate straight to the service.

9. **Write unit tests**, Mockito-style matching
   `DefaultGoogleOnboardingServiceTest`'s existing pattern in this
   codebase:
   - `AvailabilityRulesetServiceImplTest`: mock all four collaborators;
     assert `userRepository.findById` is called with the id from a
     mocked `CurrentPrincipalProvider.requireUser()`, assert
     `scheduleRepository.save` is called before
     `availabilityRepository.saveAll`, assert the response shape.
   - `ScheduleServiceImplTest`/`AvailabilityServiceImplTest`: first
     tests for these classes — cover `create`/`update` deriving the
     user from a mocked `CurrentPrincipalProvider` rather than from a
     request field, and the not-found path when the derived user id
     doesn't resolve (should not realistically happen for an
     authenticated caller, but the code path exists and should be
     covered).

10. **Apply the comment-quality pass.** Re-read every new/modified file
    against `.skills/workflows/quality-code-comments/SKILL.md`. Expected
    comment sites in this task, and only these: (a) the
    `@Transactional` placement on `AvailabilityRulesetServiceImpl`
    explaining the self-invocation precedent it follows, (b) the
    `update`-does-not-reassign-ownership decision from step 3, and (c)
    the `List<Integer>` vs `Integer[]` DTO choice. Everywhere else,
    prefer no comment — the existing codebase style (see
    `ScheduleServiceImpl` today) already has zero comments and reads
    clearly from naming alone.

11. **Write the Obsidian documentation** under
    `obsidian/PleaseBookMe/Services/Availability Ruleset/`, styled after
    `Security/OAuth2/FinalizeOnboarding Explained.md`: what problem it
    solves (reference ISSUE-0003), the endpoint contract, the
    transaction boundary and why it's structured the way it is, and the
    ownership-derivation rule — explicitly state that `userId` is
    intentionally absent from every request DTO in this flow and why.

12. **Run validation** per Section 12, record actual output.

13. **Self-review the diff** against Sections 6 (Constraints), 10
    (Non-Functional Requirements), and the Review Strategy (Section 15)
    before reporting completion.

---

## 15. REVIEWS

### What to Review

- The new `service/availabilityruleset/` stack in full (service,
  impl, DTOs, controller).
- The modified `ScheduleRequest`/`AvailabilityRequest`/
  `ScheduleServiceImpl`/`AvailabilityServiceImpl`.
- The transaction boundary and bean placement in
  `AvailabilityRulesetServiceImpl`.
- Comment density/quality across every touched file.
- The Obsidian documentation for accuracy against the actual
  implementation (not just prose plausibility).

### How to Review

This task requires **Security Review**, since it directly fixes an
authorization/ownership defect and any regression would reintroduce it.
It does not, on its own, require Architecture Review — it extends an
already-established pattern (`service/<area>/` cross-domain
orchestration, `@Transactional`-own-bean) rather than introducing a new
one; escalate to Architecture Review only if implementation surfaces a
genuine deviation from that pattern per Section 13's escalation
triggers.

Security review must specifically verify:

1. No code path in the new or modified files reads a client-supplied
   ownership id (`userId` or equivalent) from any request body,
   header, or query parameter.
2. `currentPrincipalProvider.requireUser()` is called, not
   `currentPrincipalProvider.find()`, so an unauthenticated request
   cannot reach the persistence layer at all rather than silently
   resolving to an empty/default owner.
3. `AvailabilityRulesetServiceImpl.createRuleset` is genuinely
   transactional in practice, not just annotated — confirm the
   `@Transactional` method is invoked from outside the class (from the
   controller) and is not itself called from another `@Transactional`
   or non-transactional method inside the same bean.
4. The `ScheduleRequest`/`AvailabilityRequest` field removal doesn't
   leave a dead/ignored `userId` accepted-but-discarded anywhere (i.e.
   the field is actually gone from the record, not merely unused).

### Core Components to Review

- `AvailabilityRulesetServiceImpl` (new)
- `ScheduleServiceImpl` (modified)
- `AvailabilityServiceImpl` (modified)
- `DefaultCurrentPrincipalProvider.requireUser()` (read-only reference,
  confirm correct usage, no modification expected)

Record the review as `.agents/reviews/TASK-0001-availability-ruleset-orchestration-review.md`
following this repository's existing review-artifact convention
(reviewer role, scope reviewed, findings, severity, evidence,
recommendation) once implementation is complete.
