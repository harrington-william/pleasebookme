# Widgets

## Purpose:

Widget

## Design:

### Fields

- id
- uid
- tenant_id
- service_id
- name | Format = Tenant name + “Widget”
- status | Default = REGISTERING
- type | Default = EMBEDDED
- origin_validation
- public_key
- secret_key
- issued_at
- expires_at
- last_used_at
- created_at
- updated_at

### Indexes

- tenant_id
- issued_at

---

# Widget Origins

## Purpose:

Widget to tenant’s website

## Design:

### Fields:

- id
- widget_id
- origin
- verified | Default = false
- created_by
- created_at

---

# Widget Types (Enum)

- INLINE
- POPUP
- FULL_PAGE
- EMBEDDED

---

# Widget Status (Enum)

- REGISTERING
- ACTIVE
- DISABLED
- REVOKED