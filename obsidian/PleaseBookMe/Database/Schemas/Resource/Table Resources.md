# `resource.resources`

> **Variant:** `ENTITY`

## Purpose

Stores every allocatable asset that can participate in a reservation — the thing being booked, distinct from the booking itself and from the service it's booked under.

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `uid` | `UUID` | Stable, externally-safe identifier for this resource. |
| `organization_id` | `BIGINT` | The organization that owns this resource. |
| `resource_type_id` | `BIGINT` | The category this resource belongs to. |
| `name` | `VARCHAR(255)` | Display name (e.g. "Room 101," "Barber Chair 2"). |
| `slug` | `VARCHAR(255)` | URL-safe identifier, unique per organization. |
| `description` | `TEXT` | Optional description of the resource. |
| `capacity` | `INTEGER` | Optional number of bookings/attendees this resource can hold concurrently. |
| `status` | `resource.resource_status` | Required operational status: `ACTIVE`, `INACTIVE`, `MAINTENANCE`, or `RETIRED`. |
| `is_bookable` | `BOOLEAN` | Whether this resource currently accepts new bookings. |
| `is_virtual` | `BOOLEAN` | Whether this resource is virtual (no physical presence). |
| `metadata` | `JSONB` | Free-form, application-defined data. |
| `created_at` / `updated_at` | `TIMESTAMPTZ` | Row creation/last-modified timestamps. |

## Row Semantics

Each row represents one bookable asset — a barber chair, hotel room, court, desk, or vehicle — that a reservation can be allocated against.

## Ownership

- **Domain:** Resource
- **Module:** Resource Management
- **Scope:** Organization.

## Lifecycle

Created through the standard CRUD service by an organization admin. `is_bookable` is the mechanism intended to take a resource out of new-booking circulation while preserving its historical relationships (existing bookings, assignments, pricing history) — analogous to how `core.services.status`-like fields work elsewhere in the platform, though no code path enforcing "an unbookable resource must reject new bookings" was located in this session; it appears to be a flag the booking-orchestration layer is expected to check.

## Invariants

- `(organization_id, slug)` is unique.
- `status` is required and has no database default; callers must choose it explicitly.
- `description` and `capacity` may be null.

## Relationships

- **Organization:** Owns the resource.
- **Service:** Many-to-many assignments are stored in [[Table Resource Services]].
- **Resource Type:** Classifies this resource.
- **Resource Pricing / Assignment / Calendar / Maintenance / Attribute / Override:** All scope directly by `resource_id`, attaching pricing, staff assignment, availability, downtime, custom attributes, and one-off overrides respectively.

## Usage Rules

- Writes go through `ResourceServiceImpl` (`resource/resources/`).

## Important Fields

- `status` — Intended to gate booking eligibility, though enforcement was not located in this session; treat as **Undetermined** whether it is currently checked at booking time.
- `is_bookable` — Same caveat as `status`.

## Flagged for Follow-up

- Confirm whether `core`'s booking-creation path actually checks `resources.status`/`is_bookable` before allocating a resource to a booking. Only this table's own CRUD layer was inspected in this session — the booking-orchestration side was not.
