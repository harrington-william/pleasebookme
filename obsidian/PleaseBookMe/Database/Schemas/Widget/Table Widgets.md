# `widget.widgets`

> **Variant:** `ENTITY`

## Purpose

Issues a credentialed embeddable booking widget for a tenant, distributed to the tenant's own website.

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `uid` | `UUID` | Stable, externally-safe identifier for this widget. |
| `tenant_id` | `BIGINT` | The tenant this widget belongs to. |
| `name` | `VARCHAR(255)` | Caller-assigned label for the widget. |
| `status` | `widget.widget_status` | `REGISTERING` / `ACTIVE` / `DISABLED` / `REVOKED`. |
| `type` | `widget.widget_type` | `INLINE` / `POPUP` / `FULL_PAGE` / `EMBEDDED`. |
| `origin_validation` | `BOOLEAN` | Whether requests are checked against registered origins. |
| `public_key` | `TEXT` | Public, non-secret identifier for the widget; unique. |
| `secret_key` | `TEXT` | BCrypt hash of the widget's private authentication secret. |
| `issued_at` | `TIMESTAMPTZ` | When the credentials were issued. |
| `expires_at` | `TIMESTAMPTZ` | Optional credential expiry; null for dashboard-created widgets. |
| `last_used_at` | `TIMESTAMPTZ` | Optional timestamp of last use. |
| `created_at` / `updated_at` | `TIMESTAMPTZ` | Row creation/last-modified timestamps. |

## Row Semantics

Each row represents one embeddable booking widget credentialed for a tenant.

## Ownership

- **Domain:** Widget
- **Module:** Distribution
- **Scope:** Tenant. A widget is not bound to a single service at the schema level — see Invariants.

## Lifecycle

Dashboard creation derives the tenant from the caller, stores an explicitly `ACTIVE` widget, and optionally creates its normalized origin in the same transaction. `ACTIVE` and `DISABLED` can be selected by update; `DELETE` is the only route to `REVOKED` and is an idempotent soft delete. Credential rotation replaces the public key and BCrypt hash and advances `issued_at`. Widget bootstrap verifies the plaintext secret against this hash before issuing a JWT.

## Invariants

- `public_key` is globally unique.
- `expires_at` is nullable since V136 and is not populated by dashboard create/update.
- `idx_widgets_tenant_status` (V137) serves tenant-scoped lists and status counts.
- This table was originally bound to a single `service_id` (`V47`); that column was dropped (`V98__drop_widgets_service_column.sql`) so a widget is now scoped only to its tenant, not one specific service — a single embedded widget can present multiple services from the same tenant.
- Only a BCrypt hash is stored in `secret_key`; `WidgetResponse` excludes it, and plaintext appears only in the one-time credential-generation response.

## Relationships

- **Tenant:** The tenant this widget is credentialed for.
- **Widget Origin:** `widget.widget_origins` — the domain(s) this widget is permitted to be embedded on, when `origin_validation` is enabled.
- **Refresh Token (historical):** `auth.refresh_tokens` once carried a direct `widget_id` FK; it was replaced by a generic `owner` enum discriminator (see that table's notes).

## Usage Rules

- Parent widget writes and nested-origin upserts go through `WidgetServiceImpl`.
- `secretKey` must never be returned by the API — already enforced by `WidgetResponse`'s field selection.
- Revoked rows remain directly readable but are excluded from paginated lists and `total` stats.

## Security Considerations

The plaintext secret is a bearer credential and must never appear in logs or persistent response DTOs. `origin_validation` plus `widget.widget_origins` is the defense against a leaked `public_key` being replayed from an unauthorized domain — see that table's notes.

## Flagged for Follow-up

- Widget principals still have no scopes/authorities; authorization is deferred to the platform-wide policy pass.
