# TASK-0007 Widget Page Backend Review

## Reviewer role

Architecture, security, database, performance, and testing self-review following the repository code-review workflow.

## Scope reviewed

- V139–V141 and their live Flyway application.
- Registration-time tenant provisioning and current-tenant resolution.
- Widget credentials, DTO/controller contract, origin normalization/upsert, specifications, list/stats, and soft deletion.
- New unit tests, live API/database behavior, documentation, and static rule checks.

## Findings

### Blockers

None.

### Important findings resolved during implementation

1. **Hibernate treated `WidgetEntity.issuedAt` as immutable while annotated with `@CreationTimestamp`.** Live rotation logged HHH000502 and omitted `issued_at` from the SQL update. The documented TASK-0007 fallback was applied: issuance time is now set explicitly on create and rotation. A second live run showed `issued_at` in the update and the stored value advanced.
2. **The task's bootstrap curl example conflicts with local CORS configuration.** An arbitrary `Origin: https://barbershop.com` header is rejected at the CORS filter before authentication. The real bootstrap proof was run without the browser CORS header while sending the same normalized origin in the endpoint's request body, which is the value `AuthController` passes to `WidgetIdentityLoader`; matching origin returned 200 and a mismatched origin returned 401. No out-of-scope security configuration was changed.

### Accepted known risks

- Direct `GET`, `PUT`, and `DELETE /api/v1/widgets/{id}` remain unscoped across tenants, explicitly deferred by the task contract.
- Widget quota enforcement, origin verification, refresh-token cleanup on revoke, and widget authorities remain deferred.
- The live validation created one test user/tenant and retained revoked widget/origin rows in the dev database, matching the registration and soft-delete acceptance scenarios.

## Evidence

- Baseline and final `./gradlew compileJava` succeeded.
- Baseline and final `./gradlew test` succeeded; final result: 193 tests, 0 failures, 0 errors, 0 skipped.
- Flyway advanced from 138 to 141 and V141 re-execution returned `INSERT 0 0`.
- Backfill query showed a tenant for every pre-existing organization.
- Live API checks covered registration, credential formats, BCrypt-at-rest, duplicate and Bean Validation failures, bootstrap success/mismatch, filtered/capped list, stats, no-rotation and rotation updates, soft-delete idempotency, revoked read/update behavior, and foreign-organization empty responses.
- Hibernate SQL showed one paginated widget query followed by one batched origin `IN` query; the service test also verifies exactly one origin repository call.
- Static scans found no Redis/authorization imports, `@PreAuthorize`, secret logging, forbidden request fields, migration comments, or remaining `WidgetRequest` references.

## Recommendation

PASS. The implementation satisfies TASK-0007 and is ready for TASK-0008 to consume the documented wire contract.
