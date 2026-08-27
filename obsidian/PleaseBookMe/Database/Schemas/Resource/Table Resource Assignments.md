# `resource.resource_assignments`

> **Variant:** `ASSOCIATION`

## Purpose

Assigns a staff member (via their organization membership) to a resource — e.g. which barber is assigned to which chair.

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `resource_id` | `BIGINT` | The resource being assigned. |
| `membership_id` | `BIGINT` | The organization membership (staff member) being assigned to it. |
| `assigned_at` | `TIMESTAMPTZ` | When the assignment began. |
| `released_at` | `TIMESTAMPTZ` | Optional timestamp the assignment ended. |
| `assigned_by` / `released_by` | `BIGINT` | Optional users who performed the assignment/release. |
| `is_primary` | `BOOLEAN` | Whether this is the membership's primary assignment to the resource. |

## Row Semantics

Each row represents one assignment of an organization membership (a staff member) to a resource, with an optional release marking when that assignment ended.

## Ownership

- **Domain:** Resource
- **Module:** Resource Management
- **Scope:** Organization (inherited through the parent resource/membership).

## Lifecycle

Created when a staff member is assigned to a resource. `released_at` marks the end of an assignment as a plain nullable timestamp — this table has no surrogate "status" field, so an assignment is "active" precisely when `released_at IS NULL`. Updatable and deletable directly.

## Invariants

- No unique constraint beyond the primary key — the schema does not itself prevent the same membership being assigned to the same resource more than once (e.g. across separate assignment periods).
- `assigned_by`/`released_by` are nullable with `ON DELETE SET NULL`.

## Relationships

- **Resource:** The resource being assigned.
- **Membership:** The staff member (via their organization membership, not a raw user) being assigned.
- **User (assigned by / released by):** Who performed the action, when known.

## Usage Rules

- Writes go through `ResourceAssignmentServiceImpl`.
- **`membership_id` is currently resolved as a bare reference entity, not a validated repository lookup**, in both `createResourceAssignment` and `updateResourceAssignment` — even though `MembershipRepository` now exists (used elsewhere, e.g. by `MembershipServiceImpl`). This means an invalid `membershipId` is not caught as a clean 404; it surfaces as a raw foreign-key-violation error from the database. This is a real, currently-existing gap in this specific service, not a documentation artifact — it was verified directly against `ResourceAssignmentServiceImpl` in this session.
