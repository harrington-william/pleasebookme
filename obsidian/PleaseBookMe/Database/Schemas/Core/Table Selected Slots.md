# `core.selected_slots`

> **Variant:** `STATE`

## Purpose

Holds a temporary claim on a specific bookable time slot while a customer completes the checkout flow, preventing the same slot from being booked twice concurrently.

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `uid` | `UUID` | Stable, externally-safe identifier for this hold. |
| `service_id` | `BIGINT` | The service this slot belongs to. |
| `user_id` | `BIGINT` | The user (host) whose availability this slot is drawn from. |
| `slot_start` / `slot_end` | `TIMESTAMPTZ` | The held time window. |
| `release_at` | `TIMESTAMPTZ` | When this hold expires and the slot becomes available again. |
| `is_seat` | `BOOLEAN` | Whether this hold represents one seat within a multi-attendee slot rather than the whole slot. |
| `created_at` | `TIMESTAMPTZ` | When the hold was created. |

## Row Semantics

Each row represents one temporary hold on a specific time slot, claimed by a specific user during an in-progress booking flow, expiring at `release_at` if not converted into a confirmed booking.

## Ownership

- **Domain:** Core
- **Module:** Reservation Lifecycle
- **Scope:** Organization (transitively, via the service) → User (the customer holding the slot).

## Lifecycle

Created when a customer selects a slot to book. Expected to be either converted into a `core.bookings` row before `release_at`, or to expire and free the slot — no cleanup-job/expiry-sweep code path was located in this session, so whether expired holds are actively purged or simply ignored by later queries is **Undetermined**.

## Invariants

- `uid` is generated, never client-supplied.
- `(service_id, user_id, slot_start, slot_end)` is unique — the same user cannot hold the exact same slot twice, which is also the mechanism preventing a duplicate concurrent hold from the same booking attempt.

## Relationships

- **Service:** The service this slot is drawn from.
- **User:** The host whose availability the slot occupies.

## Usage Rules

- Writes go through `SelectedSlotServiceImpl`.

## Concurrency / Idempotency

The composite unique constraint on `(service_id, user_id, slot_start, slot_end)` is the concurrency-control mechanism — a second concurrent attempt to hold the identical slot for the identical user fails at the database level rather than racing. It does **not** by itself prevent two *different* users from holding overlapping-but-not-identical slots; that would depend on availability-computation logic excluding already-held slots, which was not verified in this session.
