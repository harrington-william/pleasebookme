# `core.booking_policies`

> **Variant:** `CONFIGURATION`

## Purpose

Defines the reservation rules a service's bookings must obey — duration model, notice/lead-time limits, buffers, capacity, and confirmation behavior.

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `service_id` | `BIGINT` | The service this policy governs. |
| `booking_mode` | `core.booking_mode` | `FIXED` / `FLEXIBLE` / `HYBRID`. |
| `duration_type` | `VARCHAR(50)` | How booking duration is determined. |
| `default_duration` | `INTEGER` | Default booking length. |
| `minimum_duration` / `maximum_duration` | `INTEGER` | Optional duration bounds. |
| `minimum_notice` | `INTEGER` | How much lead time a booking requires. |
| `maximum_advance_booking` | `INTEGER` | How far ahead a booking may be made. |
| `slot_interval` | `INTEGER` | Granularity of generated slots. |
| `before_buffer` / `after_buffer` | `INTEGER` | Padding time around a booking. |
| `allow_overlap` | `BOOLEAN` | Whether overlapping bookings are permitted. |
| `allow_multiple_attendee` | `BOOLEAN` | Whether a booking may have more than one attendee. |
| `requires_payment` | `BOOLEAN` | Whether payment is required to complete a booking. |
| `auto_confirm` | `BOOLEAN` | Whether a booking is confirmed automatically (defaults to `true`). |
| `booking_window_type` | `VARCHAR(50)` | How the booking window is bounded. |
| `capacity` | `INTEGER` | Maximum concurrent bookings/attendees. |
| `metadata` | `JSONB` | Free-form, application-defined data. |
| `created_at` / `updated_at` | `TIMESTAMPTZ` | Row creation/last-modified timestamps. |

## Row Semantics

Each row represents the reservation policy configuration for one service — the rules that determine what a valid booking against that service looks like.

## Ownership

- **Domain:** Core
- **Module:** Booking Configuration
- **Scope:** Organization (inherited through the parent service).

## Lifecycle

Created alongside or shortly after a service, via the standard CRUD service. No unique constraint ties this table to exactly one row per service at the database level, despite the product intent of one active policy per service.

## Invariants

- No unique constraint beyond the primary key is declared — the schema does not itself enforce one-policy-per-service, though that is the evident product intent.
- `duration_type`/`booking_window_type` are required with no SQL default, unlike most of this table's other fields.
- `auto_confirm` defaults to `true` — the only boolean on this table (and one of the few in the whole schema) that defaults to `true` rather than `false`.

## Relationships

- **Service:** The service this policy governs.

## Usage Rules

- Writes go through `BookingPolicyServiceImpl`.

## Important Fields

- `booking_mode`/`duration_type`/`booking_window_type` — Together determine how the platform's slot-generation logic interprets a service's availability into bookable windows; this is the heart of the reservation engine's genericism across business types (appointment vs. reservation vs. rental).
