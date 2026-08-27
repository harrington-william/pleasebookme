# `auth.user_passwords`

> **Variant:** `ENTITY`

## Purpose

Stores the hashed password credential used to authenticate a user via username/password login, kept as a separate table from `auth.users` so credential material is not commingled with identity data.

## Fields

| Field | Type | Description |
|---|---|---|
| `user_id` | `BIGINT` | Primary key and foreign key at once — this table has no surrogate `id`; the owning user's id doubles as this row's identity. |
| `hash` | `TEXT` | The password credential value persisted for this user. |
| `created_at` | `TIMESTAMPTZ` | Row creation timestamp. |
| `updated_at` | `TIMESTAMPTZ` | Row last-modified timestamp. |

## Row Semantics

Each row represents the current password credential for exactly one user. Because `user_id` is both the primary key and the foreign key, at most one password row can exist per user at any time.

## Ownership

- **Domain:** Auth
- **Module:** Credentials
- **Scope:** User

## Lifecycle

- Created during self-serve registration (`AuthServiceImpl.register()`), where the incoming password is passed through the platform `PasswordEncoder` before being persisted.
- Also directly creatable/updatable via generic CRUD (`POST`/`PUT /api/v1/passwords`) — see Security Considerations for how this path differs from registration.
- `updateUserPassword` rejects a request whose value is identical to the currently stored value, throwing the same `DuplicateUserPasswordException` used for create-time duplicates.
- Deletion is physical, one row at a time.
- No bulk-list endpoint exists on this table by design — listing every user's credential row has no legitimate operational use.

## Invariants

- At most one `user_passwords` row exists per user (shared primary key).
- `createUserPassword` checks `existsById` before insert — required for a shared-PK entity, since `save()` on a non-null `@Id` would otherwise silently `merge` (overwrite) an existing row instead of failing.
- The table previously also carried a plaintext `raw` column (intentional during the platform's private-onboarding phase); it was dropped by migration `V114__auth_user_passwords_drop_raw.sql` and no longer exists in the current schema — only `hash` remains.

## Relationships

- **User:** A one-to-one, shared-primary-key relationship with `auth.users` — this row's identity is derived entirely from the user it belongs to.

## Usage Rules

- Real credential creation for registration goes through `AuthServiceImpl.register()`, which hashes the value before persisting.
- There is no `getAll` endpoint on this table — only per-user lookup (`GET /{userId}`) is supported.

## Security Considerations

`AuthServiceImpl.register()` calls `passwordEncoder.encode()` before writing `hash`, producing a real password hash. The generic CRUD path (`UserPasswordServiceImpl.createUserPassword`/`updateUserPassword`, backing `POST`/`PUT /api/v1/passwords`) does **not** perform this step — it persists the request's `password` field directly into `hash` with no encoding applied. This path should be treated as trusted-input-only (a caller responsible for pre-hashing its own value), not as a public self-service credential-set endpoint; using it with a plaintext value would persist that plaintext as the stored "hash." `PasswordResponse` excludes the credential value from the API response regardless of which path wrote it.
