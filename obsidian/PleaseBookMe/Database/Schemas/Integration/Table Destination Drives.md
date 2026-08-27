# `integration.destination_drives`

> **Variant:** `ENTITY`

## Purpose

Binds a service to an external Google Drive location it should back up or store files to.

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `integration_type` | `integration.integration_type` | Which provider integration this is (`GOOGLE_DRIVE`). |
| `external_id` | `VARCHAR(255)` | The external Drive location's identifier at the provider. |
| `user_id` | `BIGINT` | The user this destination binding belongs to. |
| `service_id` | `BIGINT` | The service this Drive destination is associated with. |
| `oauth_connection_id` | `BIGINT` | The delegated-authorization grant used to reach this Drive location. |
| `created_at` / `updated_at` | `TIMESTAMPTZ` | Row creation/last-modified timestamps. |

## Row Semantics

Each row represents one binding between a service and an external Google Drive location. The newest of the three destination tables — added at `V117`, built against `oauth_connections` from the start rather than retrofitted onto it.

## Ownership

- **Domain:** Integration
- **Module:** Drive Sync
- **Scope:** User → Service.

## Lifecycle

Created when a user connects a service to a Drive destination. Unlike `destination_calendars`/`destination_sheets`, `oauth_connection_id` was `NOT NULL` from this table's original migration — it never went through an interim nullable period.

## Invariants

- No unique constraint beyond the primary key.
- `oauth_connection_id` is required, `ON DELETE CASCADE`.
- The platform's `drive.file` scope (not full `drive` access) is what this integration is designed around — full Drive access would trigger Google's annual CASA third-party security assessment.

## Relationships

- **User:** The user who owns this binding.
- **Service:** The service this Drive destination is associated with.
- **OAuth Connection:** The credential used to reach the external Drive location.

## Usage Rules

- Writes go through `DestinationDriveServiceImpl` (`integration/drive/`).
