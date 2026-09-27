# Full Page Widget Service

## Purpose

`service/widget/fullpage` is the cross-domain application service behind every tenant's hosted booking page — the *full-page widget*, one of the platform's booking channels. It was named `service/publicbooking` until 2026-09-22; the name "public booking" is retired, and `service/widget/` now holds one subpackage per channel. It resolves a public organization and bookable services without a platform identity, delegates availability to the existing slot engine, and creates a booking plus attendee in one transaction.

## Boundary

- Base path: `/api/v1/public` (unchanged by the rename)
- Client route: `/booking/{organizationSlug}` and `/booking/{organizationSlug}/{serviceSlug}`
- Authentication: none by design
- Served tenants: `ACTIVE` and `TRIAL`
- Organization identity: `organization.slug` from the URL
- Bookable service: an organization service with a booking-policy row
- Explicitly ignored: `organization.is_private`, because provisioning currently sets it on all new organizations

Unknown, tenant-less, suspended, pending, and archived organization slugs all return the same organization 404. Reserved slugs are no longer checked here — the client route's `/booking` prefix means an organization slug cannot shadow an application route, and `ReservedOrganizationSlugs` still blocks those names at organization-creation time, so no such row exists to resolve. Unknown services and services without a policy return the same service 404. This prevents the endpoint from exposing lifecycle state.

## Structure

Since TASK-0012 this service is an adapter, not the flow. `FullPageWidgetServiceImpl` resolves the organization from the URL slug via `SlugOrganizationResolver` and delegates all four operations to the shared [[Barbershop Booking Service]], which holds the policy query, the host lock, the slot re-validation and the writes. The [[Embedded Widget Service]] is the same shape over a widget token.

`createBooking` is deliberately not `@Transactional` here, so the resolver runs before the flow opens its write transaction.

## Operations

| Operation | Effect |
|---|---|
| Get organization | Returns the organization's public branding plus policy-backed services using one policy `IN` query. |
| Get service | Returns public service/policy data and the sorted union of availability weekdays. |
| Get slots | Resolves the path-owned service and delegates unchanged to `SlotService`. |
| Create booking | Locks the host user, regenerates the requested schedule-zone day, then inserts the booking and attendee only if the exact slot is still offered. |

## Booking transaction

The pessimistic host-user lock is taken before slot generation. This serializes bookings across all services of one host, matching the slot engine's host-wide conflict rule. The requested `slotStart` is converted to the schedule timezone to select the engine date; `slotEnd` is derived from `defaultDuration`. An exact `TimeSlot` value match is mandatory.

An auto-confirming policy writes `ACCEPTED`; otherwise it writes `AWAITING_HOST`.

The booking row's `title` is **`"<service.title> with <attendee.name>"`** — for example `Consultant Meeting with Jane Doe`. This is the host-facing label in the dashboard and in any calendar the booking is later synced to, and it matches the "with …" phrasing the booking page itself uses. MVP v2's `BookingResolver` used `"<service> - <customer>"`; that format was deliberately not carried over (TASK-0011 D7), and the implementation briefly regressed to it before being corrected on 2026-09-22. `description` is the visitor's notes, `location` is copied from the service, and the attendee contact fields and timezone follow the request contract. No customer row, notification, selected-slot hold, or widget row is created.

## Failure and abuse controls

The Spring interceptor enforces IP fixed windows in Redis before the controller: 120 reads/minute and 5 writes/minute by default. Redis failure fails closed. A stale slot returns 409; limit exhaustion returns 429 with `Retry-After`. hCaptcha, blacklists, and idempotency are deferred.

## Classes

| Role | Class |
|---|---|
| Controller | `FullPageWidgetController` (`@RestController("fullPageWidgetController")`, `/api/v1/public`) |
| Service | `FullPageWidgetService` / `FullPageWidgetServiceImpl` |
| DTOs | `WidgetOrganizationResponse`, `WidgetServiceSummary`, `WidgetServiceResponse`, `WidgetBookingRequest`, `WidgetBookingResponse` |
| Exception | `SlotUnavailableException` (409) |

The DTOs are named `Widget*`, not `FullPageWidget*`, because they describe a channel-independent booking shape that the embedded widget channel will reuse verbatim (TASK-0012).
