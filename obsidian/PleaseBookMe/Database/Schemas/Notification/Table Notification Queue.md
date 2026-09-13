# `notification.notification_queue`

> **Variant:** `STATE`

## Purpose

Tracks the dispatch-attempt state of a notification as it moves toward being sent.

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `notification_id` | `BIGINT` | The notification this queue entry tracks. |
| `status` | `notification.notification_status` | Current processing status, defaulting to `QUEUED`. |
| `available_at` | `TIMESTAMPTZ` | When this entry next becomes eligible to be processed. |
| `attempts` | `INTEGER` | How many dispatch attempts have been made. |
| `last_attempt_at` | `TIMESTAMPTZ` | Optional timestamp of the most recent attempt. |
| `created_at` | `TIMESTAMPTZ` | Row creation timestamp. |

## Row Semantics

Each row represents the current dispatch/processing state of one notification as it moves through the send pipeline.

## Ownership

- **Domain:** Notification
- **Module:** Delivery Orchestration
- **Scope:** Tenant (inherited through the parent notification).

## Lifecycle

Created when a notification is queued for dispatch. `status` defaults to `QUEUED` (distinct from `notification.notifications.status`'s own default of `PENDING` — each table's status column defaults independently, not necessarily to the same enum member). `attempts`/`last_attempt_at`/`available_at` support retry-with-backoff. Updatable and deletable directly, despite having only `created_at` and no `updated_at`.

## Invariants

- No unique constraint beyond the primary key — a notification could in principle have more than one queue entry, though the intended cardinality (one active queue entry per notification) is not enforced at the database level.

## Relationships

- **Notification:** The notification this queue entry tracks the dispatch of.

## Usage Rules

- Writes go through `NotificationQueueServiceImpl`.
