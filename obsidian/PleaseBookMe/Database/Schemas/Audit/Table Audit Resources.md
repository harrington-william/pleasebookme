# `audit.audit_resources`

> **Variant:** `AUDIT`

## Purpose

Records which specific resource an audit event affected, and captures that resource's state before and after the change.

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `event_id` | `BIGINT` | The audit event this resource record belongs to. |
| `resource_type` | `audit.resource_type` | The kind of resource affected (e.g. `BOOKING`, `SERVICE`, `USER`, `TENANT`). |
| `resource_uid` | `VARCHAR(255)` | The affected resource's external identifier. |
| `resource_name` | `VARCHAR(255)` | Human-readable label for the affected resource. |
| `before_snapshot` | `JSONB` | Optional state of the resource before the change. |
| `after_snapshot` | `JSONB` | State of the resource after the change; required. |
| `created_at` | `TIMESTAMPTZ` | When this resource record was written. |

## Row Semantics

Each row represents one resource affected by one audit event, along with a snapshot of that resource's state before and after the recorded change.

## Ownership

- **Domain:** Audit
- **Module:** Audit & Compliance
- **Scope:** Inherits its tenant/organization context from the parent `audit.audit_events` row; this table carries no tenant/organization column of its own.

## Lifecycle

Created alongside (or shortly after) the `audit.audit_events` row it belongs to, once per resource the event affected. Only `create`/`getById`/`getAll` are implemented — there is no `update` or `delete`, per the audit domain's append-only business policy.

## Invariants

- `event_id` is required and foreign-keys into `audit.audit_events` with `ON DELETE CASCADE`.
- `after_snapshot` is required; `before_snapshot` is optional (a resource being created for the first time has no meaningful "before" state).
- No unique constraint exists beyond the primary key, so no `Duplicate*Exception` is defined for this table.
- No `update`/`delete` verb exists.

## Relationships

- **Audit Event:** The event this resource record was captured as part of.
- **Audit Change:** `audit.audit_changes` rows attach field-level diffs to this resource record.

## Usage Rules

- Writes go through `AuditResourceServiceImpl` — create and read only.

## Important Fields

- `before_snapshot` / `after_snapshot` — **deliberately kept in both `AuditResourceRequest` and `AuditResourceResponse`**, unlike a typical free-form `metadata`/`context` column elsewhere in the schema (e.g. `audit.audit_events.context`, excluded from both DTOs). These two JSONB columns are not optional contextual data — they *are* the core payload this table exists to record. Excluding them would make the table effectively uncreatable via the API, since `after_snapshot` is required.

## Immutability

Records are append-only after creation.

## Audit Behavior

This table is the resource dimension of the audit subsystem — it answers "what was affected" for a given event, and its before/after snapshot pair is what lets a later reader reconstruct exactly what changed without needing the live table's current or historical state.

## Security Considerations

Snapshots may contain sensitive resource data captured at the time of the event. Access should be restricted according to the platform's audit authorization policy, same as `audit.audit_events`.
