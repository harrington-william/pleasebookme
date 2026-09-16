# Workspace provisioning — one gap remaining

`WorkspaceProvisioningService` (`service/workspace/`, TASK-0010) replaced the
former `service/auth/UserProvisioningService`. It now provisions a *workspace*,
not just an identity. One item on the original list is still open.

## What it creates today

`WorkspaceProvisioningServiceImpl.provision()` writes eleven rows in one
`@Transactional` unit:

| Row | Table |
|---|---|
| User | `auth.users` |
| USER role assignment | `auth.user_roles` |
| Organization (`"<name>'s Organization"`) | `organization.organizations` |
| Membership (`accepted = true`) | `organization.memberships` |
| ORGANIZATION_OWNER role on that membership | `organization.membership_roles` |
| Profile | `organization.profiles` |
| FREE-plan tenant | `tenant.tenants` |
| Schedule (`"Working Hours"`, user timezone) | `core.schedules` |
| Availability (Mon–Fri ISO `{1..5}`, 09:00–17:00) | `core.availabilities` |
| Service (`"Consultant Meeting"` / `consultant-meeting`) | `core.services` |
| Booking policy (30 min, 120 min notice, 30-day rolling window, capacity 1) | `core.booking_policies` |

## Remaining gaps

- **Notification preferences** (`notification.notification_preferences`) —
  deliberately deferred; the notification feature is not built yet.
- **Default `ResourceType`** — undecided product call, not scheduled.
- **Google-signup timezone.** `DefaultGoogleAccountResolver` passes
  `timezone = null`, which falls back to `Australia/Sydney`. That was cosmetic
  when only the user row was written; it now sets the 9–5 schedule too. The
  client should send the browser timezone on `/auth/google` and
  `/auth/google/authorize` so the starter schedule lands in the right zone.

## Resolved

- **Tenant — TASK-0007.** V139 completes the FREE quotas, V140 seeds GENERAL,
  V141 backfills existing organizations.
- **Schedule + Availability + starter Service + Booking policy — TASK-0010.**
- **ORGANIZATION_OWNER membership role — TASK-0010.** Previously a fresh
  membership had no role at all, so `DefaultMembershipResolver` produced an
  empty org-role set for every new principal.

## Not backfilled

Users provisioned before TASK-0010 have no `membership_roles` row, no schedule,
and no starter service. No migration backfills these — they are dev-only
accounts at this stage. If that changes, a one-off V-migration or admin script
is needed, keyed on memberships with zero `membership_roles` rows.
