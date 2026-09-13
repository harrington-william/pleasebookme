# `resource.resource_pricing`

> **Variant:** `ENTITY`

## Purpose

Records a price for a resource, valid over a specific time window — allowing a resource's price to change over time without losing the historical price that applied to past bookings.

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `resource_id` | `BIGINT` | The resource this price applies to. |
| `price` | `NUMERIC(10,2)` | The price. |
| `currency` | `public.currency` | Currency the price is denominated in. |
| `effective_from` | `TIMESTAMPTZ` | When this price becomes effective. |
| `effective_until` | `TIMESTAMPTZ` | Optional end of this price's validity window. |
| `created_at` / `updated_at` | `TIMESTAMPTZ` | Row creation/last-modified timestamps. |

## Row Semantics

Each row represents one price that applied (or applies) to a resource over a specific time window.

## Ownership

- **Domain:** Resource
- **Module:** Resource Management
- **Scope:** Organization (inherited through the parent resource).

## Lifecycle

Created whenever a new price is set for a resource; an open-ended `effective_until` (`NULL`) implies the price is currently active. No overlap-prevention constraint was found — the schema does not itself stop two overlapping pricing windows from being created for the same resource.

## Invariants

- `effective_from` is required; `effective_until` is optional.
- No unique constraint beyond the primary key.

## Relationships

- **Resource:** The resource this price applies to.

## Usage Rules

- Writes go through `ResourcePricingServiceImpl`.
