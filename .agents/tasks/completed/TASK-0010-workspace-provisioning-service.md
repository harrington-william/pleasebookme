# Task Contract

## 1. IDENTITY

Title: Workspace Provisioning Service (schedule, availability, starter
       service + booking policy, organization-owner role)
Domain: Server / `service/workspace` (new orchestration package), superseding
        `service/auth/UserProvisioningService` — writes across `auth`,
        `organization`, `tenant`, `core` (Spring Boot)
Priority: High — registration was gated on this; a workspace with no
          schedule, availability, or service cannot compute a single
          bookable slot (`TASK-0009`'s engine has nothing to read).
Risk: Medium — no migration, no entity/schema change, but the change sits on
      the registration transaction for both signup paths (password and
      Google) and alters a security-relevant default: every new membership
      now receives `ORGANIZATION_OWNER`, where previously no membership had
      any role at all, so `DefaultMembershipResolver` now puts owner
      permissions on every fresh principal.

Resolves: `.agents/issues/open/ISSUE-0002-workspace-provisioning-incomplete.md`

Status: COMPLETED

---

## Objective

Replace `service/auth/UserProvisioningService` with
`service/workspace/WorkspaceProvisioningService`, extending registration-time
provisioning from an identity (6 rows) to a usable workspace (11 rows).

## Scope

- New `WorkspaceProvisioningServiceImpl.provision(...)` (initially the same six
  positional params as the old `provisionUser`, later consolidated into a
  `WorkspaceProvisionRequest` record — see Follow-ups), `@Transactional`.
- Delete `UserProvisioningService` / `UserProvisioningServiceImpl` / its test.
- Rewire `AuthServiceImpl.register` and `DefaultGoogleAccountResolver` step 3.
- New rows: `organization.membership_roles` (`ORGANIZATION_OWNER`),
  `core.schedules`, `core.availabilities`, `core.services`,
  `core.booking_policies`.
- Docs: `SERVER_AGENTS.md`, `SECURITY.md`, obsidian table docs for every
  auto-created table, `ISSUE-0002`.

## Constraints / decisions

- Google account *resolution* (existing link → email match → provision) stays
  in `DefaultGoogleAccountResolver`; the workspace service only creates.
- Repositories are written directly. `BusinessServiceImpl.createService` reads
  `CurrentPrincipalProvider`, and no principal exists during registration.
- `save()` return values are not used; built instances are passed along. The
  Mockito test therefore stubs no `save`.
- Booking policy: `defaultDuration = 30` (entity default is **1 minute**),
  `slotInterval = 30`, `minimumNotice = 120`, `maximumAdvanceBooking = 43200`,
  `bookingWindowType = "ROLLING"` (only value in use; `FIXED` unimplemented),
  `capacity = 1`, entity defaults otherwise.
- Availability days use ISO `{1,2,3,4,5}` — see the TASK-0009 finding; not the
  dashboard's Sunday = 0 encoding.
- `@Transactional` added on `provision` despite TASK-0007's caveat: both callers
  already run inside a transaction, so `REQUIRED` joins it and rollback
  semantics are unchanged; the annotation only adds protection for a future
  caller that forgets.
- Notification preferences deliberately excluded (feature not built).

## Acceptance criteria

- `./gradlew test` green, including `ServerApplicationTests` (bean wiring).
- `WorkspaceProvisioningServiceImplTest` covers every new row plus the ported
  tenant/ecosystem/plan cases and a missing-`ORGANIZATION_OWNER` case.
- No remaining reference to `UserProvisioningService` in `src/`, `SECURITY.md`,
  `SERVER_AGENTS.md`, or obsidian (completed task artifacts excepted).

## Validation

- `./gradlew test`: 240 tests, 0 failures, 0 errors (2026-09-16).
- `WorkspaceProvisioningServiceImplTest`: 10/10.
  `DefaultGoogleAccountResolverTest`: 7/7.

## Follow-ups

- Client: send browser timezone on Google sign-in / one-shot authorize so the
  starter schedule is not Sydney-by-default (`ISSUE-0002`).
- No backfill for pre-existing users (`ISSUE-0002`).

### Addendum — `provision(...)` signature consolidated (2026-09-16)

`provision(String, String, String, String, Locale, String)` was replaced with
`provision(WorkspaceProvisionRequest)`. New record:
`service/workspace/dto/WorkspaceProvisionRequest(username, name, email, phone,
locale, timezone)` — deliberately its own type, not a reuse of
`service/auth/dto/RegisterRequest`, because `RegisterRequest` carries a
`password` field `WorkspaceProvisioningService` never needs, and
`DefaultGoogleAccountResolver` has no `RegisterRequest` to pass (it only has a
`GoogleIdentity`) — reusing it would force that caller to fabricate one with a
meaningless `password = null`. `AuthServiceImpl.register` maps
`RegisterRequest` → `WorkspaceProvisionRequest`; `DefaultGoogleAccountResolver`
maps `GoogleIdentity` → `WorkspaceProvisionRequest` directly, unchanged
otherwise. Both test files updated: exact-value stubs/verifies now pass a
constructed `WorkspaceProvisionRequest` (records get `equals()` for free), and
the one partial-match case (`derivedUsernameAlreadyTaken_...`, which only
knows a username *prefix*) uses `argThat` over the whole record instead of
per-argument Mockito matchers, since Mockito matchers no longer align to
individual fields once they're packed into one object. 240/240 tests still
green.
