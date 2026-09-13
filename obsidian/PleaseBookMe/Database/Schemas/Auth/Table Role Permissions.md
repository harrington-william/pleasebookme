# `auth.role_permissions`

> **Variant:** `ASSOCIATION`

## Purpose

Grants one permission to one role, which is how RBAC's fine-grained capabilities become attached to the coarse-grained roles users are actually assigned.

## Fields

| Field | Type | Description |
|---|---|---|
| `role_id` | `BIGINT` | Part of the composite primary key; the role receiving the capability. |
| `permission_id` | `BIGINT` | Part of the composite primary key; the capability being granted. |

## Row Semantics

Each row represents one permission grant on one role. Unlike `auth.user_roles`, this table has no timestamp columns at all.

## Ownership

- **Domain:** Auth
- **Module:** Authorization (RBAC)
- **Scope:** Platform (global).

## Lifecycle

Bulk-populated by the per-role seed migrations (`V90`–`V95`) — for example `PLATFORM_OWNER` receives every seeded permission via a cross join against `auth.permissions`, while `USER` receives an explicit allow-list of slugs spanning most domains. Individual grants are also directly creatable/removable via `POST`/`DELETE /api/v1/role-permissions/{roleId}/{permissionId}`. There is no update verb — this table has no mutable attribute at all, only the composite key.

## Invariants

- A given `(role_id, permission_id)` pair cannot be duplicated — enforced with an `existsById` check before insert, for the same shared/derived-PK reason as `auth.user_roles`.
- Both the referenced role and permission must already exist.
- Deleting the underlying role or permission cascades and removes the grant.

## Relationships

- **Role:** The role receiving the capability.
- **Permission:** The capability being granted.

## Usage Rules

- Writes go through `RolePermissionServiceImpl`; grant and revoke are create/delete only.
- The seeded baseline (`V79`–`V95`) is the platform's authoritative starting RBAC configuration. Editing this table through the CRUD API changes live authorization behavior immediately for every user currently holding the affected role — there is no staging or review step.
