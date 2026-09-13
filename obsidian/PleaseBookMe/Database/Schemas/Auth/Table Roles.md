# `auth.roles`

> **Variant:** `REFERENCE`

## Purpose

Defines the vocabulary of platform-wide RBAC roles that can be granted to users and that carry a set of permissions.

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `name` | `VARCHAR(100)` | Unique role name — the value the rest of the platform (including seed migrations) actually identifies a role by. |
| `description` | `TEXT` | Optional human-readable description. |
| `created_at` / `updated_at` | `TIMESTAMPTZ` | Row creation/last-modified timestamps. |

## Row Semantics

Each row represents one named role that can be assigned to users (via `auth.user_roles`) and that grants a set of permissions (via `auth.role_permissions`).

## Ownership

- **Domain:** Auth
- **Module:** Authorization (RBAC)
- **Scope:** Platform (global) — role definitions are not tenant- or organization-scoped.

## Lifecycle

Seeded at migration time (`V89__seed_roles.sql`) with six platform roles: `PLATFORM_OWNER`, `PLATFORM_MANAGER`, `USER`, `ORGANIZATION_OWNER`, `ORGANIZATION_MANAGER`, `STAFF`. Additional roles can be created through the standard CRUD service (`POST /api/v1/roles`). Deletion is physical; because `auth.user_roles` and `auth.role_permissions` both cascade-delete on `role_id`, removing a role immediately revokes it from every user holding it and removes every permission grant on it, with no confirmation step.

## Invariants

- `name` is globally unique.
- Deleting a role in active use does not deactivate it — it is gone immediately, and so is every assignment/grant referencing it, via cascade.

## Relationships

- **Permission:** A role's granted capabilities are defined through `auth.role_permissions`, a many-to-many join into `auth.permissions`.
- **User:** A role is granted to users through `auth.user_roles`.

## Usage Rules

- Writes go through `RoleServiceImpl`.
- No dependent-row check is performed before delete beyond the database's own cascade behavior — deleting a role that users currently hold is not blocked or warned against.

## Important Fields

- `name` — The effective business identifier for a role; permission-seeding migrations (`V90`–`V95`) join against it directly (`WHERE r.name = 'PLATFORM_OWNER'`), not against `id`.
