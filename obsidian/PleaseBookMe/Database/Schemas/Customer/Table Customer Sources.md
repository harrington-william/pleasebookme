# `customer.customer_sources`

> **Variant:** `ENTITY`

## Purpose

Records where a customer originated from (e.g. a marketing channel or referral source).

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `customer_id` | `BIGINT` | The customer this source attribution concerns. |
| `source` | `VARCHAR(50)` | The acquisition source. |
| `created_at` | `TIMESTAMPTZ` | When this source record was written. |

## Row Semantics

Each row represents one acquisition-source attribution recorded for a customer.

## Ownership

- **Domain:** Customer
- **Module:** CRM
- **Scope:** Tenant (inherited through the parent customer).

## Lifecycle

Created via the standard CRUD service. No unique constraint is declared beyond the primary key, so a customer can accumulate multiple source records over time. `update`/`delete` are implemented, despite the table having only `created_at` and no `updated_at`.

## Invariants

- No unique constraint beyond the primary key — multiple source rows per customer are permitted by the schema.

## Relationships

- **Customer:** The customer this source attribution concerns.

## Usage Rules

- Writes go through `CustomerSourceServiceImpl`.
