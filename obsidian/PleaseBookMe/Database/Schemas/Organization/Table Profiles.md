# `organization.profiles`

> **Variant:** `ENTITY`

## Purpose

Stores a user's per-organization staff profile — the identity a user presents within one specific organization, distinct from their platform-wide `auth.users` identity and from their membership acceptance state.

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `uid` | `UUID` | Stable, externally-safe identifier for this profile. |
| `user_id` | `BIGINT` | The platform user this profile belongs to. |
| `organization_id` | `BIGINT` | The organization this profile is scoped to. |
| `username` | `VARCHAR(100)` | Display handle, unique within the organization. |
| `created_at` / `updated_at` | `TIMESTAMPTZ` | Row creation/last-modified timestamps. |

## Row Semantics

Each row represents one user's presence within one organization — a staff identity distinct from both the platform-wide `auth.users` record and the `organization.memberships` acceptance record.

## Ownership

- **Domain:** Organization
- **Module:** Business Identity
- **Scope:** Organization → User.

## Lifecycle

Created automatically alongside a user's starter organization and membership during provisioning (`WorkspaceProvisioningService.provision()`). Also directly creatable via generic CRUD for additional organizations a user joins.

## Invariants

- `(user_id, organization_id)` is unique — a user has at most one profile per organization.
- `(username, organization_id)` is unique — usernames are scoped per organization, not globally (unlike `auth.users.username`, which is global).

## Relationships

- **User:** The platform identity this profile belongs to.
- **Organization:** The organization this profile is scoped to.
- **Service:** `core.services.profile_id` — the staff profile presented to customers for a service.

## Usage Rules

- Writes go through `ProfileServiceImpl` (`organization/profile/`).
