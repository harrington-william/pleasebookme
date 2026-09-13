# TASK-0006 Dashboard Page Review

## Reviewer role

Independent architecture, implementation, performance, frontend, and security reviewer.

## Scope reviewed

- Task contract: `.agents/tasks/active/TASK-0006-dashboard-page.md`.
- Server orchestration package: `server/src/main/java/com/pleasebookme/server/service/dashboard/**`.
- Additive specification and repository changes in `BookingSpecifications`, `AttendeeRepository`, and `BookingResourceRepository`.
- Client page and feature implementation in `client/app/dashboard/page.tsx` and `client/features/dashboard/**`.
- Required precedents: availability-ruleset orchestration, booking tab/specification behavior, resource stats service/DTO/tile, resources Server Component, bookings table/status badge, and relevant entity mappings.
- Scope/prohibition scans for migrations, entities, `BookingRepository`, existing booking services/controllers, security wiring, exception handlers, BFF routes, duplicate wire fields/types, clock calls, and per-row repository calls.
- The unrelated dirty worktree files and the task contract's documented one-line baseline test repair were excluded from product-code findings.

## Summary

The implementation follows the requested cross-domain orchestration shape and preserves the existing bounded-context boundaries. The endpoint returns response DTOs directly through a thin controller; the client consumes the summary in one server-side platform call. Metric predicates, timezone handling, deterministic attendee selection, nullable enrichment, reserved revenue presentation, status-badge reuse, and the fixed ten-row descending table agree with the contract.

## Findings

### 🟡 Required browser console/hydration verification is incomplete

**Severity:** Suggestion / validation gap

**Evidence:** The implementation report evidence supplied to the reviewer shows that live Next HTTP requests rendered both populated and empty dashboard markup successfully. However, the in-app browser held a stale refresh cookie and rendered the intentional load-error state. Consequently, a populated `/dashboard` render and its console were not observed in the browser, so product hydration behavior for the populated state remains unverified. When the Next dev server stopped, it did report a hydration mismatch whose sole differing attribute was `data-darkreader-proxy-injected="true"` on `<html>`. That attribute is injected by the browser's Dark Reader extension before React hydrates and is not emitted by this application; it is external test-environment noise, not evidence of a TASK-0006 code defect. This leaves Validation #15 and the populated-state portion of the frontend acceptance criteria incomplete (`TASK-0006` §12 #15–#17 and §11 Frontend).

**Recommendation:** Renew or replace the signed-in in-app browser session, disable Dark Reader for the verification origin, load the populated dashboard, and confirm that the four tiles and booking table render without product-originated console or hydration errors before moving the task to completed. Do not add a source workaround or hydration suppression for the extension-injected attribute.

No blocker findings were identified in the product code.

## Review evidence

### Architecture and scope

- `DashboardController` is under `service/dashboard`, accepts only the required `organizationId`, and delegates directly to `DashboardService`.
- `DashboardServiceImpl` returns `DashboardSummaryResponse` and maps all lazy associations inside `@Transactional(readOnly = true)`.
- `BookingSpecifications` contains exactly the two requested additive factories; `BookingRepository` is unchanged.
- Each enrichment repository gained one derived `...In(...)` finder with no custom query.
- No task-scoped migration, entity, exception, handler, authorization policy/provider wiring, existing booking API reshaping, or `client/app/api/dashboard` route exists.
- Server/client wire fields and nullability match; metrics contain only `todaysBookings`, `pending`, and `cancellations`.

### Correctness

- `todaysBookings` applies the half-open local-day window and the same live status set used by the existing UPCOMING semantics.
- `pending` intentionally has no date window and uses `PENDING` plus `AWAITING_HOST`.
- `cancellations` uses the local start-day window and `CANCELLED` plus `REJECTED`.
- The organization timezone is resolved before a single `LocalDate.now(zone)` call; malformed stored zones fall back to UTC.
- Recent bookings are capped at ten, sorted by `startTime` descending, and have no status filter.
- Attendee enrichment deterministically chooses the lowest `attendeeId`; missing attendee and primary-resource values remain null through the wire and render as `—`.
- Runtime evidence supplied to the reviewer covered: missing parameter 400; expected seeded counts `2/2/2`; half-open boundary exclusion; timezone date shift; invalid-timezone UTC fallback; empty and unknown organizations; descending rows; lowest-attendee selection; nullable enrichment; and hostile parameter immunity.

### Performance and query shape

- No repository call occurs inside a row loop or mapping lambda.
- The empty recent-bookings guard precedes both batched enrichment queries.
- The implementation does not use the unbounded organization-wide attendee finder.
- Supplied SQL evidence recorded nine domain queries for the populated request: one organization lookup, three metric counts, one page query, two batched enrichment queries, one distinct service lazy load, and one distinct resource lazy load. No query was issued once per booking id. The two bounded association loads are explicitly accepted by the task contract.

### Frontend contract

- The debug-session card and page-level sign-out control are removed.
- The page uses `withAccessToken(..., { allowSessionWrite: false })`, redirects `SessionExpiredError` to `SESSION_EXPIRED_REDIRECT`, and preserves the session on other load failures.
- The page contains only the dashboard heading/date, four-tile row, and recent-bookings table on successful load.
- Revenue uses the same dashed, disabled, translucent reserved treatment as resource utilization, with no revenue wire field.
- No utilization/new-customer tile, trend, delta, controls, filters, pagination, or speculative BFF route was introduced.
- `BookingStatus` and `BookingStatusBadge` are imported from the bookings feature; there is no duplicate status-label map.
- Start times are formatted using the payload timezone and explicitly label that zone; the table owns horizontal overflow and has a real empty state.

### Security decision

- The explicit deferral was followed: no `@PreAuthorize`, principal provider, organization provider, or policy wiring was added.
- The forgeable `organizationId` is known and accepted by the task contract and is not raised as a finding.

### Validation executed or inspected

- Independent reviewer run: `./gradlew compileJava` — `BUILD SUCCESSFUL in 5s`.
- Independent reviewer run: `./gradlew test --tests '*ServerApplicationTests*'` — `BUILD SUCCESSFUL in 8s`.
- Independent reviewer run: `./gradlew test` — `BUILD SUCCESSFUL in 8s`.
- Independent reviewer run: `npx tsc --noEmit && npx eslint . && npm run build` — exit 0; Next compiled and generated all routes. ESLint reported one pre-existing warning in `features/services/components/service-card.tsx` for an unused `Sparkles` import, outside TASK-0006's diff.
- Implementation evidence inspected: endpoint scenarios, seeded metric checks, timezone/boundary/null cases, parameter immunity, SQL query count, populated and empty live Next HTTP rendering.
- Browser-environment note: the observed `data-darkreader-proxy-injected="true"` hydration difference was caused by Dark Reader modifying `<html>` and requires no product-code change.
- Not completed: populated in-app browser console/hydration verification with a valid session and without extension DOM injection, as described in the finding above.

## Recommendation

**APPROVED WITH CHANGES**

No product-code changes are required by this review. Complete the named browser verification and include its real result, or explicitly preserve the residual gap, in the closing report before merge/completion.
