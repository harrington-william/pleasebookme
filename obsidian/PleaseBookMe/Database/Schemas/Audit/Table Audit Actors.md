# `audit.audit_actors`

> **Variant:** `AUDIT`

## Purpose

Captures a point-in-time identity snapshot of whoever performed an audited action, independent of that actor's live record elsewhere in the platform.

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `actor_type` | `audit.audit_actor_type` | Which kind of actor this is (`GUEST`, `ATTENDEE`, `SYSTEM`, `WIDGET`, `API_KEY`, `WEBHOOK`, `INTEGRATION`, etc.). |
| `user_uid` | `VARCHAR(255)` | Optional snapshot of a platform user's uid, if the actor was a user. |
| `membership_id` | `BIGINT` | Optional snapshot of an organization membership id. |
| `widget_uid` | `VARCHAR(255)` | Optional snapshot of a widget's uid, if the actor was a widget. |
| `api_key_uid` | `VARCHAR(255)` | Optional snapshot of an API key's uid, if the actor was a programmatic caller. |
| `attendee_id` | `BIGINT` | Optional snapshot of a booking attendee id, if the actor was an unauthenticated booker. |
| `system_name` | `VARCHAR(100)` | Optional name identifying a system/internal actor. |
| `display_name` | `VARCHAR(255)` | Human-readable label for this actor. |
| `email` | `VARCHAR(255)` | Optional email address associated with the actor at the time of the event. |
| `ip_address` | `INET` | Network address the actor acted from. |
| `user_agent` | `TEXT` | Client user-agent string the actor acted with. |
| `created_at` | `TIMESTAMPTZ` | When this actor snapshot was recorded. |

## Row Semantics

Each row represents one audit-time identity snapshot of an actor — the user, membership, widget, API key, attendee, or system component that performed an audited action — captured independently of that actor's live database row.

## Ownership

- **Domain:** Audit
- **Module:** Audit & Compliance
- **Scope:** Platform — this table has no tenant/organization column of its own; tenant context lives on the `audit.audit_events` rows that reference an actor.

## Lifecycle

Created whenever an auditable operation needs to record who performed it. Only `create`/`getById`/`getAll` are implemented — there is no `update` or `delete`, per the audit domain's append-only business policy.

## Invariants

- `user_uid`, `attendee_id`, and `email` are each individually unique when non-`NULL` — enforced by three separate, independently-guarded `existsBy*` checks in `createAuditActor`, since Postgres unique constraints tolerate multiple `NULL`s.
- `user_uid`, `membership_id`, `widget_uid`, `api_key_uid`, and `attendee_id` are **deliberately not real foreign keys**. A real FK with `ON DELETE CASCADE` would delete audit history whenever the referenced live row is deleted, which defeats the purpose of keeping historical evidence — this is a permanent modeling decision, not a gap to "fix."
- `actor_type`, `display_name`, `ip_address`, and `user_agent` are required on every row.
- No `update`/`delete` verb exists on this table.

## Relationships

- **Audit Event:** `audit.audit_events.actor_id` is a real foreign key (`ON DELETE CASCADE`) into this table — every event has exactly one actor.
- **User / Membership / Widget / API Key / Attendee (conceptual, not enforced):** the `*_uid`/`*_id` columns loosely identify a corresponding live entity elsewhere in the platform, but the relationship is not a foreign key — the live entity may since have been modified or deleted.

## Usage Rules

- Writes go through `AuditActorServiceImpl` — create and read only.
- Do not add real foreign key constraints to `user_uid`/`membership_id`/`widget_uid`/`api_key_uid`/`attendee_id` — this is a deliberate, permanent exception to the platform's normal "always use a named FK constraint" convention.

## Immutability

Records are append-only after creation — no `update` or `delete` operation exists in the service layer.

## Audit Behavior

This table is the actor dimension of the audit subsystem: every `audit.audit_events` row references exactly one row here to record "who." It captures a snapshot (display name, email, IP, user agent) rather than a live pointer, so the audit trail survives deletion of the underlying user, membership, widget, API key, or attendee.

## Security Considerations

`email`, `ip_address`, and `user_agent` are personally identifying client/network information, retained indefinitely as part of the audit trail. Access to this table should be restricted according to the platform's audit authorization policy.
