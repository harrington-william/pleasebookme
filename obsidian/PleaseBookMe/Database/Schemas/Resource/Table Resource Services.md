# `resource.resource_services`

> **Variant:** `JOIN`

## Purpose

Assigns a resource to one or more bookable services without placing a single-service foreign key on the resource itself.

## Fields

| Field | Type | Description |
|---|---|---|
| `resource_id` | `BIGINT` | Assigned resource; part of the composite primary key. |
| `service_id` | `BIGINT` | Assigned service; part of the composite primary key. |
| `assigned_at` | `TIMESTAMPTZ` | Immutable assignment timestamp, defaulting to `now()`. |

## Invariants

- `(resource_id, service_id)` is the primary key; there is no surrogate identifier.
- A resource-service pair can exist at most once.
- Deleting either parent cascades to its assignment rows.
- The row has no mutable business attribute, so the API intentionally omits update.

## Relationships

- **Resource:** References [[Table Resources]].
- **Service:** References [[Table Services]].

## Usage Rules

- Writes go through `ResourceServiceServiceImpl` (`resource/resourceservice/`).
- `GET /api/v1/resource-services?resourceId=` supplies the service identifiers used by the Resources page.
