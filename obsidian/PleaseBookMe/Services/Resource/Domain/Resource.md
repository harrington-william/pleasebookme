# Resource

## Definition

A **resource** is one allocatable asset belonging to one organization — the thing a reservation is ultimately allocated against, as distinct from the service being booked and from the booking itself.

Examples: a treatment room, a barber chair, a projector, a delivery van, a court, a video bridge.

## Identity

A resource carries two identifiers with different audiences:

- An **internal identifier**, used for relationships inside the platform.
- A **stable external identifier**, safe to expose outside the platform.

It also carries a **slug** — a human-readable, URL-safe handle that is unique **within its organization**. Two organizations may each own a `room-1`; one organization may not own two.

## Descriptive Properties

| Property | Meaning | Required |
|---|---|---|
| Name | Display name, e.g. "Conference Room A" | Yes |
| Slug | URL-safe handle, unique per organization | Yes |
| Type | Classification — see [[Resource Type]] | Yes |
| Status | Operational condition — see [[Resource Status]] | Yes |
| Description | Free text on how the asset is used | No |
| Capacity | How many concurrent occupants or units it holds | No |

Capacity is optional because it is meaningless for a large class of assets. A projector has no capacity; a meeting room does. The inventory view renders an absent capacity as "not applicable" rather than as zero, because zero would read as "holds nobody" — a materially different claim.

## Behavioural Flags

Two flags describe intent rather than description:

- **Bookable** — whether the asset should be offered for new reservations at all.
- **Virtual** — whether the asset has no physical presence, such as a conference bridge. Virtual assets can be allocated without any spatial constraint.

Both default to sensible values (bookable, not virtual) and can be set at creation.

Note that "bookable" and [[Resource Status]] overlap in intent and are **both currently advisory**: no code path consults either before allocating. See [[Resource Status]] for why this matters.

## Relationships

- Belongs to exactly one **organization**, which is the isolation boundary.
- Has exactly one **[[Resource Type]]**.
- May serve **zero or more bookable services**, through [[Resource Service Assignment]].

## Invariants

- Slug is unique within an organization.
- Organization is fixed at creation and never changes afterwards.
- Type is always present; a resource cannot be uncategorised.
- Status is always explicitly stated — there is no implicit default.

## Deletion

Deleting a resource also removes its service assignments. The assignments describe the resource's capability, so they have no meaning once the asset is gone.

Deleting a resource's **type** also deletes the resource. This is a sharper consequence than it first appears — see [[Resource Type]].
