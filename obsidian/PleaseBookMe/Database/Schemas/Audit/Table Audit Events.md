# `audit.audit_events`

> **Variant:** `AUDIT`

## Purpose

Records one auditable operation performed within the platform, along with its actor, optional tenant/organization context, and outcome — the central table of the audit subsystem.

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `uid` | `UUID` | Stable, externally-safe identifier for this event. |
| `correlation_id` / `request_id` / `trace_id` | `VARCHAR(255)` | Optional identifiers linking this event back to the originating request/trace. |
| `tenant_id` | `BIGINT` | Optional tenant context, when the event occurred within one. |
| `organization_id` | `BIGINT` | Optional organization context. |
| `actor_id` | `BIGINT` | The actor responsible for this event. |
| `event_domain` | `audit.event_domain` | Which platform domain the event occurred in (e.g. `AUTH`, `CORE`, `TENANT`). |
| `event_type` | `audit.event_type` | The kind of entity the event concerns (e.g. `USER`, `SERVICE`, `WIDGET`). |
| `action` | `audit.audit_action` | The action that occurred (e.g. `CREATED`, `CANCELLED`, `LOGIN`). |
| `severity` | `audit.severity` | Security/operational severity classification. |
| `status` | `audit.audit_status` | Outcome of the audited operation (`SUCCESS`/`FAILED`/`PARTIAL`). |
| `context` | `JSONB` | Optional free-form contextual data. |
| `event_version` | `INTEGER` | Schema version of this event's payload shape; defaults to `1`. |
| `occurred_at` | `TIMESTAMPTZ` | When the audited operation actually occurred. |
| `created_at` | `TIMESTAMPTZ` | When this audit row was written. |

## Row Semantics

Each row represents one auditable event: one action, performed by one identified actor, at one point in time, with a recorded outcome.

## Ownership

- **Domain:** Audit
- **Module:** Audit & Compliance
- **Scope:** Tenant/Organization when the originating operation had one (both nullable); platform-wide otherwise.

## Lifecycle

Created whenever an auditable operation occurs elsewhere in the platform. Only `create`/`getById`/`getAll` are implemented — there is no `update` or `delete`, per the audit domain's append-only business policy documented in the platform's architecture notes.

## Invariants

- `actor_id` is required and foreign-keys into `audit.audit_actors` with `ON DELETE CASCADE` — deleting an actor snapshot removes every event attributed to it. This is the one place cascading delete applies within the audit subsystem, since the actor row being removed is itself audit data, not live operational data.
- `tenant_id`/`organization_id` are optional with `ON DELETE SET NULL` — an event's tenant/organization context is preserved on a best-effort basis; losing the referenced tenant or organization nulls the reference rather than deleting the event.
- `uid` is generated (`@UuidGenerator`), never client-supplied — there is no duplicate check for it, and no `Duplicate*Exception` exists for this table.
- No `update`/`delete` verb exists.

## Relationships

- **Actor:** `audit.audit_actors` identifies who performed the action.
- **Resource(s):** `audit.audit_resources` rows attach to this event — one event can concern zero, one, or several affected resources.
- **Tenant / Organization:** optional context of where the event occurred.

## Usage Rules

- Writes go through `AuditEventServiceImpl` — create and read only.
- Application code must not attempt to update or delete existing audit events.
- `occurred_at` (when the audited operation happened) and `created_at` (when this row was written) are stamped independently — both default to the current time at insert, but are conceptually distinct if an event is ever recorded after the fact.

## Important Fields

- `event_domain` / `event_type` / `action` / `severity` / `status` — the primary classification axes an event is filtered/queried by; all five are individually indexed.
- `correlation_id` / `request_id` / `trace_id` — link this event to the originating request or distributed trace for cross-system debugging.
- `context` — free-form JSONB, excluded from `AuditEventRequest`/`AuditEventResponse`, unlike the before/after snapshots on `audit.audit_resources` (see that table's notes on why the two are treated differently).

## Immutability

Records are append-only after creation.

## Audit Behavior

This is the central event record of the audit subsystem — every audited operation produces exactly one row here, which other audit tables (`audit_resources`, transitively `audit_changes`) attach additional detail to. It represents historical evidence, not the operational source of truth for the business event it describes.

## Security Considerations

Audit events may contain security-sensitive context (actor linkage, request metadata, tenant/organization scope). Access should be restricted according to the platform's audit authorization policy.
