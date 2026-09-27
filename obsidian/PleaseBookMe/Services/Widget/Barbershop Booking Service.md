# Barbershop Booking Service

## Purpose

`service/widget/barbershop` is the appointment-centric booking flow shared by every channel that offers one. It answers four questions — which organization is this, what can be booked, when is it free, and book it — and it does so without knowing how the caller was identified.

It is named for the ecosystem it serves, not the channel it serves it through. A hotel's stay-centric flow would be a sibling package, not a configuration of this one.

## Boundary

- Reads **no** principal, **no** slug, and **no** request header. It receives a `ServedOrganization` and trusts it.
- Callers: [[Full Page Widget Service]] (`/api/v1/public`, resolved by slug) and [[Embedded Widget Service]] (`/api/v1/widget`, resolved from a widget token).
- Serves: any organization a caller's resolver has already admitted.
- Bookable service: an organization service with a `core.booking_policies` row.

`ServedOrganization(organization, ecosystemCode)` is the whole contract between a channel and this flow. The ecosystem code is copied into the record by the resolver so the flow never navigates a lazy association to read it.

## Operations

| Operation | Effect |
|---|---|
| Get organization | Returns public branding, the ecosystem code, and policy-backed services using one policy `IN` query. |
| Get service | Returns public service/policy data and the sorted union of availability weekdays. |
| Get slots | Delegates unchanged to [[Slot Service]] for the resolved service. |
| Create booking | Locks the host user, regenerates the requested schedule-zone day, then inserts the booking and attendee only if the exact slot is still offered. |

## Booking transaction

The pessimistic host-user lock is taken **before** slot generation. This serializes bookings across all services of one host, matching the slot engine's host-wide conflict rule. The requested `slotStart` is converted to the schedule timezone to choose the engine's date; `slotEnd` is derived from `defaultDuration`. An exact `TimeSlot` value match is mandatory, and a miss is `SlotUnavailableException` (409).

The booking's `title` is `"<service.title> with <attendee.name>"` — see [[Full Page Widget Service]] for why that format rather than MVP v2's. An auto-confirming policy writes `ACCEPTED`; otherwise `AWAITING_HOST`.

No customer row, notification, selected-slot hold, or widget row is created.

## Why the flow is separate from its channels

Before TASK-0012 this code lived inside the hosted page's service, where resolving the organization by slug and running the booking flow were one method. Adding a second channel would have meant a second copy, and the thing that would have drifted is the lock-then-revalidate sequence — the part that is load-bearing and the part nobody would think to re-check.

Adding a third channel now means writing a resolver that produces a `ServedOrganization`. It does not mean touching this package.
