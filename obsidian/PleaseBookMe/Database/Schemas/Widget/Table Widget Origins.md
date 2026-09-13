# `widget.widget_origins`

> **Variant:** `ASSOCIATION`

## Purpose

Registers the website domain(s) a widget is permitted to be embedded on, defending a leaked `public_key` against replay from an unauthorized origin.

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `widget_id` | `BIGINT` | The widget this origin is registered for. |
| `origin` | `VARCHAR(255)` | Browser-origin form: lowercase `scheme://host[:non-default-port]`. |
| `verified` | `BOOLEAN` | Whether the origin has been verified. |
| `created_by` | `BIGINT` | Optional user who registered this origin. |
| `created_at` | `TIMESTAMPTZ` | When the origin was registered. |

## Row Semantics

Each row represents one domain a specific widget is registered to be embedded on.

## Ownership

- **Domain:** Widget
- **Module:** Distribution / Security
- **Scope:** Widget (and transitively, Tenant).

## Lifecycle

Dashboard widget create/update normalizes and upserts one effective origin in the parent widget transaction. A null/blank origin removes all rows for that widget and disables origin validation. Soft-deleting the parent keeps origin rows as the historical allow-list. The generic widget-origin CRUD endpoints remain available separately.

## Invariants

- `(widget_id, origin)` is unique — the same origin cannot be registered twice for the same widget.
- `created_by` is nullable with `ON DELETE SET NULL`.
- Dashboard-managed values omit paths, queries, fragments, and default ports because browser `Origin` headers never contain them.

## Relationships

- **Widget:** The widget this origin applies to.
- **User (creator):** The user who registered the origin, when known.

## Usage Rules

- Writes go through `WidgetOriginServiceImpl`.
- This is the data `WidgetIdentityLoader.loadByPublicKey` checks the request's `Origin` header against, when the parent widget has `origin_validation` enabled — a mismatch throws `WidgetOriginMismatchException` before a `WidgetPrincipal` is ever constructed.

## Security Considerations

Registering an origin is a security-relevant action — browsers set the `Origin` header on outbound requests in a way page JavaScript cannot override, so this table is what makes origin validation a meaningful check against unauthorized cross-origin use of a leaked `public_key`.
