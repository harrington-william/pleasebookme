# `integration.destination_calendars`

> **Variant:** `ENTITY`

## Purpose

Binds a service to an external Google Calendar it should sync bookings to.

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `integration_type` | `integration.integration_type` | Which provider integration this is (`GOOGLE_CALENDAR`, historically also `GOOGLE_SHEET`/`OUTLOOK` as enum values). |
| `external_id` | `VARCHAR(255)` | The external calendar's identifier at the provider. |
| `user_id` | `BIGINT` | The user this destination binding belongs to. |
| `service_id` | `BIGINT` | The service whose bookings sync to this calendar. |
| `oauth_connection_id` | `BIGINT` | The delegated-authorization grant used to reach this calendar. |
| `created_at` / `updated_at` | `TIMESTAMPTZ` | Row creation/last-modified timestamps. |

## Row Semantics

Each row represents one binding between a service and an external Google Calendar its bookings should sync to.

## Ownership

- **Domain:** Integration
- **Module:** Calendar Sync
- **Scope:** User → Service.

## Lifecycle

Created when a user connects a service to an external calendar. `oauth_connection_id` was added by a later migration (`V118`) and made `NOT NULL` by a subsequent one (`V125`) — every destination calendar now requires a backing `integration.oauth_connections` row; it did not always.

## Invariants

- No unique constraint beyond the primary key was declared — the schema does not itself prevent a service from having multiple destination calendars.
- `oauth_connection_id` is required, `ON DELETE CASCADE` — deleting the backing OAuth connection removes this destination binding too.

## Relationships

- **User:** The user who owns this binding.
- **Service:** The service whose bookings sync here.
- **OAuth Connection:** The credential used to reach the external calendar.

## Usage Rules

- Writes go through `DestinationCalendarServiceImpl` (`integration/calendar/`).
