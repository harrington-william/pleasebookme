# Users

## Purpose:

Manages users

## Design:

### Fields

- id
- uid
- username
- name
- email
- phone
- bio?
- avatar_url?
- locale | Default = en
- timezone? | Default = Australia/Sydney
- theme | Default = DARK
- week_start | Default = MONDAY
- account_status | Default = ACTIVE
- metadata?
- created_at
- updated_at

### Indexes

- username
- email
- phone

### Unique

- username
- email
- phone

---

# User Passwords

## Purpose

Separate passwords from users

## Design

### Fields

- user_id
- raw
- hash
- created_at
- updated_at

`raw` intentionally stores the plaintext password. Platform V1.0.0 is
privately onboarded — every account's password is set by the operator, not
chosen by the account holder through self-service. This column is removed
once the platform opens up to self-service SaaS signup.

### Indexes

user_id

---

# Roles

## Purpose

Roles

## Design

### Fields

- id
- name
- description?
- created_at
- updated_at

---

# Permissions

## Purpose

Permissions

## Design

### Fields

- id
- name
- description?
- resource
- action
- slug
- created_at
- updated_at

---

# User Roles

## Purpose

Joining table

## Design

### Fields

- user_id
- role_id
- assigned_at

---

# Role Permissions

## Purpose

Joining table

## Design

### Fields

- role_id
- permission_id

---

# Attributes

## Purpose

Attributes

## Design

### Fields

- id
- session_token
- user_id
- expires

**Excluded from Platform V1.0.0**, same as Sessions above — this section
is a byte-for-byte duplicate of Sessions with no distinct documented
purpose, kept here for whenever it's fleshed out into something real.

---

# Accounts

## Purpose

OAuth2

## Design

### Fields

- id
- user_id
- type
- provider
- provider_account_id
- provider_email?
- access_token?
- refresh_token?
- expires_at?
- token_type?
- scope?
- id_token?

### Indexes

- user_id
- type

### Uniques

- provider, provider_account_id

---

# API Keys

## Purpose

API Keys

## Design

### Fields

- id
- uid
- tenant_id
- owner_user_id
- name
- description?
- public_key
- secret_hash
- status
- last_used_at?
- expires_at?
- revoked_at?
- created_at
- updated_at

### Indexes

- tenant_id
- owner_user_id
- public_key
- status
- expires_at

### Unique

- uid
- public_key

Migration is `V34__auth_api_keys.sql`, placed in Phase 4 (right after
Tenant), not Phase 2 — `tenant_id` references `tenant.tenants`.

---

# Refresh Tokens

## Purpose

Refresh token

## Design

### Fields

- id
- secret
- owner?
- user_id
- widget_id?
- client?
- oauth_client_id?
- created_at
- expires_at
- revoked_at?

`widget_id`'s FK to `widget.widgets` is added via `V72__auth_add_refresh_token_widget_fk.sql`,
once Widget exists (Phase 8). `oauth_client_id` is still unresolved — no
`oauth_clients` table exists anywhere in the project.

`widget_id` is nullable with no FK yet — `widget.widgets` doesn't exist
until Phase 8. `oauth_client_id` has no backing table anywhere in the
project; it's an orphaned column pending a real OAuth client entity. Both
are unresolved design questions, not yet dropped.

---

# Sessions

## Purpose

Session

## Design

### Fields

- id
- session_token
- user_id
- expires

**Excluded from Platform V1.0.0.** No migration has been written for this
table — session-based auth isn't used (the platform is JWT-based, see
`security.jwt` in `application.yaml`). Kept documented for whenever a
session-backed flow is actually needed.

---

# Role Types (Enum)

- PLATFORM_OWNER
- PLATFORM_MANAGER
- TENANT
- ORGANIZATION_OWNER
- ORGANIZATION_MANAGER
- STAFF

---

# Account Status (Enum)

- ACTIVE
- SUSPENDED
- LOCKED

---

# API Key Status (Enum)

- ACTIVE
- REVOKED
- EXPIRED