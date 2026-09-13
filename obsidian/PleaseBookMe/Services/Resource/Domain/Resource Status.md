# Resource Status

## Definition

**Status** is a resource's operational condition — whether the asset is in service, and if not, why.

It is drawn from a closed, platform-wide set. Unlike [[Resource Type]], which is organization-specific vocabulary, condition means the same thing everywhere: a room under repair is under repair regardless of who owns it.

## The States

| Status | Meaning |
|---|---|
| `ACTIVE` | In service and available to be allocated. |
| `INACTIVE` | Temporarily withdrawn by choice. The asset is fine; the organization is not currently offering it. |
| `MAINTENANCE` | Unavailable because it is being serviced or repaired. |
| `RETIRED` | Permanently withdrawn. Kept for history, never to return. |

The distinction between `INACTIVE` and `MAINTENANCE` is intent, not availability — both are unavailable, but one is a business decision and the other a physical condition. They are separated because an operator reading an inventory needs to know which of the two they are looking at, and because "how much of my estate is broken" is a different question from "how much am I choosing not to offer."

`RETIRED` is distinguished from deletion: a retired asset preserves its history, its past assignments, and any bookings that referenced it. Deletion destroys them.

## Transitions

The platform imposes **no transition rules**. Any status may be set at creation, and any status may be changed to any other.

This is deliberate for now. Real estates move between these states in ways a rigid state machine tends to get wrong — an asset can go from `RETIRED` back to `ACTIVE` when a repair turns out to be viable, and from `ACTIVE` straight to `RETIRED` when it is written off without ever being serviced. Until there is a concrete rule someone actually needs enforced, allowing every transition is the honest model.

See [[Resource Lifecycle]] for how these states are used in practice.

## Status Must Be Stated

There is no default. Creating a resource requires saying what condition it is in.

The reasoning is in [[Resource Status Has No Default]]: silently defaulting an unstated condition to `ACTIVE` would put an asset into circulation that nobody asserted was ready.

## Nothing Enforces It Yet

**This is the most important caveat in the service.**

Status is recorded faithfully and reported accurately, but **no code path consults it before allocating a resource to a booking**. The same is true of the resource's bookable flag.

A resource marked `MAINTENANCE` is, today, exactly as bookable as one marked `ACTIVE`. The field expresses intent that the booking layer is expected to honour once that layer exists.

Anything that needs to answer "may this asset be used right now?" must read the status itself and decide. It cannot assume the platform has already filtered on its behalf.
