# ISSUE-0004: Service + BookingPolicy creation has no atomic endpoint and no server-side ownership check

## Resolution

Resolved by `.agents/tasks/active/TASK-0002-service-booking-policy-atomic-orchestration.md`. `POST /api/v1/services` now supports atomic nested booking-policy creation, `BusinessServiceImpl.createService`/`updateService` derive ownership from the authenticated user/profile instead of request body IDs, and standalone booking-policy duplicate creates return a typed `409`.

## Priority

**High**

Same shape as ISSUE-0003 (client-supplied ownership FKs, no transactional
batch endpoint). Data-level protection for the pair's 1:1 relationship has
since landed — `V126__add_booking_policy_service_unique.sql` adds
`UNIQUE (service_id)` to `core.booking_policies`, so a retried create can no
longer silently produce two policies for one service (see below). What
remains open, and still justifies High on its own, is unchanged: no atomic
batch endpoint, and no server-side validation that the ownership ids in the
request bodies belong to the authenticated caller.

**Unchanged by `UserPrincipal.userId`** (see below): that change makes the
`userId` half of the fix cheaper to implement. It does nothing for
`profileId`/`organizationId` — `UserPrincipal` deliberately does not carry
either (see `obsidian/PleaseBookMe/Security/Identity/UserPrincipal.md` →
"What's Deliberately Absent") — and it does nothing at all for the lack of
ownership validation in `BusinessServiceImpl`, which still trusts every
client-supplied id verbatim.

## Summary

Creating a Service together with its BookingPolicy is implemented entirely
client-side as two independent, non-transactional HTTP requests:
`POST /api/v1/services` followed by `POST /api/v1/booking-policies`. There is
no server endpoint that creates both together, no transaction spanning the
pair, and no server-side validation that `userId`/`profileId`/`organizationId`
in the request bodies belong to the authenticated caller. A database-level
guard against a service ending up with more than one policy now exists
(`V126`); an application-level guard producing a clean `409` instead of an
uncaught `500` on collision does not yet.

## Location

**Server (root cause):**
- `server/src/main/java/com/pleasebookme/server/core/service/controller/ServiceController.java` — plain CRUD, `POST /api/v1/services`
- `server/src/main/java/com/pleasebookme/server/core/bookingpolicy/controller/BookingPolicyController.java` — plain CRUD, `POST /api/v1/booking-policies`
- `server/src/main/java/com/pleasebookme/server/core/service/service/impl/BusinessServiceImpl.java` — `createService` persists `request.userId()`/`request.profileId()`/`request.organizationId()` verbatim, no `CurrentPrincipalProvider` reference anywhere in the class
- `server/src/main/java/com/pleasebookme/server/core/bookingpolicy/service/impl/BookingPolicyServiceImpl.java` — `createBookingPolicy` still has no `existsBy*` duplicate-check before `save()`, and `BookingPolicyRepository` still exposes no `existsByServiceId` method to add one. The DB constraint (below) now rejects a second row, but this class doesn't check first — a collision surfaces as an uncaught `DataIntegrityViolationException`, and `GlobalExceptionHandler` has no handler for it (confirmed by search), so it falls through to a generic `500`.
- `server/src/main/java/com/pleasebookme/server/core/service/dto/ServiceRequest.java` — `userId`, `profileId`, `organizationId` all `@NotNull BigInteger`, client-supplied

**Server (already fixed):**
- `server/src/main/resources/db/migration/core/V126__add_booking_policy_service_unique.sql` — adds `CONSTRAINT uq_booking_policies_service UNIQUE (service_id)` to `core.booking_policies`. Verified: numbered above the documented v125 high-water mark, naming matches the codebase's established `uq_<table>_<column-without-_id-suffix>` pattern (c.f. `uq_widgets_public_key`, `uq_tenants_slug`), zero comments per the Flyway-copy convention. This closes the original `V29__core_booking_policies.sql` gap (that migration's plain FK `fk_booking_policy_service` was never a `UNIQUE`) — a second `BookingPolicy` row for the same `service_id` is no longer possible at the data layer.

**Server (now available to the fix, `userId` only):**
- `server/src/main/java/com/pleasebookme/server/security/identity/principal/UserPrincipal.java` — as of this pass, carries `userId: BigInteger` (`auth.users.id`) alongside `subject: UUID`. Does **not** carry `profileId`/`organizationId` — that remains a deliberate absence (identity vs. request-scoped organization context; see the same doc page's "What's Deliberately Absent"). Resolving `profileId`/`organizationId` server-side still requires `ProfileRepository.findByUserUserId(userId)`, same as before this change.
- `server/src/main/java/com/pleasebookme/server/organization/profile/repository/ProfileRepository.java` — `findByUserUserId(BigInteger)` already exists, unaffected by the `UserPrincipal` change.
- `server/src/main/java/com/pleasebookme/server/service/auth/controller/AuthController.java` — `GET /api/v1/auth/me` now returns `userId` (via `UserPrincipal`), but still nothing about `profileId`/`organizationId`.

**Client (symptom / workaround):**
- `client/features/services/services/service-gateway.ts` — `createServiceWithPolicyOnPlatform` performs the Service-then-BookingPolicy sequence with no rollback
- `client/lib/platform-user.ts` — `resolveCurrentPlatformUser` fetches `GET /api/v1/users` and `GET /api/v1/profiles` in full and scans both to derive `userId`/`profileId`/`organizationId`. **Not yet updated** for either half: still scanning `GET /api/v1/users` for `userId` (now redundant with `/me`) and still needs the `GET /api/v1/profiles` scan for `profileId`/`organizationId` regardless (no server-side change removes that need).

## Current Behavior

1. **No batch endpoint exists.** `ServiceController`/`BookingPolicyController` only expose the standard CRUD verbs. There is no orchestration endpoint that creates a Service together with its BookingPolicy.
2. **The client fills the gap with two sequential, non-transactional calls** (`createServiceWithPolicyOnPlatform` in `service-gateway.ts`):
   ```
   POST /services         -> 201, service created
   POST /booking-policies -> may fail
   ```
   If the second call fails, the Service is left behind with no policy. There is no rollback.
3. **~~`core.booking_policies` has no unique constraint on `service_id`~~ — resolved by `V126`.** A second `BookingPolicy` row for the same `service_id` is now rejected at the database. What remains: `BookingPolicyServiceImpl.createBookingPolicy` still has no `existsBy*` duplicate-check before `save()` (there is still no `existsByServiceId` on `BookingPolicyRepository`), so a collision doesn't produce the codebase's standard clean `409 Duplicate*Exception` response — it produces an uncaught `DataIntegrityViolationException`, unhandled by `GlobalExceptionHandler`, surfacing as a generic `500`. The client's catalog-assembly code (`listMyServiceCatalogOnPlatform`, via a `Map<serviceId, BookingPolicy>` keyed by `serviceId`) is no longer at risk of silently picking one of two duplicate rows, since a second row can no longer be written at all — but a retry attempt now fails loudly and ungracefully instead.
4. **A Service created with no policy renders degraded, not broken, which currently hides the defect.** `ServiceCatalogEntry.bookingPolicy` is nullable by design (`service-gateway.ts`'s own doc comment), and the catalog card shows "Not configured" for duration/price/policy. This is a deliberate, honest fallback for the *current* non-atomic design — but it also means a partial-write failure produces a plausible-looking, silently-incomplete Service rather than a visible error.
5. **`userId`, `profileId`, `organizationId` are accepted from the client with no validation against the authenticated principal.** `BusinessServiceImpl` resolves `user`/`profile`/`organization` associations directly from the three request fields via their respective repositories' `findById(...)`. No reference to `CurrentPrincipalProvider` exists anywhere in `core/service/` (confirmed by search). Any authenticated bearer token can submit an arbitrary `organizationId` and have a Service created inside an organization the caller does not belong to. **This is still true after the `UserPrincipal.userId` change** — the field being available doesn't mean anything in `BusinessServiceImpl` reads it yet, and `profileId`/`organizationId` were never touched by that change at all.
6. **~~The client cannot supply these ids any other way even if it wanted to validate them~~ — partially resolved, `userId` only.** As of this pass, `GET /api/v1/auth/me` returns `userId`, so the client no longer strictly needs the `GET /api/v1/users` scan to learn its own numeric id. `profileId`/`organizationId` are unaffected — no endpoint returns them for the signed-in caller, so `resolveCurrentPlatformUser`'s `GET /api/v1/profiles` scan is still the only way the client can obtain them today. Either way, none of this touches the actual defect in point 5: the server still doesn't validate whatever the client sends.

## Expected Behavior

1. A single server-side orchestration endpoint creates a Service and its BookingPolicy as one atomic unit: either both commit, or neither does.
2. ~~`core.booking_policies.service_id` carries a `UNIQUE` constraint~~ — **done, `V126`.** Matches the entity's actual intended 1:1 shape (no effective-dating columns exist on `BookingPolicyEntity`, unlike `ResourcePricingEntity`'s `effective_from`/`effective_until`, confirming this is meant to be a single active row per service, not a history). Remaining, smaller gap: a collision should produce a clean `409` via the standard `existsBy*` + `Duplicate*Exception` pattern, not an uncaught `500`.
3. The endpoint derives `userId` directly from `CurrentPrincipalProvider.requireUser().userId()` (a field read, no lookup), and `profileId`/`organizationId` from `ProfileRepository.findByUserUserId(userId)` — never from client-supplied fields.
4. The client issues one HTTP call instead of two, and `resolveCurrentPlatformUser` is no longer needed for this flow at all — not its `userId` half (already partially mitigated by `/me`) and not its `profileId`/`organizationId` half either, since the server stops asking the client to supply any of the three.

## Suggested Follow-up

1. ~~Add the missing `UNIQUE (service_id)` constraint~~ — **done, `V126__add_booking_policy_service_unique.sql`.** Remaining, optional, and much smaller than it originally looked: add `existsByServiceServiceId` to `BookingPolicyRepository` and a `DuplicateBookingPolicyException` guard in `BookingPolicyServiceImpl.createBookingPolicy`, per `CODING_CONVENTIONS.md`'s standard duplicate-check-before-create rule, so a collision returns the codebase's usual clean `409` instead of an uncaught `500`. Not blocking for the rest of this issue — the data can no longer actually be corrupted either way.
2. Add a new orchestration service under `service/core/` (cross-domain: Service touches `core`, `organization`, `integration`; per `AGENTS.md` this belongs in `service/`, not inside `core/service/`'s own package) — e.g. `ServiceCatalogService` / `DefaultServiceCatalogService`.
3. New DTO: `ServiceWithPolicyRequest { ServiceRequest service, BookingPolicyRequest policy }`, or a flattened equivalent — both nested objects `@Valid`-annotated. Neither carries `userId`/`profileId`/`organizationId` fields at all.
4. `create(...)` resolves the caller's `UserEntity` via `userRepository.findById(currentPrincipalProvider.requireUser().userId())` — **now a direct `BigInteger` lookup, no `findByUserUid(UUID)` indirection needed** (see "Location" above) — then the caller's `ProfileEntity` via `profileRepository.findByUserUserId(user.getUserId())` (already exists, unaffected by the `UserPrincipal` change) to get `profileId`/`organizationId`, builds the Service, then the BookingPolicy referencing `service.getServiceId()` — all inside one `@Transactional` method on its **own bean** (same self-invocation-proxy pitfall as ISSUE-0003 and the existing `finalizeOnboarding` precedent in `SECURITY.md` §14). This method now benefits from `V126` for free: even if the transaction somehow raced with a concurrent request past the application layer, the database itself is the backstop.
5. New endpoint: `POST /api/v1/services` extended to accept an optional nested `bookingPolicy` field (preferred over a parallel endpoint — a Service with no policy is a broken Service, and there is no natural second name for the pair), or a dedicated `POST /api/v1/services/with-policy` if keeping the plain endpoint's request shape untouched is preferred. Decide deliberately — this changes the existing endpoint's contract either way, which is worth a conscious choice rather than an implicit one.
6. Once the endpoint exists, update `ServiceRequest` to drop `userId`/`profileId`/`organizationId` and derive them server-side (same resolution as step 4) in the existing single-row CRUD path too, closing the ownership gap for direct `POST /services` calls as well.
7. Client: replace `createServiceWithPolicyOnPlatform`'s two-call sequence with one call to the new endpoint. Delete `resolveCurrentPlatformUser`'s usage from this flow outright — do not migrate its `userId` half to a `/me`-based lookup first as an intermediate step; once the server derives all three ids itself, the client has no remaining reason to resolve any of them for this flow.
