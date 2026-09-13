# `resource.resource_calendars`

> **Variant:** `ASSOCIATION`

## Purpose

Links a resource to the availability schedule that governs when it can be booked.

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `resource_id` | `BIGINT` | The resource this calendar link applies to. |
| `schedule_id` | `BIGINT` | The `core.schedules` schedule governing this resource's availability. |
| `created_at` / `updated_at` | `TIMESTAMPTZ` | Row creation/last-modified timestamps. |

## Row Semantics

Each row represents one link between a resource and the availability schedule that determines when it can be booked.

## Ownership

- **Domain:** Resource
- **Module:** Resource Management
- **Scope:** Organization (inherited through the parent resource).

## Lifecycle

Created when a resource is linked to a schedule. Both FK associations are mutable, so the standard `update` verb is included — changing which resource/schedule a calendar link points to is a meaningful mutation.

## Invariants

- No unique constraint beyond the primary key — the schema does not itself prevent a resource from being linked to multiple schedules.

## Relationships

- **Resource:** The resource this calendar link applies to.
- **Schedule:** The `core.schedules` availability schedule this resource follows.

## Usage Rules

- Writes go through `ResourceCalendarServiceImpl`.
