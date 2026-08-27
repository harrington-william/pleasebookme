# Schedules

## Purpose:

Schedules

## Design:

### Fields

- id
- user_id
- title
- timezone? | Default = Australia/Sydney

### Indexes

- user_id

---

# Availabilities

## Purpose:

Availabilities

## Design:

### Fields

- id
- user_id
- schedule_id
- days
- start_time
- end_time

`days` implemented as `INTEGER[]` (weekday numbers, 0-6) — not specified
here originally, revisit if a different representation was intended.

### Indexes

- user_id
- schedule_id

---

# Services

## Purpose:

Business services

## Design:

### Fields

- id
- title
- slug
- description?
- interface_language? | Default = en
- location?
- user_id
- profile_id
- organization_id
- destination_calendar_id
- schedule_id
- period_type | Default = UNLIMITED
- timezone | Default Australia/Sydney
- min_price?
- max_price?
- currency? | Default USD
- requires_confirmation | Default false
- disable_cancelling | Default false
- disable_rescheduling | Default false
- success_redirect_url?
- is_instant_service | Default false
- max_active_booking_per_booker?
- metadata
- created_at
- updated_at

> min_price and max_price are used to define price range for frontend display. They will be inserted automatically by finding the lowest and the highest price from the resource.resource_pricing. For example, service HAIRCUT has 2 resource STYLIST #1 and STYLIST #2 with different prices, that range will be displayed on the frontend.
> 

`destination_calendar_id` is intentionally not in the Phase 5 migration.
It references `integration.destination_calendars`, which doesn't exist
until Phase 9, and that table references `core.services` right back — a
genuine circular dependency. It's added later via a deferred `ALTER TABLE`
patch once Integration exists (see `DATABASE_INIT_STRATEGY.md`).

`interface_language` and `currency` use the shared `public.locale` /
`public.currency` enums, not free text.

`period_type` is implemented as `VARCHAR(50)` pending a proper enum — only
`UNLIMITED` is documented as a value here, the rest of the value set is
undefined.

### Indexes

- user_id
- organization_id
- profile_id
- schedule_id

---

# Booking Policies

## Purpose:

Policies

## Design:

### Fields

- id
- service_id
- booking_mode | Default = FIXED
- duration_type
- default_duration | Default = 1
- minimum_duration?
- maximum_duration?
- minimum_notice
- maximum_advance_booking
- slot_interval? Default = 30
- before_buffer | Default = 0
- after_buffer | Default = 0
- allow_overlap | Default = false
- allow_multiple_attendee | Default = false
- requires_payment | Default = false
- auto_confirm = | Default = true
- booking_window_type
- capacity
- metadata
- created_at
- updated_at

`duration_type` and `booking_window_type` are implemented as `VARCHAR(50)`
pending proper enums — their value sets aren't documented here.

---

# Bookings

## Purpose:

Booking

## Design:

### Fields

id

uid

idempotency_key?

user_id

title

description?

start_time

end_time

service_id

location?

status | Default = PENDING

paid | Default = false

cancelled_by?

cancelation_reason?

rejection_reason?

destination_calendar_id

destination_sheets_id

`cancelled_by`, `rescheduled_by`, and `deleted_by` all reference
`auth.users(id)` (who performed the action), with `ON DELETE SET NULL` —
deleting a staff user shouldn't erase booking history.

`destination_calendar_id` and `destination_sheets_id` are both omitted
from the Phase 5 migration, same reason as `core.services` — deferred
until Phase 9 (Integration) via `ALTER TABLE`.

rescheduled | Default = false

rescheduled_by?

no_show_host | Default = false

deleted_at?

deleted_by?

metadata?

created_at

updated_at

### Indexes

- service_id
- user_id
- destination_calendar_id
- uid
- status
- start_time, end_time, status
- user_id, end_time
- user_id, status, start_time
- service_id, status
- user_id, created_at

---

# Attendees

## Purpose:

Attendee

## Design:

### Fields

- id
- booking_id
- email
- phone
- name
- locale? Optional but frontend should provide this (uses `public.locale`)
- timezone? Optional but frontend should provide this
- no_show | Default = false

### Indexes

- email
- phone
- booking_id
- email, booking_id
- phone, booking_id

---

# Selected Slots

## Purpose:

Prevent race conditions

## Design:

### Fields

- id
- service_id
- user_id
- slot_start
- slot_end
- uid
- release_at
- is_seat | Default = false
- created_at

### Unique

- service_id, user_id, slot_start, slot_end
- uid

The originally documented unique constraint was `(user_id, slot_start,
slot_end, uid)`. Since `uid` is unique per row, including it in the
composite key made the constraint permissive — it could never actually
block two overlapping holds, defeating this table's stated
race-condition-prevention purpose. `uid` now has its own uniqueness
instead, as a client idempotency key.

---

# Out Of Office

## Purpose:

OOO

## Design:

### Fields

- id
- uid
- start_time
- end_time
- notes?
- show_note_publicly | Default = false
- user_id
- to_user_id
- reason
- created_at
- updated_at

### Indexes

- uid
- user_id
- to_user_id
- start_time, end_time

---

# Booking Status (Enum)

- CANCELLED
- PENDING
- ACCEPTED
- REJECTED
- AWAITING_HOST

---

# Booking Modes (Enum)

- FIXED
- FLEXIBLE
- HYBRID