# `integration.sync_jobs`

> **Variant:** `STATE`

## Purpose

Queues asynchronous, retryable work to synchronize a booking to an external Google destination (Calendar, Sheet, or Drive).

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `uid` | `UUID` | Stable, externally-safe identifier for this job. |
| `job_type` | `integration.sync_job_type` | `CALENDAR_SYNC` / `SHEET_SYNC` / `DRIVE_BACKUP`. |
| `booking_id` | `BIGINT` | The booking this sync job concerns. |
| `oauth_connection_id` | `BIGINT` | The delegated-authorization credential this job will use. |
| `status` | `integration.sync_job_status` | `PENDING` / `PROCESSING` / `SUCCEEDED` / `FAILED` / `DEAD_LETTER`. |
| `attempts` | `INTEGER` | How many times this job has been attempted so far. |
| `max_attempts` | `INTEGER` | The attempt ceiling before the job is considered dead-lettered. |
| `available_at` | `TIMESTAMPTZ` | When this job next becomes eligible to be picked up (supports backoff scheduling). |
| `last_attempt_at` | `TIMESTAMPTZ` | Optional timestamp of the most recent attempt. |
| `last_error` | `TEXT` | Optional error message from the most recent failed attempt. |
| `created_at` / `updated_at` | `TIMESTAMPTZ` | Row creation/last-modified timestamps. |

## Row Semantics

Each row represents one unit of asynchronous work to synchronize a specific booking to a specific external destination via a specific OAuth connection.

## Ownership

- **Domain:** Integration
- **Module:** Sync Queue
- **Scope:** User (transitively, via the owning OAuth connection) → Booking.

## Lifecycle

Created (presumably) whenever a booking needs to be synced to a connected external destination. `status` progresses `PENDING` → `PROCESSING` → `SUCCEEDED`/`FAILED`, with `attempts` incrementing on each try and `available_at` used for backoff scheduling; a job that exhausts `max_attempts` is expected to move to `DEAD_LETTER`. No worker/consumer code path was located in this session to confirm the actual processing loop — this table's shape (composite index on `(status, available_at)`) strongly implies a polling worker, but its existence is **Undetermined** from the evidence inspected.

## Invariants

- `uid` is generated, never client-supplied.
- `booking_id`/`oauth_connection_id` are required, both `ON DELETE CASCADE`.
- This is the first table in the schema explicitly structured for eventual, retried processing rather than a direct-write operational record — see the platform's architecture notes on consistency model.

## Relationships

- **Booking:** The booking this sync job concerns.
- **OAuth Connection:** The credential the job will authenticate with.

## Usage Rules

- Writes go through `SyncJobServiceImpl`.
- No consumer/worker implementation was confirmed in this session — treat the actual sync execution mechanism as **Undetermined** pending further inspection of scheduled/background job configuration.

## Concurrency / Idempotency

`attempts`/`max_attempts`/`available_at` together form a retry-with-backoff mechanism typical of a durable work queue, though the exact concurrency guarantees (e.g. row-locking on pickup to prevent two workers processing the same job) were not verified in this session.

## Flagged for Follow-up

- Confirm whether a worker/consumer actually processes this queue. Only `SyncJobServiceImpl` (plain CRUD) was located — no scheduled job, listener, or polling loop reading `status`/`available_at` was found. If no consumer exists yet, this table is scaffolding ahead of the feature that would use it, same pattern as `auth.api_keys`.
- Once a consumer is found or built, verify its concurrency guarantee (row-locking or equivalent) so two workers can't process the same job twice.
