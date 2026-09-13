# Destination Calendar

## Purpose:

Google Calendar

## Design:

### Fields

- id
- integration_type
- external_id
- user_id
- service_id
- created_at
- updated_at

### Indexes

- user_id
- service_id

---

# Destination Sheets

## Purpose:

Google Sheet

## Design:

### Fields

- id
- integration_type
- external_id
- user_id
- service_id
- created_at
- updated_at

### Indexes

- user_id
- service_id

---

# Integration Type (Enum)

- GOOGLE_CALENDAR
- GOOGLE_SHEET
- OUTLOOK
