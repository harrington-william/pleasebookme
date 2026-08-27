# `tenant.plans`

> **Variant:** `REFERENCE`

## Purpose

Defines the subscription tiers a tenant can be on — pricing and quota limits.

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `code` | `VARCHAR(50)` | Unique short identifier for the plan. |
| `name` | `VARCHAR(100)` | Display name. |
| `price` | `NUMERIC(10,2)` | Plan price. |
| `currency` | `public.currency` | Currency the price is denominated in. |
| `max_users` / `max_services` / `max_widgets` / `max_resources` / `max_api_keys` | `INTEGER` | Quota limits for a tenant subscribed to this plan; nullable (see Invariants). |
| `features` | `JSONB` | Free-form feature-entitlement data. |
| `metadata` | `JSONB` | Free-form, application-defined data. |
| `created_at` / `updated_at` | `TIMESTAMPTZ` | Row creation/last-modified timestamps. |

## Row Semantics

Each row represents one subscription plan tier a tenant can be placed on.

## Ownership

- **Domain:** Tenant
- **Module:** SaaS Account Configuration
- **Scope:** Platform (global).

## Lifecycle

Seeded via migration (`V102__seed_plans.sql`), which currently seeds only `FREE`. Additional plans can be created through the standard CRUD service.

## Invariants

- `code` is globally unique.
- All five `max_*` columns were originally `NOT NULL` but were relaxed to nullable by `V101__drop_plans_not_null.sql`. The seeded `FREE` plan only populates `max_services`/`max_resources` — `max_users`, `max_widgets`, and `max_api_keys` are currently `NULL` on it. This is a known, documented blocker: `tenant.tenants`'s own `max_users`/`max_services`/`max_widgets` columns are `NOT NULL`, so copying the `FREE` plan's limits onto a new tenant would fail on the two unset columns until the seed is completed or the provisioning code adds fallbacks.

## Relationships

- **Tenant:** `tenant.tenants.plan_id` — a tenant's quota limits are (intended to be) copied from its subscribed plan.

## Usage Rules

- Writes go through `TenantPlanServiceImpl` (`tenant/plan/`).
