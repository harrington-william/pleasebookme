# Resources

## Purpose:

Represents a reservable resource

## Design:

### Fields

- id
- uid
- organization_id
- service_id
- resource_type_id
- name
- slug
- description
- capacity
- status
- is_bookable | Default = true
- is_virtual | Default = false
- metadata
- created_at
- updated_at

---

# Resource Types

## Purpose:

Resource Type

## Design:

### Fields:

- id
- organization_id
- name
- description?
- icon
- metadata

---

# Resource Pricing

## Purpose:

Pricing

## Design:

### Fields

- id
- resource_id
- price
- currency
- effective_from
- effective_until

---

# Resource Assignments

## Purpose:

Joining table Resource → Membership

## Design:

### Fields:

- resource_id
- membership_id
- assigned_at
- released_at
- assigned_by
- released_by
- is_primary | Default = true

---

# Resource Calendars

## Purpose:

1 Resource multiple Schedules

## Design:

### Fields:

- id
- resource_id
- schedule_id

---

# Resource Maintenance

## Purpose:

Maintenance

## Design:

### Fields:

- id
- resource_id
- start_time
- end_time
- reason
- status
- created_at

---

# Resource Attributes

## Purpose:

Attributes

## Design

### Fields:

- id
- resource_id
- key
- value

---

# Resource Availability Overrides

## Purpose:

Overrides schedule

## Design:

### Fields:

- id
- resource_id
- start_time
- end_time
- reason

---

# Resource Dependencies

## Purpose:

Dependencies

## Design:

### Fields:

---