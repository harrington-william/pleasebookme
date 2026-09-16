# `core.services`

> **Variant:** `ENTITY`

## Purpose

Stores the bookable services an organization offers — the central catalog entry that a booking policy, availability schedule, resources, and bookings themselves all attach to.

## Fields

| Field                                         | Type              | Description                                                  |
| --------------------------------------------- | ----------------- | ------------------------------------------------------------ |
| `id`                                          | `BIGSERIAL`       | Internal surrogate primary key.                              |
| `title`                                       | `VARCHAR(255)`    | Customer-facing service name.                                |
| `slug`                                        | `VARCHAR(255)`    | URL-safe identifier, unique per organization.                |
| `description`                                 | `TEXT`            | Optional customer-facing description.                        |
| `interface_language`                          | `public.locale`   | Language the booking flow is presented in.                   |
| `location`                                    | `TEXT`            | Optional service location.                                   |
| `user_id`                                     | `BIGINT`          | The user who administers this service.                       |
| `profile_id`                                  | `BIGINT`          | The staff profile presented to customers for this service.   |
| `organization_id`                             | `BIGINT`          | The organization that owns this service.                     |
| `schedule_id`                                 | `BIGINT`          | The availability schedule this service books against.        |
| `period_type`                                 | `VARCHAR(50)`     | Booking period model (defaults to `UNLIMITED`).              |
| `timezone`                                    | `VARCHAR(100)`    | Timezone used to interpret this service's availability.      |
| `min_price` / `max_price`                     | `NUMERIC(10,2)`   | Optional customer-facing price range.                        |
| `currency`                                    | `public.currency` | Currency the price range is denominated in.                  |
| `disable_cancelling` / `disable_rescheduling` | `BOOLEAN`         | Whether customers may cancel/reschedule their own bookings.  |
| `success_redirect_url`                        | `TEXT`            | Optional post-booking redirect target.                       |
| `is_instant_service`                          | `BOOLEAN`         | Whether this service skips the standard scheduling flow.     |
| `max_active_booking_per_booker`               | `INTEGER`         | Optional cap on concurrent active bookings per customer.     |
| `destination_calendar_id`                     | `BIGINT`          | Optional external calendar this service's bookings sync to.  |
| `destination_sheets_id`                       | `BIGINT`          | Optional external sheet this service's bookings export to.   |
| `metadata`                                    | `JSONB`           | Free-form, application-defined data.                         |
| `created_at` / `updated_at`                   | `TIMESTAMPTZ`     | Row creation/last-modified timestamps.                       |

## Row Semantics

Each row represents one bookable service offered by an organization — the entity a booking is created against, and the configuration hub every other reservation-domain concept (policy, schedule, resource, destination sync) attaches to.

## Ownership

- **Domain:** Core
- **Module:** Service Management
- **Scope:** Organization.

## Lifecycle

Created automatically at provisioning time as a starter `"Consultant Meeting"` (`slug = consultant-meeting`) bound to the new user/profile/organization/schedule (`WorkspaceProvisioningService.provision()`, repositories written directly since no principal exists yet). Otherwise created by an authorized organization member through the standard CRUD service (`BusinessServiceImpl`). Every FK — `user`, `profile`, `organization`, `schedule`, and the two optional destination-sync FKs — is resolved through a real repository lookup; no bare-reference-entity workaround remains on this table. Full-replace `PUT` updates the whole resource, including clearing a destination-sync link when the request omits it.

## Invariants

- `(organization_id, slug)` is unique.
- `period_type` is a plain `VARCHAR`, not a native Postgres enum, despite reading like one.
- `destination_calendar_id`/`destination_sheets_id` are both nullable and `ON DELETE SET NULL` — losing the connected external destination does not delete the service.
- `requires_confirmation` was dropped (`V142`). It briefly duplicated `core.booking_policies.auto_confirm`; that column is now the single source of truth for whether a booking needs host approval.

## Relationships

- **Organization:** Owns the service.
- **Profile:** The staff identity presented to customers.
- **Schedule:** Determines when the service can be booked.
- **Booking Policy:** `core.booking_policies.service_id` — the rules governing how this service can be booked.
- **Booking:** `core.bookings.service_id` — every booking is made against exactly one service.
- **Resource:** `resource.resources.service_id` — the bookable assets offered under this service.
- **Widget (historical):** `widget.widgets` was originally bound to one `service_id`; that FK was dropped (`V98`), so a widget no longer references a service directly.
- **Destination Calendar / Sheets:** Optional external sync targets for this service's bookings.

## Usage Rules

- Writes go through `BusinessServiceImpl` (`core/service/services/`).
