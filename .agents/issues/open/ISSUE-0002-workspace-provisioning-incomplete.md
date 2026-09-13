# Workspace provisioning — INCOMPLETE, known gap

`UserProvisioningService` is **not** a finished component. It is one slice of a
planned **Workspace Provisioning Service** that has not been built yet. Any
reading of the codebase that suggests "user provisioning is done" is wrong — it
provisions an *identity*, not a *workspace*.

## What it creates today

`UserProvisioningServiceImpl.provisionUser()` writes six rows:

| Row | Table |
|---|---|
| User | `auth.users` |
| USER role assignment | `auth.user_roles` |
| Organization (`"<name>'s Organization"`) | `organization.organizations` |
| Membership (`accepted = true`) | `organization.memberships` |
| Profile | `organization.profiles` |
| FREE-plan tenant | `tenant.tenants` |

## Provisioning status and remaining gaps

- **Tenant — resolved by TASK-0007.** V139 completes the FREE quotas, V140 seeds
  GENERAL, V141 backfills existing organizations, and provisioning now writes an
  active FREE/GENERAL tenant in the registration transaction.
- **Schedule + Availability** (`core.schedules`, `core.availabilities`) — a
  booking product with no default working hours cannot compute a single slot, so
  a freshly provisioned workspace is functionally dead until these exist.
- **Notification preferences** (`notification.notification_preferences`).
- Probably also a default `ResourceType` and a starter `Service`, so a new admin
  lands on a usable catalog instead of an empty one. Decide deliberately before
  building — this is a product call, not a schema call.

## Resolved tenant blockers

TASK-0007 resolved all five blockers without closing the remaining workspace
provisioning gaps:

1. **Resolved in V139:** the FREE plan now supplies `max_users = 1`,
   `max_widgets = 3`, and `max_api_keys = 1` alongside its existing limits.

2. **Resolved in V140:** `GENERAL` is the neutral active ecosystem for new
   self-serve tenants.

3. **Resolved in provisioning:** region derives from supported timezone groups,
   with `VN` as the launch-market fallback.

4. **Resolved in provisioning:** self-serve FREE tenants start `ACTIVE`.

5. **Resolved in provisioning:** a tenant slug collision falls back to the
   provisioned user's UID. V138 also enforces one tenant per organization.

## Historical evidence

Before V139–V141, the FREE plan left required tenant quotas null, `BARBERSHOP`
was the only ecosystem, self-serve region/status were undecided, tenant slugs
had no collision policy, and existing organizations had no tenant. The resolved
items above preserve that context without presenting it as current behavior.
