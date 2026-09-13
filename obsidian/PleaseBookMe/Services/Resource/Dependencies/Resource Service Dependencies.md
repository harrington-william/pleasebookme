# Resource Service Dependencies

## What This Service Depends On

### Organization membership — required

The service cannot act without resolving the caller to a single organization holding an **accepted** membership. This supplies the ownership and isolation boundary for every operation.

This is the service's hardest dependency. Without it there is no answer to "whose asset is this?", and every operation is undefined.

### Identity — required

The authenticated actor, from which membership is resolved. The service consumes identity and never establishes it.

### The bookable service catalogue — required for capability

Assignments point at bookable services. The service catalogue is authoritative for what a service is; this service only records which assets can serve one.

The dependency is one-directional: resources know about services, services do not know about resources.

## What Depends On This Service

**Nothing enforces resource condition yet.** No booking or scheduling flow currently consults resources at all.

The intended consumer is allocation: when a booking is made for a service, the booking layer will need the set of assets capable of serving it, filtered by condition and availability. That consumer does not exist, which is why [[Resource Status]] is currently advisory.

Anything that becomes a consumer must read condition itself and decide, rather than assuming the platform has filtered on its behalf.

## Sibling Capabilities Not Owned Here

The same schema holds pricing, staff assignment, calendars, maintenance windows, overrides, and custom attributes. All scope by resource, and all are outside this service's behaviour today. They are storage and basic record-keeping, not part of the inventory capability described here.

Do not assume, for example, that a maintenance window recorded against a resource changes that resource's condition. Nothing connects them.

## Directional Summary

```text
        identity ──────┐
                       ▼
    organization ──► Resource service ──► records capability against
      membership          │                the bookable service catalogue
                          │
                          ▼
                 (no consumer yet)
              allocation will read this
```

## Related

- [[Resource Schema]] — persistence model
- [[Resource API Summary]] — HTTP surface
