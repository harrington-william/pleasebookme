# `integration.oauth_connections`

> **Variant:** `STATE`

## Purpose

Stores a user's delegated-authorization grant to a third-party provider (currently Google) — the live, encrypted credential that lets the platform act on the user's behalf against Calendar, Sheets, and Drive. This is the sole live-credential store for delegated authorization on the platform.

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `uid` | `UUID` | Stable, externally-safe identifier for this connection. |
| `user_id` | `BIGINT` | The user who granted this authorization. |
| `provider` | `integration.oauth_provider` | The external provider (`GOOGLE`). |
| `provider_account_id` | `VARCHAR(255)` | The provider's identifier for the granting account. |
| `provider_email` | `VARCHAR(255)` | Optional email reported by the provider. |
| `scopes` | `TEXT[]` | The Google API scopes actually granted — raw provider URI strings, not enum names. |
| `access_token` | `TEXT` | Encrypted (AES-256-GCM) short-lived access token. |
| `refresh_token` | `TEXT` | Encrypted long-lived refresh token. |
| `token_key_version` | `SMALLINT` | Which encryption key version encrypted the two token columns. |
| `token_expires_at` | `TIMESTAMPTZ` | When the current access token expires. |
| `status` | `integration.oauth_connection_status` | `ACTIVE` / `REVOKED` / `EXPIRED` / `ERROR`. |
| `connected_at` | `TIMESTAMPTZ` | When the connection was first established. |
| `last_refreshed_at` | `TIMESTAMPTZ` | Optional timestamp of the last successful token refresh. |
| `last_used_at` | `TIMESTAMPTZ` | Optional timestamp of last use. |
| `revoked_at` | `TIMESTAMPTZ` | Optional timestamp the connection was revoked. |
| `created_at` / `updated_at` | `TIMESTAMPTZ` | Row creation/last-modified timestamps. |

## Row Semantics

Each row represents one delegated-authorization grant a user has made to a third-party provider account, with its current live credential and status.

## Ownership

- **Domain:** Integration
- **Module:** Delegated Authorization
- **Scope:** User.

## Lifecycle

Written exclusively by the OAuth consent flow (`DefaultGoogleConnectService`/`GoogleConnectionWriter`, and the shared onboarding path in `GoogleOnboardingService`) — never by a client-supplied request body. On a brand-new connection, if Google's response includes no `refresh_token`, the row is skipped rather than written (the column is `NOT NULL` with nothing to fall back on, and writing would abort a transaction that may also be provisioning the user's account). On a re-consent, a missing `refresh_token` is tolerated — the previously stored one is kept. `status` moves to `ACTIVE` unconditionally on write (even a narrowed-scope grant is still `ACTIVE`); it moves to `REVOKED` only through the disconnect flow, which revokes at Google first and marks the row `REVOKED` second — never the local row alone.

## Invariants

- `(user_id, provider, provider_account_id)` is unique — one connection per user per external account.
- `refresh_token` is `NOT NULL`; see Lifecycle for how a missing one from Google is handled without violating this.
- `status = ACTIVE` does **not** imply Calendar/Sheets/Drive access was granted — the user can untick individual scopes during consent. Any "is this connected?" decision must read the `scopes` array, never `status` alone.
- Google's granted scopes come back as a space-delimited string and are merged with any previously granted scopes on write, so a narrower re-consent cannot silently revoke a previously granted capability.

## Relationships

- **User:** The user who granted this authorization.
- **Destination Calendar / Sheets / Drive:** `integration.destination_calendars`/`destination_sheets`/`destination_drives.oauth_connection_id` all FK into this table — a sync destination cannot exist without a backing connection.
- **Sync Job:** `integration.sync_jobs.oauth_connection_id` — the credential a queued sync job will use.
- **Account (conceptual, not a foreign key):** `auth.accounts` records the same external identity's *sign-in* linkage; this table records the same identity's *delegated-authorization* grant. The two are deliberately not joined at the schema level — see that table's notes.

## Usage Rules

- **Deliberately not a full CRUD surface.** `POST`, `PUT`, and list-all were removed from `/api/v1/oauth-connections` along with the request DTO that would have carried client-suppliable tokens — writing this table any other way would let a caller inject forged credentials. Only an owner-scoped `GET /{id}` and `DELETE /{id}` (disconnect) remain.
- All token reads must go through `GoogleAccessTokenProvider`, the single component permitted to return a decrypted token — it handles the expiry/refresh/revoke lifecycle in one place.
- `disconnect` returns the same error for "not yours" and "doesn't exist," so the endpoint cannot be used to probe which connection UIDs are real.

## Security Considerations

`access_token`/`refresh_token` are ciphertext (AES-256-GCM, fresh IV per encryption); `token_key_version` is what makes key rotation a configuration change rather than a re-encryption sweep. Neither column is ever returned by the API. A Google refresh token is effectively a long-lived password to the user's calendar and files, so this table is one of the most security-sensitive in the schema.
