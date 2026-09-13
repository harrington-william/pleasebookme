# `notification.notifications`

> **Variant:** `TRANSACTION`

## Purpose

Represents one outbound communication the platform intends to send to a recipient — the central record of the notification subsystem, orchestrating template, channel, and delivery status.

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `uid` | `UUID` | Stable, externally-safe identifier for this notification. |
| `tenant_id` | `BIGINT` | The tenant this notification was sent on behalf of. |
| `organization_id` | `BIGINT` | The organization context. |
| `recipient_type` | `notification.recipient_type` | `USER` / `ATTENDEE` / `CUSTOMER` / `SYSTEM`. |
| `recipient_uid` | `VARCHAR(255)` | The recipient's external identifier — a polymorphic reference, not a foreign key. |
| `template_id` | `BIGINT` | The template used to render this notification. |
| `channel_id` | `BIGINT` | The channel this notification is sent through. |
| `status` | `notification.notification_status` | `PENDING` / `QUEUED` / `PROCESSING` / `SENT` / `FAILED` / `CANCELLED` / `EXPIRED`. |
| `priority` | `notification.notification_priority` | `LOW` / `NORMAL` / `HIGH` / `CRITICAL`. |
| `subject` | `TEXT` | Optional rendered subject. |
| `content` | `TEXT` | Optional rendered body. |
| `locale` | `public.locale` | Language this notification was rendered in. |
| `scheduled_at` | `TIMESTAMPTZ` | When this notification is scheduled to be sent. |
| `sent_at` | `TIMESTAMPTZ` | Optional timestamp it was actually sent. |
| `metadata` | `JSONB` | Free-form, application-defined data. |
| `created_at` / `updated_at` | `TIMESTAMPTZ` | Row creation/last-modified timestamps. |

## Row Semantics

Each row represents one outbound communication the platform intends (or intended) to send to one recipient, along with its current delivery status.

## Ownership

- **Domain:** Notification
- **Module:** Delivery Orchestration
- **Scope:** Tenant → Organization.

## Lifecycle

Created when a notifiable event occurs elsewhere in the platform (no specific trigger code path was verified in this session). Progresses through `status` from `PENDING` toward a terminal state (`SENT`/`FAILED`/`CANCELLED`/`EXPIRED`); `notification.notification_queue` and `notification.notification_deliveries` are the tables that track the actual dispatch attempts behind this status.

## Invariants

- `uid` is generated, never client-supplied.
- `recipient_uid` is a polymorphic external identifier, not a real foreign key — it can point at a `auth.users`, `core.attendees`, or `customer.customers` row (or a system actor) depending on `recipient_type`, without a schema-level dependency on any of them.

## Relationships

- **Tenant / Organization:** The business context this notification was generated within.
- **Template:** The content template used to render it.
- **Channel:** The delivery channel it's sent through.
- **Notification Queue:** `notification.notification_queue.notification_id` — the dispatch-attempt tracking for this notification.
- **Notification Delivery:** `notification.notification_deliveries.notification_id` — the provider-level delivery attempt record(s) for this notification.

## Usage Rules

- Writes go through `NotificationServiceImpl`.

## Important Fields

- `recipient_type`/`recipient_uid` — Together form the polymorphic recipient reference; `recipient_type` determines how `recipient_uid` should be interpreted.

## Flagged for Follow-up

- Confirm what actually creates rows here (which domains call `NotificationServiceImpl.createNotification` on what trigger) and whether a live sender consumes `notification_queue`/writes `notification_deliveries`, or whether the whole pipeline is scaffolding ahead of a feature, same pattern as `integration.sync_jobs`. Related: [[Table Notification Channels]], [[Table Notification Preferences]].
