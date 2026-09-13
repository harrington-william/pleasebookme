# `auth.refresh_tokens`

> **Variant:** `STATE`

## Purpose

Tracks issued refresh-token sessions so a bearer token can be verified, rotated, and revoked without the caller re-authenticating with a password.

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `secret` | `TEXT` | The refresh-token bearer value; unique. |
| `owner` | `auth.refresh_owner` | Which kind of actor this session belongs to (`USER`, `WIDGET`, `TENANT`). |
| `user_id` | `BIGINT` | The user this session belongs to. |
| `device_name` | `VARCHAR(255)` | Optional caller-supplied device/client label. |
| `oauth_client_id` | `VARCHAR(255)` | Optional OAuth client identifier associated with the session. |
| `created_at` | `TIMESTAMPTZ` | When the session was issued. |
| `expires_at` | `TIMESTAMPTZ` | When the session stops being valid. |
| `revoked_at` | `TIMESTAMPTZ` | When the session was revoked, if it has been. |

## Row Semantics

Each row represents one issued refresh-token session. It is single-use: a successful refresh rotates it out (marks it revoked) and creates a new row rather than mutating this one in place.

## Ownership

- **Domain:** Auth
- **Module:** Sessions
- **Scope:** User (currently) — `owner` widens this to `WIDGET`/`TENANT` at the schema level, but only `USER`-owned tokens are exercised by any code path today.

## Lifecycle

- Created at login (`AuthServiceImpl.login()`), at registration (`AuthServiceImpl.register()`), and on every successful refresh (`TokenRefresher.refresh()`), each time paired with a freshly issued refresh token as the row's `secret`.
- `TokenRefresher.refresh()` implements rotation-on-use: verifying and consuming a refresh token immediately sets `revoked_at` on the row being used and inserts a brand-new row for the caller's next refresh. A given row is exchanged for new tokens exactly once.
- Also directly creatable/updatable via generic CRUD (`POST`/`PUT /api/v1/refresh-tokens`), which does not perform rotation and can set `revoked_at`/`expires_at` directly.
- Deletion is physical.

## Invariants

- `secret` is globally unique.
- A token is valid only while `revoked_at IS NULL` and `expires_at` is in the future. `DefaultRefreshTokenVerifier` checks both explicitly before allowing a refresh, and distinguishes the two failure cases (`RefreshTokenRevokedException` vs. `RefreshTokenExpiredException`) rather than collapsing them into one error.
- `TokenRefresher` currently rejects any token whose `owner` is not `USER` — the `WIDGET`/`TENANT` enum values exist in the schema ahead of the code paths that would issue or consume them, the same forward-reservation pattern used for not-yet-implemented permission slugs.

## Relationships

- **User:** The user this session belongs to.
- **Widget (historical, no longer present):** this table previously carried a direct `widget_id` foreign key into `widget.widgets` (`V63`); it was dropped in favor of the generic `owner` enum discriminator (`V96`/`V97`/`V99`).

## Usage Rules

- Real session issuance and rotation go through `AuthServiceImpl`/`TokenRefresher`, not the generic CRUD service — the CRUD service performs no rotation or verification and must not be used to simulate a login or refresh.
- A row with `revoked_at` set must never be treated as a valid session, regardless of `expires_at`.

## Security Considerations

`secret` is a live bearer credential and is excluded from `RefreshTokenResponse`, consistent with the platform's standing rule against echoing stored credentials back over the API.

## Concurrency / Idempotency

Rotation-on-use means a given `secret` can only be successfully verified and exchanged once; a second concurrent attempt to refresh the same token finds `revoked_at` already set and is rejected as revoked rather than succeeding twice.
