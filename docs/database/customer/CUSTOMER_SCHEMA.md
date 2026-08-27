# Customers

## Purpose

Customer

## Design

### Fields

- id
- uid
- tenant_id
- organization_id
- email
- phone
- name
- avatar_url?
- locale?
- timezone?
- birthday
- gender?
- status
- marketing_consent
- notes?
- metadata?
- created_at
- updated_at

### Indexes

- tenant_id
- organization_id
- email
- phone
- name

### Unique

- tenant_id, email
- tenant_id, phone

---

# Customer Activities

## Purpose

Activities

## Design

### Fields

- id
- customer_id
- activity_type
- reference_type
- reference_uid
- description?
- metadata
- occurred_at

---

# Customer Notes

## Purpose

Notes

## Design

### Fields:

- customer_id
- author_user_id
- content
- created_at

---

# Customer Tags

## Purpose

Tagging

## Design

### Fields:

- id
- customer_id
- tag

### Examples

- VIP
- LOYAL
- NEW
- HIGH_VALUE

---

# Customer Sources

## Purpose

Used for marketing

## Design

### Fields

- id
- source
- customer_id

### Examples

- DIRECT
- WEBSITE
- WIDGET
- GOOGLE
- FACEBOOK
- INSTAGRAM
- REFERAL
- API
