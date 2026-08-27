# `organization.memberships`

> **Variant:** `ASSOCIATION`

## Purpose

Tracks whether a user belongs to an organization and whether that membership has been accepted.

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `organization_id` | `BIGINT` | The organization being joined. |
| `user_id` | `BIGINT` | The user joining. |
| `accepted` | `BOOLEAN` | Whether the membership has been accepted. |
| `created_at` / `updated_at` | `TIMESTAMPTZ` | Row creation/last-modified timestamps. |

## Row Semantics

Each row represents one user's membership in one organization, and whether that membership is currently accepted.

## Ownership

- **Domain:** Organization
- **Module:** Business Identity
- **Scope:** Organization → User.

## Lifecycle

Created automatically, pre-accepted (`accepted = true`), as part of user provisioning — a user's own starter organization needs no invite/acceptance step since they're joining a workspace they just created. For any other organization, a membership can also be created via generic CRUD with `accepted` defaulting to `false`, implying an invite-and-accept flow, though no dedicated invite/accept endpoint beyond full-replace `PUT` was located in this session.

## Invariants

- `(user_id, organization_id)` is unique — a user has at most one membership per organization.

## Relationships

- **Organization:** The organization being joined.
- **User:** The user joining.
- **Membership Role:** `organization.membership_roles` attaches RBAC roles to a specific membership.
- **Resource Assignment:** `resource.resource_assignments.membership_id` — resources are assigned to a membership, not directly to a user, so assignment is organization-scoped.

## Usage Rules

- Writes go through `MembershipServiceImpl`.
