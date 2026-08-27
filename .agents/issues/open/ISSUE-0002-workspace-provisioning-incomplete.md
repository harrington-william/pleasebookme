# Workspace provisioning — INCOMPLETE, known gap

`UserProvisioningService` is **not** a finished component. It is one slice of a
planned **Workspace Provisioning Service** that has not been built yet. Any
reading of the codebase that suggests "user provisioning is done" is wrong — it
provisions an *identity*, not a *workspace*.

## What it creates today

`UserProvisioningServiceImpl.provisionUser()` writes five rows:

| Row | Table |
|---|---|
| User | `auth.users` |
| USER role assignment | `auth.user_roles` |
| Organization (`"<name>'s Organization"`) | `organization.organizations` |
| Membership (`accepted = true`) | `organization.memberships` |
| Profile | `organization.profiles` |

## What it does NOT create, and must

- **Tenant — the critical one.** No `tenant.tenants` row is ever written, which
  is precisely why every account today has a null tenant. Every new user must
  receive a tenant on the **`FREE`** plan at provisioning time.
- **Schedule + Availability** (`core.schedules`, `core.availabilities`) — a
  booking product with no default working hours cannot compute a single slot, so
  a freshly provisioned workspace is functionally dead until these exist.
- **Notification preferences** (`notification.notification_preferences`).
- Probably also a default `ResourceType` and a starter `Service`, so a new admin
  lands on a usable catalog instead of an empty one. Decide deliberately before
  building — this is a product call, not a schema call.

## Schema/seed blockers that will stop "every user gets a FREE tenant"

Found by inspection. Resolve these **before** writing provisioning code, because
each one is a `NOT NULL` violation waiting to happen at runtime:

1. **The FREE plan cannot populate a tenant's required limits.**
   `tenant.tenants` requires `max_users`, `max_services`, `max_widgets` (all
   `NOT NULL`). But `V101__drop_plans_not_null.sql` made every `max_*` on
   `tenant.plans` nullable, and `V102__seed_plans.sql` seeds FREE with only two
   of the five:
   ```sql
   INSERT INTO tenant.plans(code, name, price, max_services, max_resources)
   VALUES ('FREE', 'Free plan', 0.00, 10, 10);
   ```
   `max_users`, `max_widgets` and `max_api_keys` are therefore **NULL on the
   FREE plan** and cannot be copied onto the tenant row. Fix by re-seeding FREE
   with all five limits (preferred — keeps the plan the single source of truth),
   or by hardcoding fallbacks in provisioning.

2. **`ecosystem_id` is `NOT NULL`, but only one ecosystem is seeded** —
   `BARBERSHOP`. A rental or court business has nothing valid to point at.
   Needs a broader seed, a neutral `GENERAL`/`UNCATEGORIZED` row, or an
   onboarding step that asks the admin what they run.

3. **`region` is `NOT NULL` with no default** (`AU`/`UK`/`US`/`SG`/`VN`). Must
   be derived at signup — from timezone, locale, or an explicit picker. Vietnam
   is the launch market per the business docs, so `VN` is the likely default.

4. **`status` has no obvious value for a self-serve free signup.** The enum
   offers `ACTIVE`/`TRIAL`/`PENDING`/`SUSPENDED`/`ARCHIVED`. Pick one
   deliberately — `ACTIVE` matches "the free plan is a real plan, not a trial".

5. **`tenants.slug` is `UNIQUE`** and needs the same collision fallback the
   organization slug already has (`UserProvisioningServiceImpl` currently falls
   back to the user UID when the org slug collides).