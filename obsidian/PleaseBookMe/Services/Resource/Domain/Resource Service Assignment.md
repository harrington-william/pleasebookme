# Resource Service Assignment

## Definition

An **assignment** records that one [[Resource]] is capable of serving one bookable service.

It is a statement of capability, not of schedule. "Conference Room A can host Client Meetings" — not "Conference Room A is hosting a Client Meeting on Thursday."

## Shape Of The Relationship

The relationship is **many-to-many**:

- One resource may serve several services. A conference room hosts both Client Meetings and Workshops.
- One service may be served by several resources. Any of four identical treatment rooms can deliver a consultation.

The second direction is what makes allocation possible at all: a booking layer needs a set of candidate assets to choose from, not a single predetermined one.

This shape replaced an earlier model in which a resource pointed at exactly one service — see [[Resources Serve Many Services]].

## Identity

The assignment **is** the pair of resource and service. There is no separate identifier for the relationship, because there is nothing to say about it beyond the fact that it holds.

It also records **when** the capability was recorded, which is useful for auditing how an estate's configuration evolved.

## Invariants

- A given resource–service pair exists at most once. Recording the same capability twice is rejected rather than silently accepted — the reasoning is in [[Assignments Are Created and Revoked Never Updated]].
- Both sides must exist before the assignment can be recorded.
- Removing either side removes the assignment.

## Operations

An assignment can be **created** and **revoked**. It cannot be edited.

Changing "Room A serves Workshops" into "Room B serves Workshops" is not an edit of one fact — it is the revocation of one capability and the assertion of a different one. Modelling it as an update would obscure that. See [[Assignments Are Created and Revoked Never Updated]].
