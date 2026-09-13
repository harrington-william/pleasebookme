# `resource.resource_overrides`

> **Variant:** `STATE`

## Purpose

Records a one-off change to a resource's normal availability for a specific time window, distinct from a recurring schedule or a maintenance closure.

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `resource_id` | `BIGINT` | The resource this override applies to. |
| `start_time` / `end_time` | `TIMESTAMPTZ` | The override window. |
| `reason` | `TEXT` | Why the override was made. |
| `created_at` / `updated_at` | `TIMESTAMPTZ` | Row creation/last-modified timestamps. |

## Row Semantics

Each row represents one one-off availability override for a resource over a specific time window.

## Ownership

- **Domain:** Resource
- **Module:** Resource Management
- **Scope:** Organization (inherited through the parent resource).

## Lifecycle

Created when a one-off override is needed. Identical shape to `resource.resource_maintenance` minus the `status` column — this table has no status field of its own, so an override's applicability is determined entirely by its time window.

## Invariants

- `start_time`/`end_time`/`reason` are required.
- No unique constraint beyond the primary key.

## Relationships

- **Resource:** The resource this override applies to.

## Usage Rules

- Writes go through `ResourceOverrideServiceImpl`.
