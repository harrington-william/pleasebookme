# `organization.membership_roles`

> **Variant:** `ASSOCIATION`

## Purpose

Grants a platform RBAC role to a specific organization membership, rather than to the user platform-wide.

## Fields

| Field | Type | Description |
|---|---|---|
| `membership_id` | `BIGINT` | Part of the composite primary key; the membership receiving the role. |
| `role_id` | `BIGINT` | Part of the composite primary key; the role being granted. |
| `assigned_at` | `TIMESTAMPTZ` | When the assignment was created. Immutable, and — despite the name — was renamed from `created_at` (`V75`). |

## Row Semantics

Each row represents one role assignment scoped to one organization membership.

## Ownership

- **Domain:** Organization
- **Module:** Business Identity / Authorization
- **Scope:** Organization → Membership. This is narrower than `auth.user_roles`, which grants a role platform-wide.

## Lifecycle

Created directly via `POST`/removed via `DELETE /api/v1/membership-roles/{membershipId}/{roleId}`. No update — the table has nothing mutable beyond the immutable `assigned_at` timestamp, same shape as `auth.user_roles`.

## Invariants

- A given `(membership_id, role_id)` pair cannot be duplicated — enforced via `existsById` before insert, mandatory for a composite/no-surrogate-PK table.
- Both the referenced membership and role must already exist.
- Deleting the underlying membership or role cascades and removes the assignment.

## Relationships

- **Membership:** The organization membership receiving the role.
- **Role:** The `auth.roles` role being granted — reuses the same role vocabulary as platform-wide RBAC, just scoped to one membership.

## Usage Rules

- Writes go through `MembershipRoleServiceImpl`; grant and revoke are create/delete only.
