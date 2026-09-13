# `resource.resource_attributes`

> **Variant:** `CONFIGURATION`

## Purpose

Stores arbitrary key/value attributes on a resource, for data that doesn't warrant its own dedicated column.

## Fields

| Field | Type | Description |
|---|---|---|
| `resource_id` | `BIGINT` | Part of the composite primary key; the resource this attribute belongs to. |
| `key` | `VARCHAR(100)` | Part of the composite primary key; the attribute's name. |
| `value` | `TEXT` | The attribute's value. |
| `created_at` / `updated_at` | `TIMESTAMPTZ` | Row creation/last-modified timestamps — added by a later migration (`V74`), not present in the original `CREATE TABLE`. |

## Row Semantics

Each row represents one named attribute (a key/value pair) set on a resource.

## Ownership

- **Domain:** Resource
- **Module:** Resource Management
- **Scope:** Organization (inherited through the parent resource).

## Lifecycle

Created when a custom attribute is set on a resource. Updatable (the `value` can change for a given key) and deletable.

## Invariants

- `(resource_id, key)` is the composite primary key — a resource cannot have the same attribute key set twice; setting it again is an update, not a new row.
- `existsById` is checked before insert — mandatory for this composite/no-surrogate-PK table, since `save()` on a non-null `@Id` would otherwise silently `merge` instead of failing.

## Relationships

- **Resource:** The resource this attribute is set on.

## Usage Rules

- Writes go through `ResourceAttributeServiceImpl` (`resource/attribute/`); reads/updates/deletes take both `resourceId` and `key` (e.g. `/{resourceId}/{key}`).
