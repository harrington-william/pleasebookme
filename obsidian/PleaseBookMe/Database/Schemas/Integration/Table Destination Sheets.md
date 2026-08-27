# `integration.destination_sheets`

> **Variant:** `ENTITY`

## Purpose

Binds a service to an external Google Sheet it should export booking data to.

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `integration_type` | `integration.integration_type` | Which provider integration this is. |
| `external_id` | `VARCHAR(255)` | The external sheet's identifier at the provider. |
| `user_id` | `BIGINT` | The user this destination binding belongs to. |
| `service_id` | `BIGINT` | The service whose bookings export to this sheet. |
| `oauth_connection_id` | `BIGINT` | The delegated-authorization grant used to reach this sheet. |
| `created_at` / `updated_at` | `TIMESTAMPTZ` | Row creation/last-modified timestamps. |

## Row Semantics

Each row represents one binding between a service and an external Google Sheet its booking data should export to. Identical column shape to `integration.destination_calendars`, differing only in what the destination represents.

## Ownership

- **Domain:** Integration
- **Module:** Sheets Sync
- **Scope:** User → Service.

## Lifecycle

Created when a user connects a service to an external sheet. `oauth_connection_id` was added and later made required by the same two migrations (`V118`, `V125`) that affected `destination_calendars`.

## Invariants

- No unique constraint beyond the primary key.
- `oauth_connection_id` is required, `ON DELETE CASCADE`.
- **Enum-value naming drift, historically flagged**: the Postgres enum literal is declared `'GOOGLE_SHEET'` (singular) in the checked-in migration files, but the corresponding Java enum constant is `GOOGLE_SHEETS` (plural). The live database was manually altered at some point to rename the label to match the Java code — the migration files were never updated to reflect that manual change. If a `NAMED_ENUM` mapping error ever surfaces here, check whether the live database and the migration files have re-diverged before assuming the Java enum is wrong.

## Relationships

- **User:** The user who owns this binding.
- **Service:** The service whose bookings export here.
- **OAuth Connection:** The credential used to reach the external sheet.

## Usage Rules

- Writes go through `DestinationSheetsServiceImpl` (`integration/sheets/`).
