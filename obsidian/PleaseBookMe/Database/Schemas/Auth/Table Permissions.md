# `auth.permissions`

> **Variant:** `REFERENCE`

## Purpose

Defines the fixed vocabulary of fine-grained, `<RESOURCE>.<ACTION>` permission slugs that the platform's RBAC checks are ultimately expressed against.

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `name` | `VARCHAR(100)` | Human-readable permission name. |
| `description` | `TEXT` | Optional description. |
| `resource` | `VARCHAR(100)` | The resource/entity this permission applies to (e.g. `USER`, `BOOKING`). |
| `action` | `VARCHAR(100)` | The action this permission grants (e.g. `CREATE`, `CANCEL`). |
| `slug` | `VARCHAR(200)` | Unique `<RESOURCE>.<ACTION>` identifier — the value actually checked at authorization time. |
| `created_at` / `updated_at` | `TIMESTAMPTZ` | Row creation/last-modified timestamps. |

## Row Semantics

Each row represents one grantable capability, identified by its `slug`, that can be attached to a role via `auth.role_permissions`.

## Ownership

- **Domain:** Auth
- **Module:** Authorization (RBAC)
- **Scope:** Platform (global).

## Lifecycle

Seeded per bounded context via dedicated migrations (`V79`–`V88`), one migration per domain that already has a real repository/service/controller stack. The seeded set follows a plain CRUD convention (`<RESOURCE>.CREATE/READ/UPDATE/DELETE`, with only `CREATE`/`READ` for the append-only `audit.*` tables), plus a small number of explicitly seeded non-CRUD actions such as `APIKEY.REVOKE`, `BOOKING.CANCEL`/`REJECT`, and `MEMBERSHIPROLE.ASSIGN`/`REVOKE`. Additional permissions can also be created through the standard CRUD service. Deletion is physical and cascades to `auth.role_permissions`, removing the capability from every role that had granted it.

## Invariants

- `slug` is globally unique and is the value the authorization engine actually checks — not `id`.
- Some permission rows are seeded ahead of the feature they gate: `SESSION.READ`/`SESSION.REVOKE` exist even though `auth/session/` has no implementation yet, reserved deliberately so future role assignments referencing them don't require a follow-up migration.
- `resource` values are derived from each entity's class name (minus `Entity`, uppercased) at the time the seed migration was written, not the table name — if an entity is later renamed, its existing `resource`/`slug` values do not update automatically.

## Relationships

- **Role:** A permission is attached to roles through `auth.role_permissions`.

## Usage Rules

- Writes go through `PermissionServiceImpl`; the authoritative baseline is migration-managed (`V79`–`V88`), not expected to be edited ad hoc through the CRUD API during normal operation.
- No dependent-row check is performed before delete beyond the database's own cascade behavior.

## Important Fields

- `slug` — The actual authorization-check key; `resource`/`action` are its decomposed display parts.
