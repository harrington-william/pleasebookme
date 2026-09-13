# `core.schedules`

> **Variant:** `ENTITY`

## Purpose

Stores a named, timezone-scoped container of availability that one or more services book against.

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `user_id` | `BIGINT` | The user this schedule belongs to. |
| `title` | `VARCHAR(255)` | Display name for the schedule (e.g. "Working Hours"). |
| `timezone` | `VARCHAR(100)` | The timezone this schedule's availability windows are interpreted in. |
| `created_at` / `updated_at` | `TIMESTAMPTZ` | Row creation/last-modified timestamps. |

## Row Semantics

Each row represents one named schedule — a timezone-scoped container that a set of recurring availability windows is attached to, and that one or more services reference to determine when they can be booked.

## Ownership

- **Domain:** Core
- **Module:** Scheduling
- **Scope:** User.

## Lifecycle

Created via the standard CRUD service. A schedule has no meaningful state beyond its title/timezone — its actual availability is entirely defined by the `core.availabilities` rows attached to it, so creating a schedule with no availabilities is valid but produces no bookable time.

## Invariants

- No unique constraint beyond the primary key — a user can have multiple schedules with the same title.

## Relationships

- **User:** The user this schedule belongs to.
- **Availability:** `core.availabilities.schedule_id` — the recurring weekly windows that make up this schedule.
- **Service:** `core.services.schedule_id` — a service uses this schedule to determine when it can be booked.
- **Resource Calendar:** `resource.resource_calendars.schedule_id` — a resource can also follow this schedule's availability.

## Usage Rules

- Writes go through `ScheduleServiceImpl`.
