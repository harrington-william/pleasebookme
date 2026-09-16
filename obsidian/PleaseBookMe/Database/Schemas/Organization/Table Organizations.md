# `organization.organizations`

> **Variant:** `ENTITY`

## Purpose

Stores the businesses on the platform — the root business-identity entity every staff profile, membership, service, resource, and tenant subscription ultimately traces back to.

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `name` | `VARCHAR(255)` | Organization's display name. |
| `slug` | `VARCHAR(255)` | Unique URL-safe identifier. |
| `logo_url` / `banner_url` | `TEXT` | Optional branding image links. |
| `bio` | `TEXT` | Optional description. |
| `is_private` | `BOOLEAN` | Whether the organization is private. |
| `metadata` | `JSONB` | Free-form, application-defined data. |
| `timezone` | `VARCHAR(100)` | Organization's default timezone. |
| `week_start` | `public.week_start` | Organization's preferred first day of the week. |
| `created_at` / `updated_at` | `TIMESTAMPTZ` | Row creation/last-modified timestamps. |

## Row Semantics

Each row represents one business on the platform — the entity every user's workspace, staff membership, and (once subscribed) tenant configuration is built on top of.

## Ownership

- **Domain:** Organization
- **Module:** Business Identity
- **Scope:** Platform-wide root — an organization has no parent scope; every other business-hierarchy concept scopes underneath it.

## Lifecycle

Created automatically as part of user provisioning — every newly registered or Google-onboarded user receives a starter organization named `"<name>'s Organization"` (`WorkspaceProvisioningService.provision()`), created atomically alongside the user, a self-accepted membership, and a profile. Also directly creatable via generic CRUD. No subscription is required for an organization to exist or operate the booking engine — see `tenant.tenants` for the separate, optional subscription layer.

## Invariants

- `slug` is globally unique; the provisioning flow falls back to the user's uid as the slug if the derived name-based slug collides.
- An organization requiring no active tenant subscription to exist is a deliberate product rule, not an oversight — `core`/`resource` scope by `organization_id`, not `tenant_id`.

## Relationships

- **Tenant:** `tenant.tenants.organization_id` — an optional subscription overlay, created only once the organization subscribes to a plan.
- **Profile / Membership:** Staff belong to an organization through these two tables.
- **Service / Resource / Customer:** All scope directly by `organization_id`.

## Usage Rules

- Writes go through `OrganizationServiceImpl` (`organization/organizations/`), or implicitly through `WorkspaceProvisioningService` at registration time.

## Important Fields

- `slug` — The organization's externally-facing identifier; collision-checked and auto-resolved during provisioning.
