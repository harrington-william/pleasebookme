# Slot Service

## Purpose

The Slot service computes the bookable time slots a customer may choose for one service on one calendar date, as of the moment the question is asked.

It is the read side of the reservation engine: it turns a service's recurring availability windows, its booking policy, and everything already occupying the host's time into a concrete list of `[start, end)` intervals. It exists so that a widget or dashboard never has to reconstruct that arithmetic — and so that two services owned by the same host can never be offered at the same time, which was possible in MVP v2 where conflicts were scoped to a single service.

The service was ported from MVP v2 under [TASK-0009](../../../../.agents/tasks/active/TASK-0009-slot-generation-engine.md).

## Responsibilities

- Resolve the service, its booking policy, its schedule's timezone, and its host.
- Select the availability windows that apply on the requested weekday.
- Generate candidate slots from each window at the policy's duration and interval.
- Discard candidates outside the policy's booking window (minimum notice, maximum advance).
- Discard candidates that collide with a blocking booking, a live hold, or an out-of-office period of the host.
- Return the survivors, sorted and de-duplicated, with the timezone they were computed in.

## Does Not Own

- Holding a slot, booking it, or releasing a hold — every write to `core.selected_slots` and `core.bookings` belongs to their own services.
- Enforcing conflicts at booking time. A booking created through the Bookings API is **not** re-validated against this engine.
- Capacity, overlap allowance, multi-attendee seating, or resources. Every conflict is binary: one overlap makes the slot unavailable.
- Authorization. Any authenticated actor may ask about any service — see [[#Authorization Rules]].

## Domain Concepts

| Concept | Meaning in this service |
|---|---|
| Availability window | A recurring `[startTime, endTime)` on a set of weekdays, attached to a schedule. Weekdays use the ISO numbering `Monday = 1 … Sunday = 7`; `0` is not a weekday and never matches. |
| Schedule timezone | The zone in which availability times are read. Also the zone echoed back in the response. The service's own `timezone` field is presentation only and is ignored here. |
| Candidate slot | A `[start, start + defaultDuration)` interval placed every `slotInterval` minutes inside a window, before any check. |
| Booking window | `[now + minimumNotice, now + maximumAdvanceBooking]`, applied to a candidate's start. `bookingWindowType` is not consulted: `FIXED` behaves as `ROLLING` because the schema has nothing for it to be fixed to. |
| Blocking booking | A booking against **any service owned by the host** whose status is `PENDING`, `ACCEPTED` or `AWAITING_HOST` and which is not soft-deleted. |
| Live hold | A selected slot against any service owned by the host whose `releaseAt` is still in the future. Expired holds are simply ignored; nothing sweeps them. |
| Out-of-office | A period during which the host is absent. Only the absent host matters; the delegate does not block anything. |
| Buffer | The requested service's `beforeBuffer`/`afterBuffer`. Each booking and hold is expanded to `[start − before, end + after]` before comparison. Out-of-office periods are never buffered — an absence is not an engagement and needs no setup or teardown gap. |
| Host | The user who owns the service (`service → user`). Conflict scope is derived from service ownership, never from a booking's or hold's own `user_id` column, which a client can set to anything. |

## Workflow — Get available slots

```text
1. Load the service                              → 404 if unknown
2. Load its booking policy                       → 404 if the service has none
3. zone = schedule timezone; host = service owner
4. Load the schedule's availability windows; keep those whose days contain the requested weekday
   └─ none match → return an empty list. Nothing else is loaded.
5. now = current instant
   dayStart / dayEnd  = the requested date's bounds in zone
   loadFrom / loadTo  = [dayStart − afterBuffer, dayEnd + beforeBuffer]
6. Load, once each, before any loop:
   ├─ blocking bookings of the host overlapping [loadFrom, loadTo)
   ├─ live holds of the host overlapping [loadFrom, loadTo)
   └─ out-of-office periods of the host overlapping [dayStart, dayEnd)
7. For each matching window:
   ├─ generate candidates
   ├─ drop those outside the booking window
   └─ drop those overlapping a buffered booking, a buffered hold, or an out-of-office period
8. Sort by start, remove duplicates, return with the zone id
```

Overlap is the half-open interval test `candidate.start < blocked.end AND candidate.end > blocked.start`. Touching intervals do not conflict: a 10:00–10:30 booking with no buffer leaves 10:30–11:00 available.

The load window in step 5 is wider than the day on purpose. A booking that ends at 23:55 the previous day with a 15-minute after-buffer reaches into today and must block the 00:00 slot; loading only the day itself would miss it.

## Rules

### Business Rules

- A slot is offered only if it fits entirely inside an availability window. A window that cannot hold one full duration yields nothing.
- A slot whose start is earlier than `now + minimumNotice` is never offered, which also means a slot that has already started is never offered.
- A slot whose start is later than `now + maximumAdvanceBooking` is never offered.
- Bookings and holds of **every** service owned by the host block, with the requested service's buffers applied — read as "the gap this service needs around any engagement of its host".
- Cancelled, rejected, and soft-deleted bookings never block. Expired holds never block.
- An empty result is a normal answer, not an error: a closed weekday, a fully booked day, and a schedule with no windows all return `[]`.

### Validation Rules

- `serviceId` and `date` are both required; `date` is an ISO calendar date interpreted in the schedule timezone.
- A booking policy with a non-positive `slotInterval` or `defaultDuration` is rejected before generation begins. The schema has no `CHECK` constraint on those columns, and an unguarded zero interval would never terminate.

### Authorization Rules

None, by explicit decision. Any authenticated actor — a dashboard user or a bootstrapped widget — may request slots for any service. The response carries only time intervals; it never reveals which booking, hold, or absence removed a slot. Scoping is deferred to the cross-domain authorization policy plan and is recorded as a known gap in `SECURITY.md`.

## Lifecycle

The service is stateless and read-only. Each request opens one read-only transaction, performs six repository reads at most (three when the weekday is closed), and writes nothing. There is no cache; every answer reflects the database at the moment of the call.

## Failure Handling

| Condition | Outcome |
|---|---|
| Unknown `serviceId` | `404`, the service's own not-found error |
| Service without a booking policy | `404`, the booking policy's own not-found error |
| Malformed or missing `serviceId`/`date` | `400` |
| No bearer token | `401` |
| Schedule timezone that cannot be parsed | `500` — deliberately not masked with a fallback zone, which would silently shift every slot |
| Policy with `slotInterval <= 0` or `defaultDuration <= 0` | `500`, immediately — verified to fail in milliseconds rather than hang |

## Dependencies

- Services and booking policies (`core`) — the subject and its rules.
- Schedules and availabilities (`core`) — the windows and the timezone.
- Bookings, selected slots, out-of-office (`core`) — what occupies the host.
- The shared timezone conversion utility.

## Depended On By

- The booking widget (planned consumer of `GET /api/v1/slots`).
- Any future dashboard view that previews a service's availability.

## Decisions

| Decision | Rationale |
|---|---|
| Host-wide conflict scope | A single host cannot serve two services at once. MVP v2 scoped conflicts per service and allowed exactly that. |
| Scope through service ownership, not `user_id` columns | `bookings.user_id` is client-supplied today and `selected_slots.user_id` is documented inconsistently; service ownership cannot be spoofed by a payload. |
| Holds are buffered | A hold becomes a booking; an unbuffered comparison offers slots that will conflict on conversion. |
| Out-of-office is not buffered | An absence needs no preparation or cleanup time around it. |
| Schedule timezone over service timezone | Availability times belong to the schedule; the service's zone is a display preference. |
| Include-list of blocking statuses | A future status such as `COMPLETED` or `NO_SHOW` must not silently start blocking slots, which an exclude-list would cause. |
| `FIXED` treated as `ROLLING` | No columns exist to bound a fixed window; returning zero slots for every `FIXED` policy would be worse than rolling behaviour. |
| Binary conflicts, no capacity | Booking creation enforces no capacity either; the two sides should disagree on nothing until capacity is designed once for both. |
