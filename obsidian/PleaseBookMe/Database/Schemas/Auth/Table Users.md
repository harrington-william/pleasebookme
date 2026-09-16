# `auth.users`

> **Variant:** `ENTITY`

## Purpose

Stores the platform identity used to authenticate and identify every user across the application — the root identity record that all other user-scoped data (business identity, sessions, credentials, RBAC assignments) is built on top of.

## Fields

| Field            | Type                  | Description                                                                               |
| ---------------- | --------------------- | ----------------------------------------------------------------------------------------- |
| `id`             | `BIGSERIAL`           | Internal surrogate primary key.                                                           |
| `uid`            | `UUID`                | Stable, externally-safe identifier for this user.                                         |
| `username`       | `VARCHAR(100)`        | Unique handle used for username/password login and profile linkage.                       |
| `name`           | `VARCHAR(255)`        | Display name.                                                                             |
| `email`          | `VARCHAR(255)`        | Unique contact address; also the field Google Sign-In account resolution matches against. |
| `phone`          | `VARCHAR(50)`         | Unique contact number; nullable to accommodate Google Sign-In-provisioned accounts.       |
| `bio`            | `TEXT`                | Optional free-text profile description.                                                   |
| `avatar_url`     | `TEXT`                | Optional link to the user's profile image.                                                |
| `locale`         | `public.locale`       | Preferred language/locale for presentation.                                               |
| `timezone`       | `VARCHAR(100)`        | Preferred timezone for presentation.                                                      |
| `theme`          | `public.theme`        | Preferred UI theme.                                                                       |
| `week_start`     | `public.week_start`   | Preferred first day of the week for calendar/schedule display.                            |
| `account_status` | `auth.account_status` | Authentication gate — determines whether the account may currently sign in.               |
| `metadata`       | `JSONB`               | Free-form, application-defined data; not exposed on `UserRequest`/`UserResponse`.         |
| `created_at`     | `TIMESTAMPTZ`         | Row creation timestamp.                                                                   |
| `updated_at`     | `TIMESTAMPTZ`         | Row last-modified timestamp.                                                              |

## Row Semantics

Each row represents one platform-level user identity. It is distinct from business identity (`organization.profiles`) and from tenant subscription (`tenant.tenants`) — a `users` row establishes that an actor exists and can authenticate, not what business it runs or what it may do within one.

## Ownership

- **Domain:** Auth
- **Module:** Identity
- **Scope:** Platform (global). Not tenant- or organization-scoped — a `users` row can attach to multiple organizations via `organization.memberships`, though the current provisioning flow always creates exactly one starter organization per user.

## Lifecycle

- Created through one of two application entry points, both routed through the shared `WorkspaceProvisioningService.provision()`: self-serve registration (`AuthServiceImpl.register()`) and the Google one-shot onboarding flow. Both create the user row atomically alongside a `USER` role assignment, a starter `organization.organizations` row, a self-accepted `organization.memberships` row carrying `ORGANIZATION_OWNER` in `organization.membership_roles`, an `organization.profiles` row, a `FREE` `tenant.tenants` row, a Mon–Fri 9–5 `core.schedules`/`core.availabilities` pair, and a starter `core.services` ("Consultant Meeting") with its `core.booking_policies` row — a `users` row produced by either path never exists in isolation from that starter workspace.
- Also directly creatable via generic CRUD (`POST /api/v1/users`), which persists only the `users` row itself, with none of the accompanying role/organization/membership/profile rows. See Usage Rules — this path does not produce an authenticatable identity under the platform's current identity-loading contract.
- Updated via full-replace `PUT /api/v1/users/{userId}`.
- Deleted via `DELETE /api/v1/users/{userId}`, a physical delete. `ON DELETE CASCADE` foreign keys mean removing a user also removes essentially every row scoped to it across `auth`, `organization`, `core`, and other schemas.
- No soft-delete or deactivation state exists on this table. `account_status` is the closest thing to a lifecycle state, but no application code observed in this session transitions it away from its `ACTIVE` default — suspension/locking workflows are not yet implemented.

## Invariants

- `username`, `email`, and `phone` are each globally unique across the platform.
- `phone` may be `NULL` — required at table creation, relaxed by a later migration specifically to support Google Sign-In, which never supplies a phone number.
- Uniqueness on `username`/`email`/`phone` is checked in the application layer on create (`UserServiceImpl.createUser`); `updateUser` does not re-check them, so changing one of these fields to a value already used by another row is not caught until the database rejects it.
- Per the platform's identity-loading contract, an authenticatable user is expected to resolve to exactly one `organization.memberships` row and one `organization.profiles` row; their absence is treated as a data-integrity error, not a valid state.

## Relationships

- **Role:** A user's platform-wide RBAC roles are assigned through `auth.user_roles`, a many-to-many join into `auth.roles`.
- **Credentials:** A user's password hash lives in a separate one-to-one `auth.user_passwords` row, not on this table.
- **OAuth identity:** `auth.accounts` links this user to third-party sign-in providers (currently Google) by provider account id, independent of any delegated-authorization grant.
- **Business identity:** `organization.profiles` and `organization.memberships` attach this identity to one or more organizations; this table has no direct knowledge of which.
- **Sessions:** `auth.refresh_tokens` records issued sessions owned by this user.
- **API access:** `auth.api_keys.owner_user_id` records which user is accountable for a tenant-scoped programmatic key.

## Usage Rules

- Do not use `POST /api/v1/users` to provision a real, authenticatable account — it bypasses `WorkspaceProvisioningService` and produces a user with no role, organization, membership, or profile, which fails the platform's identity-loading contract. Real account creation goes through registration or Google onboarding.
- Writes must occur through the `auth.user` service layer (`UserServiceImpl`) or the shared orchestration services that wrap it (`WorkspaceProvisioningService`, `AuthServiceImpl`) — no other domain writes to this table directly.
- Deletion is physical and cascades broadly; no dependency check is performed before a delete beyond the database's own foreign-key cascade behavior.

## Important Fields

- `account_status` — Gates authentication directly: Spring Security's `isAccountNonLocked()`/`isEnabled()`/`isAccountNonExpired()` are derived from this field (via `UserPrincipal.isActive()` and an explicit `LOCKED` check), so a non-`ACTIVE` value blocks sign-in at the framework level.
- `uid` — The stable, externally-safe identifier for this user; `id` is the internal surrogate key.
- `phone` — Nullable specifically to accommodate Google Sign-In-provisioned accounts, which never receive a phone number from the identity provider.
- `metadata` — Free-form JSONB, not exposed on `UserRequest`/`UserResponse`.

## Security Considerations

This table holds no credential material itself — no password hash, no token. Those live in `auth.user_passwords` and `auth.refresh_tokens` respectively, kept deliberately separate from identity. `email` and `phone` are personally identifying and are returned as-is in `UserResponse`; no masking is applied at the API layer.
