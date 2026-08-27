# Tenants

## Purpose:

Tenants

## Design:

### Fields

- id
- uid
- organization_id
- owner_user_id
- ecosystem_id
- name
- slug
- status
- plan_id
- region
- default_timezone | Default = Australia/Sydney
- default_locale | Default = en
- max_users
- max_services
- max_widgets
- settings? JSON
- created_at
- updated_at

### Indexes

- owner_user_id
- organization_id

---

# Tenant Domains

## Purpose:

Store tenant’s website domain

## Design:

### Fields:

- id
- tenant_id
- domain
- verified | Default = false
- is_primary | Default = true
- verification_token
- created_at

---

# Tenant Plans

## Purpose:

Plan

## Design:

### Fields:

- id
- code
- name
- price
- currency
- max_users
- max_services
- max_widgets
- max_resources
- max_api_keys
- features
- metadata

---

# Ecosystems

## Purpose:

Business type boundaries

## Design:

### Fields

- id
- code
- name
- description
- icon
- status | Default = REVIEWING
- metadata
- created_at

---

# Tenant Statuses (Enum)

- ACTIVE
- SUSPENDED
- TRIAL
- PENDING
- ARCHIVED

---

# Tenant Regions (Enum)

- AU
- UK
- US
- SG
- VN

---

# Ecosystem Status (Enum)

- REVIEWING
- ACTIVE
- SUSPENDED
- DISCONTINUED