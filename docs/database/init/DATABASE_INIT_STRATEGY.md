# Phases

```
Phase 1
Foundation
↓
Phase 2
Business Domains
↓
Phase 3
Infrastructure Domains
↓
Phase 4
Indexes & Seed Data
```

Notice

I don't organize by schema.

I organize by **dependency graph**.

That dramatically reduces FK problems.

---

# Why?

Suppose

```
core.services

↓

organization.organizations

↓

tenant.tenants

↓

resource.resources
```

If you simply split by schema

```
auth.sql

core.sql

resource.sql
```

immediately hit

```
FK does not exist
```

Instead

I organize

by dependency.

---

# Phase 1

## Foundation

Nothing references these.

```
V1__create_extensions.sql

V2__create_schemas.sql

V3__create_enum_types.sql

V4__create_organization_schema.sql

V5__drop_core_weekstart_type.sql

V6__create_public_weekstart_type.sql

V7__create_public_currency_type.sql

V8__create_public_theme_type.sql

V9__create_public_locale_type.sql
```

`V4` was added after the fact, when `auth` was split into system identity
(`auth`) and business identity (`organization`). It could have lived inside
`V2`, but additive foundation migrations are cheap, so it's its own file
instead of editing an already-applied one.

`V5`-`V9` moved a set of enums that are shared across multiple domains
(week start, currency, theme, locale) out of domain-owned schemas and into
`public`, since none of them belong to a single domain. `core.week_start`
specifically is dropped and recreated as `public.week_start` — it was
originally created under `core` in `V3`, but is used by `auth.users` and
`organization.organizations`, neither of which is Core's concern.

---

## V1

```
uuid extension

citext

pgcrypto

...
```

---

## V2

Create schemas

```
auth

core

tenant

resource

customer

widget

notification

integration

audit
```

---

## V3

Every enum.

Examples

```
booking_status

booking_mode

tenant_status

resource_status

notification_status

audit_action

...
```

One file.

Done forever.

---

## V4

Create the `organization` schema, split out from `auth`.

`auth` keeps system identity: credentials, sessions, RBAC, API keys.

`organization` owns business identity: organizations, staff profiles,
memberships, membership roles.

---

# Phase 2

## Identity (Auth)

Everything starts here.

```
V10__auth_users.sql

V11__auth_user_passwords.sql

V12__auth_roles.sql

V13__auth_permissions.sql

V14__auth_user_roles.sql

V15__auth_role_permissions.sql

V16__auth_sessions.sql

V17__auth_accounts.sql

V18__auth_refresh_tokens.sql
```

Identity first.

Everything else depends on it.

`auth.api_keys` is **not** in this phase — it references `tenant.tenants`,
which doesn't exist yet. It moves to Phase 4, right after Tenant. See that
phase for why.

---

# Phase 3

## Organization

Organization depends on

```
Auth (users, roles)
```

which already exists.

```
V20__organization_organizations.sql

V21__organization_profiles.sql

V22__organization_memberships.sql

V23__organization_membership_roles.sql
```

Notice

Organizations

↓

Profiles

↓

Memberships

↓

Membership Roles

Dependency order. Everything downstream that used to reference
`auth.organizations` / `auth.memberships` now references
`organization.organizations` / `organization.memberships` instead.

---

# Phase 4

## Tenant

```
V30__tenant_plans.sql

V31__tenant_ecosystems.sql

V32__tenant_tenants.sql

V33__tenant_domains.sql

V34__auth_api_keys.sql
```

Notice

Plans

↓

Ecosystems

↓

Tenants

↓

Domains

Dependency order.

`V34__auth_api_keys.sql` lives here, not in Phase 2, because
`api_keys.tenant_id` references `tenant.tenants`. It's still owned by the
`auth` schema — only its migration's *position* moved, to keep every phase
self-contained (introduce only objects whose dependencies already exist).

---

# Phase 5

## Core

```
V40__core_schedules.sql

V41__core_availabilities.sql

V42__core_services.sql

V43__core_booking_policies.sql

V44__core_bookings.sql

V45__core_attendees.sql

V46__core_selected_slots.sql

V47__core_out_of_office.sql
```

`core.services` and `core.bookings` are created here **without**
`destination_calendar_id`. That column references
`integration.destination_calendars`, which doesn't exist until Phase 9 —
and `integration.destination_calendars` references `core.services` right
back, so this is a genuine circular dependency, not just an ordering
oversight. See the patch migration after Phase 9.

---

# Phase 6

## Resource

Because

Resources depend on

```
Organization

Membership

Service
```

which already exist.

```
V50__resource_types.sql

V51__resources.sql

V52__resource_pricing.sql

V53__resource_assignments.sql

V54__resource_calendars.sql

V55__resource_maintenance.sql

V56__resource_attributes.sql

V57__resource_overrides.sql
```

---

# Phase 7

## Customer

Customer depends on Tenant.

```
V60__customers.sql

V61__customer_notes.sql

V62__customer_activities.sql

V63__customer_tags.sql

V64__customer_sources.sql
```

---

# Phase 8

## Widget

Widget depends on

Tenant

Service

```
V70__widgets.sql

V71__widget_origins.sql
```

---

## Patch: Auth deferred Widget FK

```
V72__auth_add_refresh_token_widget_fk.sql
```

`auth.refresh_tokens.widget_id` has existed as a nullable, FK-less column
since Phase 2 — `widget.widgets` didn't exist yet. Now that Widget is
built, this patch adds the FK via `ALTER TABLE`. Same deferred-ALTER
pattern as the Core ↔ Integration patch below. `oauth_client_id` remains
unresolved — no `oauth_clients` table exists anywhere yet.

---

# Phase 9

## Integration

```
V80__destination_calendars.sql

V81__destination_sheets.sql
```

---

## Patch: Core ↔ Integration circular FK

```
V85__core_add_destination_calendar_fk.sql
```

Now that `integration.destination_calendars` exists, add
`destination_calendar_id` to `core.services` and `core.bookings` via
`ALTER TABLE ... ADD COLUMN ... REFERENCES integration.destination_calendars(id)`.
This is the deferred-ALTER pattern used anywhere a genuine circular
dependency exists — create both sides without the mutual column first,
then patch the column in once both tables exist.

---

# Phase 10

## Notification

Notification depends on

Tenant

Auth

Customer

```
V90__notification_channels.sql

V91__notification_templates.sql

V92__notifications.sql

V93__notification_preferences.sql

V94__notification_queue.sql

V95__notification_deliveries.sql
```

Notice

Channels

↓

Templates

↓

Notifications

↓

Queue

↓

Deliveries

Dependency order.

---

# Phase 11

## Audit

Audit is last.

Everything depends on it.

Audit depends on almost everything.

```
V100__audit_actors.sql

V101__audit_events.sql

V102__audit_resources.sql

V103__audit_changes.sql
```

---

# Phase 12

## Indexes

I would not create indexes inside every table migration.

Instead

```
V200__indexes_auth.sql

V201__indexes_organization.sql

V202__indexes_tenant.sql

V203__indexes_core.sql

V204__indexes_customer.sql

V205__indexes_widget.sql

V206__indexes_integration.sql

V207__indexes_notification.sql

V208__indexes_audit.sql
```

Resource has no file — nothing in `RESOURCE_SCHEMA.md` documents an
`### Indexes` section for any of its tables, so there's nothing to add
beyond what its unique constraints already index.

Why?

Cleaner.

When optimizing

only index migrations change.

---

# Phase 13

## Seed Data

```
V300__seed_roles.sql

V301__seed_permissions.sql

V302__seed_plans.sql

V303__seed_ecosystems.sql

V304__seed_notification_channels.sql
```

Examples

```
FREE

STARTER

PRO
```

```
EMAIL

SMS

PUSH

IN_APP
```

```
PLATFORM_OWNER

BUSINESS_OWNER

STAFF
```

---

# Folder Structure

Instead of

```
db/migration

↓

150 sql files
```

Migrations are organized by domain subfolder, matching the phase list above:

```
db
└── migration
    ├── common
    │   ├── V1__create_extensions.sql
    │   ├── V2__create_schemas.sql
    │   ├── V3__create_enum_types.sql
    │   └── V4__create_organization_schema.sql
    │
    ├── auth
    │   ├── V10__auth_users.sql
    │   ├── V11__auth_user_passwords.sql
    │   ├── ...
    │   └── V34__auth_api_keys.sql   (deferred, see Phase 4)
    │
    ├── organization
    │   ├── V20__organization_organizations.sql
    │   ├── ...
    │
    ├── tenant
    │
    ├── core
    │
    ├── resource
    │
    ├── customer
    │
    ├── widget
    │
    ├── integration
    │
    ├── notification
    │
    └── audit
```

This is purely a filesystem convenience for navigation. **Flyway merges every configured location into one flat pool of migrations and sorts them by version number globally** — a file in `auth/` and a file in `core/` are ordered against each other exactly as if they lived in the same folder, and version numbers must stay unique across every folder combined. Subfolders do not create independent per-schema migration tracks; there is still exactly one `flyway_schema_history` table for the whole database. This is also why `V34__auth_api_keys.sql` can physically sit in the `auth/` folder while still executing after everything in `tenant/` — folder placement and execution order are independent.

This is now wired up via `spring.flyway.locations` in `application.yaml`:

```yaml
spring:
  flyway:
    locations:
      - classpath:db/migration/common
      - classpath:db/migration/auth
      - classpath:db/migration/organization
      - classpath:db/migration/tenant
      - classpath:db/migration/core
      - classpath:db/migration/resource
      - classpath:db/migration/customer
      - classpath:db/migration/widget
      - classpath:db/migration/integration
      - classpath:db/migration/notification
      - classpath:db/migration/audit
```

Domain subfolders are created as their first migration is written rather than pre-created empty — `billing`, `analytics`, and `webhook` are intentionally absent, per `AGENTS.md`.

If this platform is later decomposed into microservices, per-schema subfolders in one repo are not the mechanism for that. A truly independent service gets its own database/schema and its own separate Flyway instance with its own version sequence and its own history table at that point — not a shared global sequence split across folders. Splitting into independent Flyway configurations today, while everything still runs against one shared Postgres instance with cross-schema FKs (e.g. Core ↔ Integration), would reintroduce the exact ordering hazards this dependency-graph strategy exists to avoid.

---

# Dependency Graph

This is the most important thing.

```
Extensions
        │
        ▼
Schemas
        │
        ▼
Enums
        │
        ▼
Auth
        │
        ▼
Organization
        │
        ▼
Tenant
        │
        ▼
Core
        │
        ▼
Resource
        │
        ▼
Customer
        │
        ├───────────────┬────────────────┐
        ▼                ▼                ▼
     Widget         Integration      Notification
        │                │                │
        └────────────────┴────────────────┘
                          │
                          ▼
                        Audit
                          │
                          ▼
                        Indexes
                          │
                          ▼
                       Seed Data
```

Organization sits directly after Auth and before Tenant: it depends on Auth (`users`, `roles`) and nothing else, and Tenant depends on it (`tenant.tenants.organization_id`).

Widget, Integration, and Notification don't depend on each other — each only needs Auth/Organization/Tenant/Core/Customer, which already exist by this point. Flyway still requires *some* total order since it always executes versions sequentially, but the relative order of Phases 8/9/10 among themselves is arbitrary, not dependency-driven. Audit sits after all three because it can reference actors and resources originating from any of them.

That dependency order closely matches the architectural layering you've defined for PleaseBookMe and keeps each migration introducing only objects whose dependencies already exist.

# Recommendation

I would make one organizational adjustment that will pay off over the life of the project.

Instead of thinking:

> "I need to initialize the whole database."
> 

Think:

> **"I need to initialize the platform."**
> 

That means your initialization naturally follows the same order as your architecture:

1. **Platform Foundation** (extensions, schemas, enums)
2. **Identity & Security** (Auth)
3. **Business Identity** (Organization)
4. **Multi-tenancy** (Tenant)
5. **Business Engine** (Core)
6. **Allocatable Assets** (Resource)
7. **Business Relationships** (Customer)
8. **Distribution** (Widget)
9. **External Connectivity** (Integration)
10. **Communication** (Notification)
11. **Observability** (Audit)
12. **Performance** (Indexes)
13. **Bootstrap Data** (Seed)

This mirrors the platform architecture you've been building, keeps dependencies clear, and gives you a migration history that remains understandable years later as the platform evolves into the reservation infrastructure you've envisioned.
