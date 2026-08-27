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
| `secret_key` | `TEXT` | The widget's private signing/auth secret. |
| `issued_at` | `TIMESTAMPTZ` | When the credentials were issued. |
| `expires_at` | `TIMESTAMPTZ` | When the credentials expire. |
| `last_used_at` | `TIMESTAMPTZ` | Optional timestamp of last use. |
| `created_at` / `updated_at` | `TIMESTAMPTZ` | Row creation/last-modified timestamps. |

## Row Semantics

Each row represents one embeddable booking widget credentialed for a tenant.

## Ownership

- **Domain:** Widget
- **Module:** Distribution
- **Scope:** Tenant. A widget is not bound to a single service at the schema level — see Invariants.

## Lifecycle

Created via `WidgetServiceImpl`, resolving `tenant_id` through the real `TenantRepository`. `status` progresses conceptually from `REGISTERING` through `ACTIVE` to `DISABLED`/`REVOKED`, though no dedicated status-transition endpoint was located in this session — a status change currently relies on full-replace `PUT`. Widget credential *verification* has its own live endpoint, `POST /api/v1/auth/widget/bootstrap` (`AuthController.bootstrapWidget` → `WidgetIdentityLoader.loadByPublicKey`), which exchanges `public_key`/`secret_key` plus an `Origin` header for a JWT — this contradicts `SECURITY.md` §10's "Not Yet Implemented" note that this flow doesn't exist; the code has evolved past that doc.

## Invariants

- `public_key` is globally unique.
- This table was originally bound to a single `service_id` (`V47`); that column was dropped (`V98__drop_widgets_service_column.sql`) so a widget is now scoped only to its tenant, not one specific service — a single embedded widget can present multiple services from the same tenant.
- `secret_key` is a live bearer credential — `WidgetResponse` excludes it, consistent with the platform's standing rule against echoing stored credentials.

## Relationships

- **Tenant:** The tenant this widget is credentialed for.
- **Widget Origin:** `widget.widget_origins` — the domain(s) this widget is permitted to be embedded on, when `origin_validation` is enabled.
- **Refresh Token (historical):** `auth.refresh_tokens` once carried a direct `widget_id` FK; it was replaced by a generic `owner` enum discriminator (see that table's notes).

## Usage Rules

- Writes go through `WidgetServiceImpl`.
- `secretKey` must never be returned by the API — already enforced by `WidgetResponse`'s field selection.

## Security Considerations

`secret_key` is a bearer credential and must never appear in logs or documentation. `origin_validation` plus `widget.widget_origins` is the defense against a leaked `public_key` being replayed from an unauthorized domain — see that table's notes.

## Flagged for Follow-up

- `SECURITY.md` §10 ("Not Yet Implemented") should be updated: it states the widget bootstrap flow doesn't exist yet, but `POST /api/v1/auth/widget/bootstrap` is fully implemented (`AuthController.bootstrapWidget` → `WidgetIdentityLoader.loadByPublicKey`). Worth also confirming whether `WidgetPrincipal`'s scopes/authority gap (also noted in that same section) is still accurate.
- No status-transition endpoint (`REGISTERING` → `ACTIVE` → `DISABLED`/`REVOKED`) was located beyond full-replace `PUT` — confirm whether that's intentional or a gap.
