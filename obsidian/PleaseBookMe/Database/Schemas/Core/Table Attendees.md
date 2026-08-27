# `core.attendees`

> **Variant:** `ENTITY`

## Purpose

Captures the contact details of a person attending a booking — a point-in-time snapshot taken at booking time, distinct from any durable CRM identity.

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `booking_id` | `BIGINT` | The booking this attendee is part of. |
| `email` | `VARCHAR(255)` | Optional email address. |
| `phone` | `VARCHAR(50)` | Phone number. |
| `name` | `VARCHAR(255)` | Attendee's name. |
| `locale` | `public.locale` | Optional preferred language. |
| `timezone` | `VARCHAR(100)` | Optional preferred timezone. |
| `no_show` | `BOOLEAN` | Whether the attendee failed to show. |
| `created_at` | `TIMESTAMPTZ` | When the attendee record was created. |

## Row Semantics

Each row represents one person attending one booking, captured as a contact-details snapshot at booking time.

## Ownership

- **Domain:** Core
- **Module:** Reservation Lifecycle
- **Scope:** Organization (transitively, via the parent booking).

## Lifecycle

Created when a booking is made, one row per attendee. Updatable (real mutable columns: `email`, `phone`, `name`, `locale`, `timezone`, `noShow`) and deletable, despite having only `created_at` and no `updated_at`.

## Invariants

- `phone`/`name` are required; `email`/`locale`/`timezone` are optional.
- No unique constraint beyond the primary key.

## Relationships

- **Booking:** The booking this attendee is part of.
- **Customer (conceptual, not a foreign key):** `customer.customers` is the durable CRM identity a tenant maintains for a person; this table is the per-booking contact snapshot taken at the time of that specific reservation. The two are deliberately separate concepts, not a normalization the schema collapsed — an attendee record does not update if the customer's CRM profile changes later.

## Usage Rules

- Writes go through `AttendeeServiceImpl`.
