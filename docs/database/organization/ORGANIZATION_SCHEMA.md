# Organizations

## Purpose

Represents businesses

## Design

### Fields

- id
- name
- slug
- logo_url?
- banner_url?
- bio?
- is_private | Default = false
- metadata?
- timezone | Default = Australia/Sydney
- week_start | Default = MONDAY
- created_at
- updated_at

---

# Profiles

## Purpose

Profile

## Design

### Fields

- id
- uid
- user_id
- organization_id
- username (Used for organization/{staff} path on website)
- created_at
- updated_at

### Indexes

- uid
- user_id
- organization_id

### Unique

- user_id, organization_id
- username, organization_id

---

# Memberships

## Purpose

Membership

## Design

### Fields

- id
- organization_id
- user_id
- accepted | Default = false
- created_at
- updated_at

### Indexes

- user_id, organization_id
- organization_id
- user_id
- accepted

---

# Membership Roles

## Purpose

Membership - Role junction table

## Design

### Fields

- membership_id
- role_id
- created_at