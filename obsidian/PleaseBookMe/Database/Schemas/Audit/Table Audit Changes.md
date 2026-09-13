# `audit.audit_changes`

> **Variant:** `AUDIT`

## Purpose

Records one field-level change within a resource captured by an audit event — the finest-grained level of detail in the audit subsystem.

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `resource_id` | `BIGINT` | The audit resource record this field change belongs to. |
| `field_name` | `VARCHAR(100)` | Name of the field that changed. |
| `old_value` | `TEXT` | Optional prior value, as text. |
| `new_value` | `TEXT` | Optional new value, as text. |
| `occurred_at` | `TIMESTAMPTZ` | When this change was recorded. |

## Row Semantics

Each row represents one field-level change (a single field's old value and new value) belonging to one audited resource.

## Ownership

- **Domain:** Audit
- **Module:** Audit & Compliance
- **Scope:** Inherits its context transitively from the `audit.audit_resources` → `audit.audit_events` chain; this table carries no tenant/organization column of its own.

## Lifecycle

Created alongside the `audit.audit_resources` row it details, typically one row per field that changed. Only `create`/`getById`/`getAll` are implemented — there is no `update` or `delete`, per the audit domain's append-only business policy.

## Invariants

- `resource_id` is required and foreign-keys into `audit.audit_resources` with `ON DELETE CASCADE`.
- `field_name` is required; `old_value`/`new_value` are both optional (a newly created field has no meaningful "old" value; a removed field has no meaningful "new" value).
- No unique constraint exists beyond the primary key, so no `Duplicate*Exception` is defined for this table.
- No `update`/`delete` verb exists.
- Despite having only one timestamp column and it being named `occurred_at` rather than `created_at`, the column is still `@CreationTimestamp`-backed and immutable — the naming reflects that a field change is itself a point-in-time fact, not bookkeeping metadata.

## Relationships

- **Audit Resource:** The resource-level audit record this field change details.

## Usage Rules

- Writes go through `AuditChangeServiceImpl` — create and read only.

## Immutability

Records are append-only after creation.

## Audit Behavior

This table is the field-level detail layer beneath `audit.audit_resources` — where a resource's before/after snapshot captures the whole picture, this table optionally decomposes that difference into individual field-level entries for finer-grained review.

## Security Considerations

`old_value`/`new_value` may contain sensitive field data captured at the time of the change. Access should be restricted according to the platform's audit authorization policy, same as the rest of this subsystem.
