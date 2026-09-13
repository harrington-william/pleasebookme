# `core.availabilities`

> **Variant:** `CONFIGURATION`

## Purpose

Defines a recurring weekly time window during which a schedule is open, which the platform's slot-generation logic reads to compute bookable times.

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `user_id` | `BIGINT` | The user this availability window belongs to. |
| `schedule_id` | `BIGINT` | The schedule this window is part of. |
| `days` | `INTEGER[]` | The day(s) of the week this window recurs on. |
| `start_time` / `end_time` | `TIME` | The recurring daily window. |
| `created_at` / `updated_at` | `TIMESTAMPTZ` | Row creation/last-modified timestamps. |

## Row Semantics

Each row represents one recurring weekly time window — a set of days plus a start/end time — during which the parent schedule is open for bookings.

## Ownership

- **Domain:** Core
- **Module:** Scheduling
- **Scope:** User (via the owning schedule).

## Lifecycle

Created when a schedule's recurring hours are configured. Per the platform's MVP v1 findings (`CLAUDE.md`), available slots are computed dynamically from these rows at read time — the platform deliberately does not store pre-generated slot rows.

## Invariants

- `days` is a native Postgres array, not a join table — a single row can recur across multiple days at once.
- No unique constraint beyond the primary key — overlapping windows for the same schedule are not prevented at the database level.

## Relationships

- **User:** The user this window belongs to.
- **Schedule:** The parent schedule this window contributes to.

## Usage Rules

- Writes go through `AvailabilityServiceImpl`.
- Slot/availability computation is expected to read this table dynamically rather than from any pre-generated slot store — see `core.selected_slots` for the one place a specific slot *is* persisted, and only as a short-lived hold during checkout.
