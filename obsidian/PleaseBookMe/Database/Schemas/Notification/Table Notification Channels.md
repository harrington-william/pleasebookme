# `notification.notification_channels`

> **Variant:** `REFERENCE`

## Purpose

Defines the delivery channels the notification system can send through (e.g. email, SMS) and whether each is currently enabled.

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `code` | `VARCHAR(50)` | Unique short identifier for the channel. |
| `name` | `VARCHAR(100)` | Display name. |
| `enabled` | `BOOLEAN` | Whether this channel is currently active. |
| `created_at` | `TIMESTAMPTZ` | Row creation timestamp. |

## Row Semantics

Each row represents one delivery channel the platform can send notifications through.

## Ownership

- **Domain:** Notification
- **Module:** Delivery
- **Scope:** Platform (global).

## Lifecycle

Seeded/created via the standard CRUD service. `enabled` is expected to gate whether new notifications may be dispatched through this channel, though the enforcement point was not verified in this session. Updatable and deletable, despite having only `created_at` and no `updated_at`.

## Invariants

- `code` is globally unique.

## Relationships

- **Notification:** `notification.notifications.channel_id` — every notification is sent through exactly one channel.

## Usage Rules

- Writes go through `NotificationChannelServiceImpl`.

## Flagged for Follow-up

- Confirm whether `enabled` is actually checked before a notification is dispatched through this channel. Only the CRUD layer was inspected. Same open question as [[Table Notification Preferences]] and [[Table Notifications]] — whether a real dispatch pipeline exists downstream of `notification.notifications`/`notification_queue` was not verified in this session.
