# `core.out_of_office`

> **Variant:** `STATE`

## Purpose

Blocks out a time window during which a user is unavailable, optionally redirecting attention to a delegate user during that period.

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `uid` | `UUID` | Stable, externally-safe identifier for this record. |
| `start_time` / `end_time` | `TIMESTAMPTZ` | The out-of-office window. |
| `notes` | `TEXT` | Optional internal notes. |
| `show_note_publicly` | `BOOLEAN` | Whether `notes` should be visible to customers/bookers. |
| `user_id` | `BIGINT` | The user who is out of office. |
| `to_user_id` | `BIGINT` | The delegate user, if coverage is being redirected. |
| `reason` | `TEXT` | Why the user is out of office. |
| `created_at` / `updated_at` | `TIMESTAMPTZ` | Row creation/last-modified timestamps. |

## Row Semantics

Each row represents one period during which a user is unavailable, with an optional delegate to redirect coverage to.

## Ownership

- **Domain:** Core
- **Module:** Availability
- **Scope:** User.

## Lifecycle

Created when a user schedules time away. Both `user_id` and `to_user_id` are required (`to_user_id` is not optional at the schema level, despite "delegate coverage" reading like an optional feature) — every out-of-office record names a covering user.

## Invariants

- `user_id`/`to_user_id` are both required, both `ON DELETE CASCADE` into `auth.users`.
- `reason` is required; `notes` is optional.
- No unique constraint beyond the primary key — overlapping out-of-office windows for the same user are not prevented at the database level.

## Relationships

- **User:** The user who is out of office.
- **User (delegate):** The user coverage is redirected to.

## Usage Rules

- Writes go through `OutOfOfficeServiceImpl`.
- Availability computation is expected to treat an active out-of-office window as removing the affected user's slots (and potentially routing to the delegate), though the actual enforcement path was not located in this session — flagged as **Undetermined**, same open question as `resource.resource_maintenance`.
