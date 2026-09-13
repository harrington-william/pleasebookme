# Audit Actors

## Purpose

Actor

## Design

### Fields

- id
- actor_type
- user_uid?
- membership_id?
- widget_uid?
- api_key_uid?
- attendee_id?
- system_name?
- display_name
- email?
- ip_address
- user_agent
- created_at

### Indexes

- email
- user_uid
- attendee_id

### Unique

- user_uid
- attendee_id
- email

---

# Audit Events

## Purpose

Audit

## Design

### Fields

- id
- uid
- correlation_id
- request_id
- trace_id
- tenant_id
- organization_id
- actor_id
- event_domain
- event_type
- action
- severity
- status
- context? JSONB
- event_version
- occurred_at
- created_at

### Indexes

- actor_id
- organization_id
- occurred_at
- event_domain
- event_type
- status
- severity
- correlation_id
- trace_id

---

# Audit Resources

## Purpose:

Resource

## Design:

### Fields:

- id
- event_id
- resource_type
- resource_uid
- resource_name
- before_snapshot? JSONB
- after_snapshot JSONB
- created_at

### Indexes

- event_id
- resource_type
- resource_uid

---

# Audit Changes

## Purpose

Audit changes

## Design

### Fields

- id
- resource_id
- field_name
- old_value
- new_value
- occurred_at

### Indexes

- resource_id
- field_name
- occurred_at

---

# Audit Action (Enum)

- CREATED
- UPDATED
- DELETED
- CANCELLED
- ACCEPTED
- REJECTED
- PENDING
- AWAITING_HOST
- RESCHEDULED
- LOCATION_CHANGED
- NO_SHOW_UPDATED
- REGISTERED
- GRANTED
- REVOKED
- LOGIN
- LOGOUT
- SYNCED
- FAILED
- STARTED
- COMPLETED
- EXPIRED

---

# Audit Actor Type (Enum)

- GUEST
- ATTENDEE
- SYSTEM
- WIDGET
- API_KEY
- WEBHOOK
- INTEGRATION

---

# Event Domain (Enum)

- AUTH
- CORE
- WIDGET
- TENANT
- INTEGRATION
- NOTIFICATION
- BILLING
- SYSTEM
- RESOURCE
- ORGANIZATION

---

# Event Type (Enum)

- USER
- ROLE
- PERMISSION
- SERVICE
- RESOURCE
- WIDGET
- MEMBERSHIP
- CALENDAR
- TENANT

---

# Severity (Enum)

- WARNING
- ERROR
- INFO
- SECURITY
- CRITICAL

---

# Audit Status (Enum)

- SUCCESS
- FAILED
- PARTIAL

---

# Resource Type (Enum)

- BOOKING
- SERVICE
- RESOURCE
- USER
- ROLE
- PERMISSION
- TENANT
- WIDGET
- WEBHOOK
- CALENDAR
- PROFILE
- MEMBERSHIP
- NOTIFICATION
- INTEGRATION

---

# Event Source (Enum)

- WEB
- API
- WIDGET
- SYSTEM
- WEBHOOK
- GOOGLE_CALENDAR

---

# Audit Event Context Convention

Follow this pattern:

```json
{

ip,

country,

origin,

browser,

apiVersion,

widgetUid,

endpoint,

httpMethod,

responseStatus,

latency

}
```
