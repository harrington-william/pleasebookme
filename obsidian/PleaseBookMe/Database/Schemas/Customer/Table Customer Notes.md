# `customer.customer_notes`

> **Variant:** `ENTITY`

## Purpose

Stores free-text notes staff attach to a customer's CRM record.

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `customer_id` | `BIGINT` | The customer this note is attached to. |
| `author_user_id` | `BIGINT` | Optional user who wrote the note. |
| `content` | `TEXT` | The note's text. |
| `created_at` | `TIMESTAMPTZ` | When the note was written. |

## Row Semantics

Each row represents one note written about one customer.

## Ownership

- **Domain:** Customer
- **Module:** CRM
- **Scope:** Tenant (inherited through the parent customer) → User (author).

## Lifecycle

Created via the standard CRUD service when staff add a note. Updatable in place (this table has real mutable columns — `customer`, `authorUser`, `content` — despite having no `updated_at` column). Deletable directly.

## Invariants

- `content` is required.
- `author_user_id` is nullable with `ON DELETE SET NULL` — a note survives the deletion of the user who wrote it, losing only the attribution.
- No unique constraint beyond the primary key.

## Relationships

- **Customer:** The customer this note concerns.
- **User (author):** The staff member who wrote the note, when known.

## Usage Rules

- Writes go through `CustomerNoteServiceImpl`.
