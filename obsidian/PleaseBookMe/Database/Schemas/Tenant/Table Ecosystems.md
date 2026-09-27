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

Seeded with the vertical-specific `BARBERSHOP` row in V100 and the neutral active `GENERAL` row in V140. Registration assigns `GENERAL`; additional ecosystems can be created through the standard CRUD service.

V144 re-seeds `BARBERSHOP` with `ON CONFLICT (code) DO NOTHING` and is therefore a **no-op** — it was written for the widget library without noticing V100, so the row keeps V100's name `Barbershops`. It is retained because it has already been applied and removing an applied migration fails Flyway's validate at boot.

`code` is what the widget library's `registry.ts` switches on to choose which ecosystem's widget to render, so these values are a wire contract, not just labels.

## Invariants

- `code` is globally unique.
- `GENERAL` is the neutral fallback for self-serve tenants that have not selected a listed vertical.

## Relationships

- **Tenant:** `tenant.tenants.ecosystem_id` classifies each tenant against one ecosystem.

## Usage Rules

- Writes go through `EcosystemServiceImpl` (`tenant/ecosystem/`).
