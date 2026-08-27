# `customer.customer_tags`

> **Variant:** `ENTITY`

## Purpose

Attaches free-form labels to a customer's CRM record for segmentation and filtering.

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `customer_id` | `BIGINT` | The customer this tag is attached to. |
| `tag` | `VARCHAR(50)` | The tag label. |
| `created_at` / `updated_at` | `TIMESTAMPTZ` | Row creation/last-modified timestamps. |

## Row Semantics

Each row represents one tag applied to one customer.

## Ownership

- **Domain:** Customer
- **Module:** CRM
- **Scope:** Tenant (inherited through the parent customer).

## Lifecycle

Created via the standard CRUD service; updatable and deletable directly.

## Invariants

- `(customer_id, tag)` is unique — the same tag cannot be applied twice to the same customer.

## Relationships

- **Customer:** The customer this tag is attached to.

## Usage Rules

- Writes go through `CustomerTagServiceImpl`.
