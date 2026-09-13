# `customer.customers`

> **Variant:** `ENTITY`

## Purpose

Stores the CRM profile a tenant maintains for a person who books with it — the long-term customer relationship, distinct from any individual booking record.

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `uid` | `UUID` | Stable, externally-safe identifier for this customer. |
| `tenant_id` | `BIGINT` | The tenant this customer belongs to — the CRM isolation boundary. |
| `organization_id` | `BIGINT` | The organization this customer is associated with. |
| `email` | `VARCHAR(255)` | Contact email, unique per tenant. |
| `phone` | `VARCHAR(50)` | Contact phone, unique per tenant. |
| `name` | `VARCHAR(255)` | Customer's display name. |
| `avatar_url` | `TEXT` | Optional avatar image link. |
| `locale` | `public.locale` | Optional preferred language/locale. |
| `timezone` | `VARCHAR(100)` | Optional preferred timezone. |
| `birthday` | `DATE` | Customer's date of birth. |
| `gender` | `VARCHAR(20)` | Optional gender. |
| `status` | `VARCHAR(50)` | Current CRM status of the customer record. |
| `marketing_consent` | `BOOLEAN` | Whether the customer has consented to marketing communication. |
| `notes` | `TEXT` | Optional free-text notes. |
| `metadata` | `JSONB` | Free-form, application-defined data. |
| `created_at` / `updated_at` | `TIMESTAMPTZ` | Row creation/last-modified timestamps. |

## Row Semantics

Each row represents one customer relationship a tenant maintains — a person the tenant does business with, independent of any specific booking.

## Ownership

- **Domain:** Customer
- **Module:** CRM
- **Scope:** Tenant → Organization. Both are recorded directly on the row, but the uniqueness boundary (email/phone) is tenant-level, not organization-level.

## Lifecycle

Created through the standard CRUD service (`CustomerServiceImpl`); no dedicated onboarding/import workflow was located in this session. Updated via full-replace `PUT`. No soft-delete or status-driven deactivation mechanism was found — `status` is a plain string field with no enforced state machine in code.

## Invariants

- `(tenant_id, email)` and `(tenant_id, phone)` are each unique — a tenant cannot have two customer records sharing the same email or phone, but the same email/phone can recur across different tenants.
- `status` is a plain `VARCHAR`, not a native Postgres enum, despite reading like one — valid values are not enforced at the database level.
- `locale`/`timezone` are optional, unlike the same-named columns on `auth.users`/`organization.organizations`, which default to a fixed value — a customer's locale/timezone preference is only recorded when actually known.

## Relationships

- **Tenant:** The CRM isolation boundary this customer belongs to.
- **Organization:** The organization this customer is associated with.
- **Customer Note / Activity / Tag / Source:** Each attaches supplementary CRM data to this customer.
- **Attendee (conceptual, not a foreign key):** `core.attendees` captures the point-in-time booking contact details for a specific reservation; this table is the durable CRM identity the tenant maintains across bookings. The two are intentionally separate concepts.

## Usage Rules

- Writes go through `CustomerServiceImpl`.
- Queries must stay tenant-scoped — this table is the primary tenant-isolation boundary for the entire `customer` schema.

## Security Considerations

`email`, `phone`, `birthday`, and `gender` are personal data. `marketing_consent` should gate any outbound marketing communication built on top of this table.
