# `resource.resource_types`

> **Variant:** `REFERENCE`

## Purpose

Defines the categories of bookable asset an organization uses to classify its resources (e.g. "barber chair," "room," "vehicle").

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `organization_id` | `BIGINT` | The organization this resource type belongs to. |
| `name` | `VARCHAR(100)` | Display name. |
| `description` | `TEXT` | Optional description. |
| `icon` | `VARCHAR(100)` | Optional display icon. |
| `metadata` | `JSONB` | Free-form, application-defined data. |
| `created_at` / `updated_at` | `TIMESTAMPTZ` | Row creation/last-modified timestamps. |

## Row Semantics

Each row represents one category of bookable asset an organization has defined.

## Ownership

- **Domain:** Resource
- **Module:** Resource Management
- **Scope:** Organization.

## Lifecycle

Created through the standard CRUD service by an organization admin, before any resources of that type can be created.

## Invariants

- `(organization_id, name)` is unique at the database level.
- The CRUD service does not yet perform a pre-flush duplicate check, so this constraint currently surfaces as a database error.

## Relationships

- **Organization:** The organization this type belongs to.
- **Resource:** `resource.resources.resource_type_id` classifies each resource against one type.

## Usage Rules

- Writes go through `ResourceTypeServiceImpl`.
