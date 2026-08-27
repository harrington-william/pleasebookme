# `tenant.ecosystems`

> **Variant:** `REFERENCE`

## Purpose

Defines the business-category vocabulary a tenant is classified under (e.g. barbershop), used to tailor the platform's behavior or presentation to a specific vertical.

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `code` | `VARCHAR(50)` | Unique short identifier for the ecosystem. |
| `name` | `VARCHAR(100)` | Display name. |
| `description` | `TEXT` | Description of the ecosystem. |
| `icon` | `VARCHAR(100)` | Display icon. |
| `status` | `tenant.ecosystem_status` | `REVIEWING` / `ACTIVE` / `SUSPENDED` / `DISCONTINUED`. |
| `metadata` | `JSONB` | Free-form, application-defined data. |
| `created_at` / `updated_at` | `TIMESTAMPTZ` | Row creation/last-modified timestamps. |

## Row Semantics

Each row represents one business-category vertical a tenant can be classified under.

## Ownership

- **Domain:** Tenant
- **Module:** SaaS Account Configuration
- **Scope:** Platform (global).

## Lifecycle

Seeded via migration (`V100__seed_ecosystems.sql`), which currently contains only one row: `BARBERSHOP`. Additional ecosystems can be created through the standard CRUD service. This is a known, documented gap — a rental, court, or coworking business currently has no valid, purpose-fit ecosystem to point at; `tenant.tenants.ecosystem_id` is `NOT NULL`.

## Invariants

- `code` is globally unique.
- Only one ecosystem exists in the seeded data as of the current migration state (`BARBERSHOP`) — every non-barbershop tenant is currently forced to use it or block on a new ecosystem being added.

## Relationships

- **Tenant:** `tenant.tenants.ecosystem_id` classifies each tenant against one ecosystem.

## Usage Rules

- Writes go through `EcosystemServiceImpl` (`tenant/ecosystem/`).
