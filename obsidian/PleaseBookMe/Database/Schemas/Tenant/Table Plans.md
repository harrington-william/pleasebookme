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

Seeded via migration (`V102__seed_plans.sql`), which currently seeds only `FREE`; V139 completes its provisional free-tier limits. Additional plans can be created through the standard CRUD service.

## Invariants

- `code` is globally unique.
- All five `max_*` columns are nullable at the schema level after V101, but provisioning requires the FREE plan's tenant quotas to be present. V139 sets FREE to `max_users = 1`, `max_services = 10`, `max_widgets = 3`, `max_resources = 10`, and `max_api_keys = 1`; missing tenant-required limits fail registration loudly.

## Relationships

- **Tenant:** `tenant.tenants.plan_id` — a tenant's quota limits are (intended to be) copied from its subscribed plan.

## Usage Rules

- Writes go through `TenantPlanServiceImpl` (`tenant/plan/`).
