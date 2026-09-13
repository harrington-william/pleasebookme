# Decision: Resources Serve Many Services

## Context

A resource originally pointed at exactly one bookable service, as a required single reference. Every asset had to name one service, and could name only one.

## Problem

The model contradicted how estates actually work in both directions:

- A conference room hosts Client Meetings *and* Workshops. Under a single reference, the room had to pick one, or be duplicated once per service.
- More importantly, allocation needs the reverse: given a service, *which assets can deliver it?* A single reference could express that a room serves meetings, but not that any of four rooms could.

The requirement was also visible in the interface design, which showed several services against a single asset — the model simply could not express what the product already assumed.

## Decision

Model the relationship as **many-to-many**, in its own right, and remove the single reference from the resource.

The pair of resource and service is the whole fact, so the pair is the identity. There is no separate identifier for the relationship.

## Consequences

- A resource may serve any number of services, including none. An asset with no assignments is valid — a newly acquired room that has not been put to use yet.
- A service may be served by any number of resources, which is what makes allocation from a candidate set possible.
- Capability became a separate concern from the asset itself, so it is recorded and revoked independently — see [[Assignments Are Created and Revoked Never Updated]].
- Reading the inventory now needs capability resolved alongside the assets, which shaped how [[Browse the Resource Inventory]] fetches.

## Rejected Alternative

**Keep the single reference and make it optional.** This would have removed the "must pick one" problem without addressing the real one: the reverse direction. Allocation would still have had no way to ask which assets can serve a service. It solved the visible symptom and not the requirement.
