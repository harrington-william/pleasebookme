# Decision: Ownership Is Derived Not Supplied

## Context

Every [[Resource]] and [[Resource Type]] belongs to an organization. Originally the caller supplied that organization in the request.

## Problem

Identifiers are sequential and therefore trivially guessable. A caller could name any organization and have their record created inside it. The boundary that was supposed to isolate organizations was a field the caller filled in.

The same weakness applied to updates, where the ownership field could move an existing asset into a different organization.

## Decision

Derive the organization from the caller's **accepted membership**. Remove the field from requests entirely.

## Why Membership Rather Than Profile

An earlier approach in a neighbouring service derived ownership from the caller's *profile*. Membership is the correct basis, for two reasons:

- **A profile is a display identity** — a name presented within an organization. It says nothing about entitlement.
- **Membership records acceptance.** An invitation that was never accepted is not membership, and deriving from a profile would have treated it as though it were.

## Consequences

- Acting on another organization's estate is not merely rejected — it is **inexpressible**. There is no field in which to attempt it.
- Ownership became immutable, because an update has no ownership field to change.
- A caller with no accepted membership cannot act at all.
- A caller belonging to several organizations makes the request ambiguous. The service refuses to guess rather than silently picking one — recorded in [[Resource Authorization Rules]].

## Limitation

Multi-organization callers are currently blocked rather than served. The intended resolution is for the request to *name* the organization and for membership to validate that choice, which restores the guarantee while supporting several memberships. The distinction is important: naming an organization that is then validated is safe; naming one that is trusted is exactly the flaw this decision removed.
