# ISSUE-0003: Schedule + Availability creation has no atomic endpoint and no server-side ownership check

## Priority

**High**

Two independent defects compound here: a correctness/atomicity gap (partial
writes silently produce wrong availability data) and an authorization gap
(any authenticated user can attribute a Schedule/Availability row to another
user's numeric id). The authorization gap alone is enough to rate this High —
it is an unauthenticated-object-reference-style bug (client-supplied
ownership FK, never validated against the caller), not a design preference.

**Unchanged by `UserPrincipal.userId`** (see below): that change makes the
fix for this issue cheaper to implement, it does not fix anything by itself.
`ScheduleServiceImpl`/`AvailabilityServiceImpl` still trust the client-supplied
`userId` field with zero validation against the caller.

## Summary

Creating an "availability ruleset" (one Schedule + N Availability rows) is
implemented entirely client-side as a sequence of independent, non-
transactional HTTP requests: `POST /api/v1/schedules` followed by one
`POST /api/v1/availabilities` per time window. There is no server endpoint
that creates both together, no transaction spanning the sequence, and no
server-side validation that the `userId` in each request body actually
belongs to the authenticated caller.

## Location

**Server (root cause):**
- `server/src/main/java/com/pleasebookme/server/core/schedule/controller/ScheduleController.java` — plain CRUD, `POST /api/v1/schedules`
- `server/src/main/java/com/pleasebookme/server/core/availability/controller/AvailabilityController.java` — plain CRUD, `POST /api/v1/availabilities`
- `server/src/main/java/com/pleasebookme/server/core/schedule/service/impl/ScheduleServiceImpl.java` — `createSchedule` persists `request.userId()` verbatim, no `CurrentPrincipalProvider` reference anywhere in the class
- `server/src/main/java/com/pleasebookme/server/core/availability/service/impl/AvailabilityServiceImpl.java` — same, for `request.userId()`
- `server/src/main/java/com/pleasebookme/server/core/schedule/dto/ScheduleRequest.java` — `userId` is `@NotNull BigInteger`, client-supplied
- `server/src/main/java/com/pleasebookme/server/core/availability/dto/AvailabilityRequest.java` — `userId` is `@NotNull BigInteger`, client-supplied

**Server (now available to the fix):**
- `server/src/main/java/com/pleasebookme/server/security/identity/principal/UserPrincipal.java` — as of this pass, carries `userId: BigInteger` (`auth.users.id`) alongside `subject: UUID` (`auth.users.uid`). See `obsidian/PleaseBookMe/Security/Identity/UserPrincipal.md` → "Why Both `subject` (UUID) and `userId` (BigInteger) Exist".
- `server/src/main/java/com/pleasebookme/server/security/identity/mapper/UserPrincipalMapper.java` — populates it from `aggregation.user().getUserId()`, no extra query (the `UserEntity` is already loaded).
- `server/src/main/java/com/pleasebookme/server/service/auth/controller/AuthController.java` — `GET /api/v1/auth/me` returns `UserPrincipal` verbatim, so `userId` is externally reachable there too, with no DTO change needed.

**Client (symptom / workaround):**
- `client/features/availability/services/availability-gateway.ts` — `createAvailabilityRulesetOnPlatform` performs the 1-schedule-then-N-availabilities sequence with no rollback
- `client/lib/platform-user.ts` — `resolveUserId` exists solely to produce the numeric `userId` these DTOs require, by fetching every user on the platform (`GET /api/v1/users`) and scanning for a UUID match. **Not yet updated** to read `userId` off `GET /api/v1/auth/me` instead — still doing the full-table scan as of this writing.

## Current Behavior

1. **No batch endpoint exists.** `ScheduleController`/`AvailabilityController` only expose the standard five CRUD verbs (`CODING_CONVENTIONS.md`'s standard method set). There is no orchestration endpoint that creates a Schedule together with its Availability rows.
2. **The client fills the gap with sequential, non-transactional calls.** `createAvailabilityRulesetOnPlatform` (`availability-gateway.ts`) does:
   ```
   POST /schedules            -> 201, schedule created
   for each window:
     POST /availabilities     -> may fail on window 2..N
   ```
   If any `POST /availabilities` call fails partway through, the Schedule and any already-created Availability rows are left behind. There is no rollback and no cleanup path — the caller only sees a thrown error and must manually delete the orphaned Schedule (which cascades via `ON DELETE CASCADE`, per `V27__core_availabilities.sql`) and retry.
3. **A partial write is not a visible error state.** A Schedule intended to cover Mon–Fri that only persisted Mon–Wed (because the Thu/Fri `POST` failed) is a valid-looking row that silently computes wrong availability. In a booking product this directly risks accepting or rejecting real bookings against hours the business never actually configured.
4. **`userId` is accepted from the client with no validation against the authenticated principal.** `ScheduleServiceImpl.createSchedule` and `AvailabilityServiceImpl.createAvailability` both resolve the `user` association directly from `request.userId()` via `userRepository.findById(...)`. Neither class references `CurrentPrincipalProvider` (confirmed by search — zero matches in `core/schedule/` and `core/availability/`). Any authenticated bearer token can therefore submit an arbitrary `userId` and have a Schedule/Availability row created and attributed to a different user's numeric id, with no ownership check. **This is still true after the `UserPrincipal.userId` change** — that change only makes the numeric id *available* to server code that asks for it; nothing in `ScheduleServiceImpl`/`AvailabilityServiceImpl` asks for it yet.
5. **~~The client cannot supply `userId` any other way even if it wanted to validate it~~ — partially resolved.** As of this pass, `GET /api/v1/auth/me` returns `UserPrincipal` including `userId`, so the client *can* now obtain its own numeric id without scanning `GET /api/v1/users`. The client has not yet been switched over (`resolveUserId` in `platform-user.ts` is unchanged), and switching it would only remove the client's O(n) list-scan — it does nothing to close the server-side validation gap in point 4, since the server still never checks the submitted `userId` against the caller regardless of how the client obtained it.

## Expected Behavior

1. A single server-side orchestration endpoint creates a Schedule and its Availability rows as one atomic unit: either all rows commit, or none do.
2. The endpoint derives `userId` directly from `CurrentPrincipalProvider.requireUser().userId()` — now a plain field read, no repository lookup required — never from a client-supplied field. `ScheduleRequest.userId`/`AvailabilityRequest.userId` stop being an externally-controllable ownership assertion.
3. The client issues one HTTP call instead of `1 + N`, and `lib/platform-user.ts`'s `resolveUserId` is no longer needed for this flow at all (not even in its cheaper `/me`-based form) — the server no longer needs the client to supply `userId` in the first place.

## Suggested Follow-up

1. Add a new orchestration service under `service/core/` (per `AGENTS.md`'s package-structure convention: cross-domain flows that coordinate several repositories belong in `service/`, not inside one bounded context's own `service/` package) — e.g. `AvailabilityRulesetService` / `DefaultAvailabilityRulesetService`.
2. New DTOs: `AvailabilityRulesetRequest { title, timezone?, List<AvailabilityWindowRequest> windows }` and `AvailabilityWindowRequest { List<Integer> days, LocalTime startTime, LocalTime endTime }` — both `@Valid`-annotated on the nested list (a bare `List<...>` without `@Valid` leaves nested `@NotNull`/`@NotEmpty` inert, same trap already documented for `@RequestBody` in `CODING_CONVENTIONS.md`). Use `List<Integer>` rather than the entity's `Integer[]` for the nested DTO — array-typed record fields get broken `equals`/`hashCode`. Neither DTO carries a `userId` field at all.
3. `create(...)` resolves the caller's `UserEntity` via `userRepository.findById(currentPrincipalProvider.requireUser().userId())` — **now a direct `BigInteger` lookup, no `findByUserUid(UUID)` indirection needed**, since `UserPrincipal.userId()` already holds the numeric PK (see "Location" above). Builds the Schedule, then `saveAll(...)`s the Availability rows in the same `@Transactional` method — on its **own bean**, not a private method (Spring does not proxy self-invocation; this exact pitfall is already documented and fixed once for `finalizeOnboarding` in `SECURITY.md` §14, and the fix generalizes here).
4. New endpoint: `POST /api/v1/availability-rulesets`, response reusing the existing `ScheduleResponse`/`AvailabilityResponse::from` mappers — no new mapping logic.
5. Once the endpoint exists, update `ScheduleRequest`/`AvailabilityRequest` to drop `userId` entirely and derive it server-side (via `CurrentPrincipalProvider.requireUser().userId()`) in the existing single-row CRUD services too, so the ownership gap is closed for direct `POST /schedules`/`POST /availabilities` calls as well, not only the new batch path.
6. Client: replace `createAvailabilityRulesetOnPlatform`'s `1 + N`-call sequence with one call to the new endpoint. `lib/platform-user.ts`'s `resolveUserId` usage in this flow is deleted outright at that point, not migrated to a `/me`-based lookup — once the server derives `userId` itself, the client has no remaining reason to know it for this flow (it may still be needed elsewhere until ISSUE-0004's server-side identity work lands there too).
