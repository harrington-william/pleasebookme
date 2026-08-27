# `core.bookings`

> **Variant:** `TRANSACTION`

## Purpose

Records one reservation made against a service — the central operational record of the platform's reservation engine.

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `uid` | `UUID` | Stable, externally-safe identifier — used as the external booking reference (e.g. by third-party integrations). |
| `idempotency_key` | `VARCHAR(255)` | Optional caller-supplied dedup key (see Invariants). |
| `user_id` | `BIGINT` | The user (host) this booking is made with. |
| `title` | `VARCHAR(255)` | Booking title. |
| `description` | `TEXT` | Optional description. |
| `start_time` / `end_time` | `TIMESTAMPTZ` | The reserved time window. |
| `service_id` | `BIGINT` | The service this booking is made against. |
| `location` | `TEXT` | Optional booking location. |
| `status` | `core.booking_status` | `PENDING` / `ACCEPTED` / `REJECTED` / `AWAITING_HOST` / `CANCELLED`. |
| `paid` | `BOOLEAN` | Whether payment has been made. |
| `cancelled_by` | `BIGINT` | Optional user who cancelled the booking. |
| `cancellation_reason` | `TEXT` | Optional reason given for cancellation. |
| `rejection_reason` | `TEXT` | Optional reason given for rejection. |
| `rescheduled` | `BOOLEAN` | Whether this booking has been rescheduled. |
| `rescheduled_by` | `BIGINT` | Optional user who rescheduled it. |
| `no_show_host` | `BOOLEAN` | Whether the host failed to show. |
| `deleted_at` | `TIMESTAMPTZ` | Optional soft-delete marker (see Lifecycle). |
| `deleted_by` | `BIGINT` | Optional user who deleted the booking. |
| `destination_calendar_id` / `destination_sheets_id` | `BIGINT` | Optional external sync destinations for this booking. |
| `metadata` | `JSONB` | Free-form, application-defined data. |
| `created_at` / `updated_at` | `TIMESTAMPTZ` | Row creation/last-modified timestamps. |

## Row Semantics

Each row represents one booking request and its resulting operational state — from initial creation through acceptance, rejection, cancellation, or rescheduling.

## Ownership

- **Domain:** Core
- **Module:** Reservation Lifecycle
- **Scope:** Organization (transitively, via the owning service) → Host User.

## Lifecycle

Created against a service via the standard CRUD service (`BookingServiceImpl`), which resolves every FK — including the three separate nullable `auth.users` references (`cancelled_by`, `rescheduled_by`, `deleted_by`) and the two optional destination-sync FKs — through real repository lookups. `status` is expected to move between `PENDING`, `ACCEPTED`, `REJECTED`, `AWAITING_HOST`, and `CANCELLED`, but no dedicated state-transition endpoint (accept/reject/cancel) was located in this session — a status change currently relies on a full-replace `PUT` supplying the new value. `deleted_at`/`deleted_by` are plain nullable columns, not managed by `@CreationTimestamp`/`@UpdateTimestamp` — they are exposed like any other mutable field under full-replace CRUD, not backed by a dedicated soft-delete workflow. A separate physical `DELETE` also exists.

## Invariants

- `uid` is generated, never client-supplied.
- `idempotency_key` is a plain, unvalidated `VARCHAR` — the migration declares no uniqueness on it, so despite its name it is **not currently enforced as a deduplication mechanism** at the database level.
- `cancelled_by`/`rescheduled_by`/`deleted_by` are all nullable, `ON DELETE SET NULL` — losing the referenced user preserves the booking record, only the attribution.
- No overlap/conflict-prevention constraint exists on `(service_id, start_time, end_time)` at the database level — conflict detection, if it happens, happens in application logic not inspected in this session.

## Relationships

- **User (host):** The user this booking is made with.
- **Service:** The service this booking is made against.
- **Attendee:** `core.attendees.booking_id` — the attendee(s) attached to this booking.
- **Sync Job:** `integration.sync_jobs.booking_id` — queued work to sync this booking to an external destination.
- **Destination Calendar / Sheets:** Optional external sync targets specific to this booking (in addition to the service-level defaults on `core.services`).

## Usage Rules

- Writes go through `BookingServiceImpl`.

## Flagged for Follow-up

- **No availability/conflict-detection or accept/reject/cancel workflow was found in `BookingServiceImpl`** — it is plain CRUD (create/read/update/delete resolving FKs), with no check that `start_time`/`end_time` doesn't overlap an existing booking on the same service, and no dedicated endpoint for the status transitions the `status` enum implies. Given `CLAUDE.md`'s account that MVP v1's core discovery was "the real complexity of booking systems exists in temporal orchestration, concurrency control, and availability computation," this strongly suggests that logic lives elsewhere (a service not yet built, or one this session didn't locate) rather than being genuinely absent — worth confirming before assuming bookings can currently be created without any conflict checking in production use.
- `idempotency_key` has no backing unique constraint — confirm whether idempotent booking creation is enforced anywhere else (e.g. at the widget/API boundary) before relying on it.
