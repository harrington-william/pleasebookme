# `auth.user_roles`

> **Variant:** `ASSOCIATION`

## Purpose

Grants a platform-wide RBAC role to a user.

## Fields

| Field | Type | Description |
|---|---|---|
| `user_id` | `BIGINT` | Part of the composite primary key; the user receiving the role. |
| `role_id` | `BIGINT` | Part of the composite primary key; the role being assigned. |
| `assigned_at` | `TIMESTAMPTZ` | When the assignment was created. Immutable — this table has no `updated_at`. |

## Row Semantics

Each row represents one active role assignment for one user.

## Ownership

- **Domain:** Auth
- **Module:** Authorization (RBAC)
- **Scope:** Platform (global) — the assignment itself is not tenant- or organization-scoped.

## Lifecycle

Created directly at user-provisioning time: every user produced by `UserProvisioningService.provisionUser()` receives a `USER` role assignment as part of the same atomic workspace-creation flow. Also directly creatable/removable via `POST`/`DELETE /api/v1/user-roles/{userId}/{roleId}`. There is no update — the table has nothing mutable beyond the immutable `assigned_at` timestamp, so revoking a role is a delete, not a state transition.

## Invariants

- A given `(user_id, role_id)` pair cannot be duplicated — `existsById` is checked before insert, which is mandatory for a composite/no-surrogate-PK table since `save()` on a non-null `@Id` would otherwise silently `merge` instead of failing.
- Both the referenced user and role must already exist — resolved through their own repositories, not written blind.
- Deleting the underlying user or role cascades and removes the assignment.

## Relationships

- **User:** Identifies who receives the role.
- **Role:** Identifies which role — and therefore which `auth.role_permissions` grants — applies to this user.

## Usage Rules

- Writes go through `UserRoleServiceImpl`; assignment and revocation are create/delete, not update.
- Authorization decisions must evaluate current assignment rows only — no historical record of a past, now-deleted assignment is retained.
