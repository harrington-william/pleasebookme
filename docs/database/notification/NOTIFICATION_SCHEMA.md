# Notifications

## Purpose

Notification

## Design

### Fields

- id
- uid
- tenant_id
- organization_id
- recipient_type
- recipient_uid
- template_id
- channel_id
- status
- priority
- subject?
- content?
- locale
- scheduled_at
- sent_at?
- metadata?
- created_at

`recipient_uid` added — `recipient_type` alone doesn't identify who to notify. It's a loose polymorphic reference (no FK), matching the pattern used by `audit.audit_resources.resource_uid`.

### Indexes

- tenant_id
- recipient_uid
- status
- scheduled_at
- created_at

---

# Notification Templates

## Purpose

Stores reusable templates

## Design

### Fields

- id
- tenant_id?
- code
- name
- channel
- subject_template?
- body_template
- locale
- enabled
- version
- created_at
- updated_at

---

# Notification Preferences

## Purpose

User’s notification preferences

## Design

### Fields

- id
- user_id
- notification_type
- email_enabled | Default = false
- sms_enabled | Default = false
- push_enabled | Default = false
- in_app_enabled | Default = false
- quiet_hours_start?
- quiet_hours_end?
- updated_at

---

# Notification Deliveries

## Purpose

Track every delivery attempt

## Design

### Fields

- id
- notification_id
- provider
- provider_message_id?
- status
- attempt
- error_message?
- sent_at
- created_at

---

# Notification Channels

## Purpose

Communicate channels

## Design

### Fields

- id
- code
- name
- enabled | Default = false
- created_at

---

# Notification Queue

## Purpose

Message bus

## Design

### Fields

- id
- notification_id
- status
- available_at
- attempts
- last_attempt_at?
- created_at

---

# Notification Status

- PENDING
- QUEUED
- PROCESSING
- SENT
- FAILED
- CANCELLED
- EXPIRED

---

# Notification Priority

- LOW
- NORMAL
- HIGH
- CRITICAL

---

# Recipient Type

- USER
- ATTENDEE
- CUSTOMER
- SYSTEM
