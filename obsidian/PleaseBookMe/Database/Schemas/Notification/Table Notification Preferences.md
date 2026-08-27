# `notification.notification_preferences`

> **Variant:** `CONFIGURATION`

## Purpose

Records a user's per-notification-type delivery preferences — which channels they want to receive a given kind of notification through, and any quiet hours.

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `user_id` | `BIGINT` | The user these preferences belong to. |
| `notification_type` | `VARCHAR(50)` | The kind of notification this preference row governs. |
| `email_enabled` / `sms_enabled` / `push_enabled` / `in_app_enabled` | `BOOLEAN` | Per-channel opt-in flags. |
| `quiet_hours_start` / `quiet_hours_end` | `TIME` | Optional daily window during which delivery should be suppressed. |
| `updated_at` | `TIMESTAMPTZ` | When this preference was last changed. |

## Row Semantics

Each row represents one user's delivery preferences for one notification type.

## Ownership

- **Domain:** Notification
- **Module:** Preferences
- **Scope:** User.

## Lifecycle

Created when a user's preferences for a notification type are first set; updated in place thereafter. This is the first table in the schema with only an `updated_at` column and no `created_at` at all — there is nothing to timestamp at creation beyond "now," so only the mutation timestamp is tracked.

## Invariants

- `(user_id, notification_type)` is unique — a user has at most one preference row per notification type.
- `notification_type` is a plain `VARCHAR`, not a native enum.

## Relationships

- **User:** The user these preferences belong to.

## Usage Rules

- Writes go through `NotificationPreferenceServiceImpl`; whether the actual notification-dispatch path reads this table before sending was not verified in this session.

## Flagged for Follow-up

- Confirm whether a real dispatch pipeline reads this table before sending a notification, or whether preferences are currently unenforced. Same open question as [[Table Notification Channels]] and [[Table Notifications]].
