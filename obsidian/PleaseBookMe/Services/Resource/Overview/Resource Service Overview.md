# Resource Service Overview

## What This Service Is

The Resource service is the platform's **inventory of allocatable assets**.

A resource is any physical or virtual thing a reservation can be allocated against — a treatment room, a barber chair, a projector, a van, a tennis court, a video conference bridge. The service answers three questions for an organization:

1. What assets do we have?
2. How are they classified, and what condition are they in?
3. Which of our bookable services can each asset serve?

It is an **inventory and classification** capability, not a scheduling one. It records that a room exists, that it seats twelve, that it is currently under maintenance, and that it can host Client Meetings and Workshops. It does not decide whether a particular booking may take that room at 3pm on Thursday.

## Why It Exists Separately

Bookable services (what a customer buys) and resources (what physically delivers it) change independently. A clinic can add a new consultation type without buying a room; it can take a room offline for repairs without withdrawing the service that room usually hosts.

Modelling resources separately lets the platform express that a single asset serves several services and that a service is delivered by any of several interchangeable assets — the relationship the booking layer will eventually need in order to allocate.

## Responsibilities

- Maintain the per-organization catalogue of resource types
- Record and classify the organization's resources
- Track each resource's operational condition
- Record which bookable services each resource can serve
- Answer inventory questions: browse, filter, search, and summarise the estate

## Does Not Own

- **Reservation workflows.** Nothing here creates, allocates, or validates a booking.
- **Availability computation.** Whether an asset is free at a given moment is a scheduling concern.
- **Booking eligibility enforcement.** The service records condition; it does not police it — see [[Resource Status]] for the consequence.
- **Pricing, staff rostering, maintenance windows, calendars, and custom attributes.** These live in sibling tables of the same schema but are not part of this service's behaviour today.
- **Identity and organization membership.** Both are consumed, not owned — see [[Resource Service Dependencies]].

## Ownership Boundary

Every resource and every resource type belongs to exactly one organization. The organization is never supplied by the caller; it is derived from the caller's accepted membership — see [[Ownership Is Derived Not Supplied]].

This makes the organization the unit of isolation for the whole service: two organizations may both own a "Room 1" without collision, and neither can see the other's estate through the ordinary inventory views.

## Current Maturity

The service is functional for cataloguing and assignment. Two things are deliberately incomplete and should be understood before building on it:

- **No consumer enforces resource condition yet.** See [[Resource Status]].
- **Authorization is coarse.** Organization scoping is applied on the collection views but not uniformly on single-resource access — see [[Resource Authorization Rules]].

## Related

- [[Resource Schema]] — the persistence model
- [[Resource API Summary]] — the HTTP surface
