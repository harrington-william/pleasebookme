# `resource.resource_maintenance`

> **Variant:** `STATE`

## Purpose

Blocks out a time window during which a resource is unavailable for maintenance.

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `resource_id` | `BIGINT` | The resource undergoing maintenance. |
| `start_time` / `end_time` | `TIMESTAMPTZ` | The maintenance window. |
| `reason` | `TEXT` | Why the resource is under maintenance. |
| `status` | `VARCHAR(50)` | Current status of the maintenance window. |
| `created_at` / `updated_at` | `TIMESTAMPTZ` | Row creation/last-modified timestamps. |

## Row Semantics

Each row represents one scheduled or in-progress maintenance window during which a resource should not be booked.

## Ownership

- **Domain:** Resource
- **Module:** Resource Management
- **Scope:** Organization (inherited through the parent resource).

## Lifecycle

Created when a maintenance window is scheduled. `status` presumably tracks whether the window is upcoming, active, or completed, though no enforced state machine was located in this session — it is a plain `VARCHAR`, not a native enum.

## Invariants

- `start_time`/`end_time`/`reason` are required.
- No unique constraint beyond the primary key — overlapping maintenance windows for the same resource are not prevented at the database level.

## Relationships

- **Resource:** The resource undergoing maintenance.

## Usage Rules

- Writes go through `ResourceMaintenanceServiceImpl`.
- Booking-availability computation is expected to treat an active maintenance window as blocking new bookings, though the actual enforcement path was not located in this session — flagged as **Undetermined**.

## Flagged for Follow-up

- Confirm whether `core`'s availability-computation logic actually reads `resource_maintenance` (or `resource_overrides`) when generating bookable slots. Only this table's own CRUD layer was inspected.
