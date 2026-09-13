# `notification.notification_deliveries`

> **Variant:** `EVENT`

## Purpose

Records the outcome of one provider-level delivery attempt for a notification — the audit trail of what actually happened when the platform tried to send it.

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `notification_id` | `BIGINT` | The notification this delivery attempt belongs to. |
| `provider` | `VARCHAR(100)` | The delivery provider used (e.g. an email/SMS gateway). |
| `provider_message_id` | `VARCHAR(255)` | Optional identifier the provider assigned to this attempt. |
| `status` | `notification.notification_status` | Outcome of this specific delivery attempt. |
| `attempt` | `INTEGER` | Which attempt number this is. |
| `error_message` | `TEXT` | Optional error detail if the attempt failed. |
| `sent_at` | `TIMESTAMPTZ` | Optional timestamp the attempt succeeded. |
| `created_at` | `TIMESTAMPTZ` | Row creation timestamp. |

## Row Semantics

Each row represents one provider-level delivery attempt for a notification — a record of what happened when the platform tried to actually send it through a specific provider.

## Ownership

- **Domain:** Notification
- **Module:** Delivery Orchestration
- **Scope:** Tenant (inherited through the parent notification).

## Lifecycle

Created once per delivery attempt against an external provider. Unlike `notification.notifications`/`notification.notification_queue`, `status` here has **no SQL default** — it must be supplied explicitly, since a delivery-attempt record only makes sense once an outcome is known. Updatable and deletable directly, despite having only `created_at`.

## Invariants

- No unique constraint beyond the primary key — multiple attempts per notification are expected and tracked via `attempt`, not deduplicated.
- `status` is required with no default, unlike the same enum's use elsewhere in this schema.

## Relationships

- **Notification:** The notification this delivery attempt belongs to.

## Usage Rules

- Writes go through `NotificationDeliveryServiceImpl`.
