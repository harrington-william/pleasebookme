# `notification.notification_templates`

> **Variant:** `CONFIGURATION`

## Purpose

Defines the content template used to render a notification's subject/body for a given channel and locale, either globally or overridden per tenant.

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `tenant_id` | `BIGINT` | Optional tenant override; `NULL` means a platform-default template. |
| `code` | `VARCHAR(100)` | Identifier for the template, unique per tenant. |
| `name` | `VARCHAR(255)` | Display name. |
| `channel` | `VARCHAR(50)` | Which delivery channel this template renders for. |
| `subject_template` | `TEXT` | Optional subject-line template. |
| `body_template` | `TEXT` | Body template. |
| `locale` | `public.locale` | Language this template is written in. |
| `enabled` | `BOOLEAN` | Whether this template is currently usable. |
| `version` | `INTEGER` | Template version number. |
| `created_at` / `updated_at` | `TIMESTAMPTZ` | Row creation/last-modified timestamps. |

## Row Semantics

Each row represents one versioned notification content template for a specific `code`/`channel`/`locale` combination, either platform-default (`tenant_id IS NULL`) or tenant-specific.

## Ownership

- **Domain:** Notification
- **Module:** Content / Delivery
- **Scope:** Platform (when `tenant_id` is null) or Tenant.

## Lifecycle

Created via the standard CRUD service. `enabled`/`version` both have SQL defaults but no enforced version-increment behavior was located in this session — `version` appears to be caller-managed rather than auto-incremented on template edits.

## Invariants

- `(tenant_id, code)` is unique — Postgres composite-unique `NULL` semantics apply literally here (two platform-default templates with the same `code` and both `NULL` `tenant_id` would not collide at the database level, a nuance the platform's code does not specially guard against).
- `channel` is a plain `VARCHAR`, not a native enum, despite reading like one.

## Relationships

- **Tenant:** Optional — a tenant-specific override of a template.
- **Notification:** `notification.notifications.template_id` — every notification is rendered from exactly one template.

## Usage Rules

- Writes go through `NotificationTemplateServiceImpl`.
