# `customer.customer_activities`

> **Variant:** `EVENT`

## Purpose

Logs activity entries against a customer's CRM record, linking to the platform event/entity that produced them.

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `customer_id` | `BIGINT` | The customer this activity concerns. |
| `activity_type` | `VARCHAR(50)` | The kind of activity recorded. |
| `reference_type` | `VARCHAR(50)` | The kind of entity this activity references (e.g. a booking). |
| `reference_uid` | `VARCHAR(255)` | The external identifier of the referenced entity. |
| `description` | `TEXT` | Optional human-readable description. |
| `metadata` | `JSONB` | Free-form, application-defined data. |
| `occurred_at` | `TIMESTAMPTZ` | When the activity occurred. |

## Row Semantics

Each row represents one activity-log entry recorded against a customer, referencing the entity elsewhere in the platform that the activity concerns.

## Ownership

- **Domain:** Customer
- **Module:** CRM
- **Scope:** Tenant (inherited through the parent customer).

## Lifecycle

Created whenever a customer-relevant activity is recorded. `occurred_at` is `@CreationTimestamp`-backed and immutable, but the table has no separate `updated_at` — despite this, the standard `update` verb is implemented, since `activityType`/`referenceType`/`referenceUid`/`description` remain mutable columns.

## Invariants

- No unique constraint beyond the primary key.
- `reference_type`/`reference_uid` identify the related entity by type and external id rather than a real foreign key — this table can reference any domain's entity without a schema dependency on it.

## Relationships

- **Customer:** The customer this activity concerns.
- **Referenced entity (conceptual, not a foreign key):** identified by `reference_type`/`reference_uid`, not enforced at the database level.

## Usage Rules

- Writes go through `CustomerActivityServiceImpl`.
