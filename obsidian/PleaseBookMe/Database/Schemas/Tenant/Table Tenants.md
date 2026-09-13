# `tenant.tenants`

> **Variant:** `ENTITY`

## Purpose

Represents an organization's active subscription to a plan — the SaaS account layer that carries quotas, region, and billing-adjacent configuration on top of an organization's business identity.

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `uid` | `UUID` | Stable, externally-safe identifier for this tenant. |
| `organization_id` | `BIGINT` | The organization this subscription belongs to. |
| `owner_user_id` | `BIGINT` | The user accountable for the subscription. |
| `ecosystem_id` | `BIGINT` | The business-category ecosystem this tenant belongs to (e.g. barbershop). |
| `name` | `VARCHAR(255)` | Tenant display name. |
| `slug` | `VARCHAR(255)` | Unique URL-safe identifier. |
| `status` | `tenant.tenant_status` | `ACTIVE` / `SUSPENDED` / `TRIAL` / `PENDING` / `ARCHIVED`. |
| `plan_id` | `BIGINT` | The subscribed plan. |
| `region` | `tenant.region` | Deployment/data region (`AU`/`UK`/`US`/`SG`/`VN`). |
| `default_timezone` | `VARCHAR(100)` | Tenant-wide default timezone. |
| `default_locale` | `public.locale` | Tenant-wide default locale. |
| `max_users` / `max_services` / `max_widgets` | `INTEGER` | Quota limits, copied from the subscribed plan at creation time. |
| `settings` | `JSONB` | Free-form, application-defined tenant configuration. |
| `created_at` / `updated_at` | `TIMESTAMPTZ` | Row creation/last-modified timestamps. |

## Row Semantics

Each row represents one active subscription an organization holds to a plan — not the organization itself, and not required for the organization to exist or operate the booking engine.

## Ownership

- **Domain:** Tenant
- **Module:** SaaS Account Configuration
- **Scope:** Organization (one subscription overlay per organization) → Owner User.

## Lifecycle

Created automatically inside the same transaction as user registration. New organizations receive an `ACTIVE` tenant on the `FREE` plan and `GENERAL` ecosystem; quotas are copied from the plan, timezone/locale come from the user, and region derives from timezone with `VN` as the fallback. V141 backfills organizations that predate provisioning. Tenants can also be managed through generic CRUD.

## Invariants

- `slug` is globally unique.
- `organization_id` is unique since V138, enforcing one tenant subscription overlay per organization.
- `ecosystem_id`/`plan_id` are resolved via real repository lookups in `TenantServiceImpl` (upgraded from an earlier bare-reference-entity workaround once `EcosystemRepository`/`TenantPlanRepository` were built).
- `UserPrincipal.tenantUid` remains null by design in the current identity loader; request-time tenant resolution uses `CurrentTenantProvider` over organization context instead.

## Relationships

- **Organization:** The organization this subscription belongs to.
- **Owner User:** The user accountable for the subscription.
- **Ecosystem / Plan:** Classify the tenant's business category and quota tier.
- **Customer / Widget / API Key / Notification:** All scope directly by `tenant_id` — this is the isolation boundary for the platform's paid-tier capabilities, distinct from the `organization_id` boundary that `core`/`resource` use.

## Usage Rules

- Writes go through `TenantServiceImpl` (`tenant/tenants/`).
- New registrations have a tenant; legacy data relies on V141 and missing rows fail explicitly rather than being created lazily.

## Important Fields

- `max_users`/`max_services`/`max_widgets` — Denormalized quota values, expected to be copied from the subscribed plan at creation time rather than looked up live on every check.

## Flagged for Follow-up

- Schedule, availability, and notification-preference provisioning remain open in ISSUE-0002.
