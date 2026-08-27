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

**Not yet created automatically at registration** — this is a known, documented gap (`SERVER_AGENTS.md`'s "Workspace provisioning" section): `UserProvisioningService.provisionUser()` does not currently write a `tenant.tenants` row, so a freshly registered user's organization has no tenant by default. Every account today therefore has a null tenant relationship from `UserPrincipal`'s perspective. Creating a tenant (e.g. onto the `FREE` plan) is planned future provisioning work, blocked on several schema/seed gaps documented in `SERVER_AGENTS.md` (missing `max_*` values on the seeded `FREE` plan, only one ecosystem seeded, no default `region`/`status` chosen for self-serve signup). Tenants can be created directly via generic CRUD today.

## Invariants

- `slug` is globally unique.
- `ecosystem_id`/`plan_id` are resolved via real repository lookups in `TenantServiceImpl` (upgraded from an earlier bare-reference-entity workaround once `EcosystemRepository`/`TenantPlanRepository` were built).
- A `UserPrincipal` is not guaranteed to have a corresponding tenant — code reading `tenantUid` off a principal must null-check it and treat `null` as "no active plan," per `SECURITY.md`'s Tenant Optionality section.

## Relationships

- **Organization:** The organization this subscription belongs to.
- **Owner User:** The user accountable for the subscription.
- **Ecosystem / Plan:** Classify the tenant's business category and quota tier.
- **Customer / Widget / API Key / Notification:** All scope directly by `tenant_id` — this is the isolation boundary for the platform's paid-tier capabilities, distinct from the `organization_id` boundary that `core`/`resource` use.

## Usage Rules

- Writes go through `TenantServiceImpl` (`tenant/tenants/`).
- Do not assume every organization or every `UserPrincipal` has a tenant — see Lifecycle and Invariants.

## Important Fields

- `max_users`/`max_services`/`max_widgets` — Denormalized quota values, expected to be copied from the subscribed plan at creation time rather than looked up live on every check.

## Flagged for Follow-up

- `UserProvisioningService.provisionUser()` does not write a `tenant.tenants` row — automatic tenant creation at registration is not implemented. Before building it, three seed/schema gaps need resolving: (1) [[Table Plans]]'s seeded `FREE` row is missing `max_users`/`max_widgets`/`max_api_keys`; (2) [[Table Ecosystems]] seeds only `BARBERSHOP`, no neutral fallback for other business types; (3) no default `region`/`status` has been chosen for self-serve signup. All three are pre-existing, documented in `SERVER_AGENTS.md`, not new findings from this pass — flagged here for visibility alongside the rest of the schema docs.
