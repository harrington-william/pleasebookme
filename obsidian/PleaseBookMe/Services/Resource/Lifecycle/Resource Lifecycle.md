# Resource Lifecycle

How an organization's estate comes into being and changes over time.

## Organization Setup

An organization begins with **no vocabulary and no assets**. The first meaningful act is defining a [[Resource Type]], because a [[Resource]] cannot exist without one.

This ordering is a genuine constraint, not an artefact:

```text
organization exists
        │
        ▼
define a resource type          ← unavoidable first step
        │
        ▼
create resources of that type
        │
        ▼
assign resources to services
```

Anything built on this service has to handle the empty-vocabulary state deliberately, because a brand-new organization always starts there and cannot create a resource until it leaves.

## An Individual Resource

```text
                    created (in a stated condition)
                              │
                              ▼
                    ┌──────► ACTIVE ◄──────┐
                    │         │            │
                    │         ▼            │
              MAINTENANCE   INACTIVE ───────┘
                    │         │
                    └────┬────┘
                         ▼
                      RETIRED
                         │
                         ▼
                     (deleted)
```

Every arrow is permitted, and so are the ones not drawn: the platform enforces no transition rules. The diagram shows the *usual* path, not a constraint — see [[Resource Status]].

A resource's condition may change any number of times. Its organization never changes, and its identity is stable across every transition.

## Capability Over Time

Assignments accrue and are revoked independently of condition. An asset under maintenance keeps its recorded capabilities — it is still *capable* of hosting workshops; it is merely unavailable. Losing the assignments on every maintenance window would mean rebuilding the estate's configuration each time something is repaired.

Each assignment records when it was made, so the evolution of an estate's configuration can be reconstructed.

## End Of Life

Two distinct endings, easily confused:

- **Retirement** keeps the record and its history. The asset is permanently out of service but remains countable, attributable, and referenced by past bookings.
- **Deletion** destroys the record and its assignments.

Retirement is nearly always correct for an asset that existed and has now gone. Deletion is for records that should never have existed.

A third ending is easy to trigger by accident: deleting a [[Resource Type]] deletes every resource under it, bypassing this distinction entirely.

## What The Lifecycle Does Not Yet Govern

No stage of this lifecycle currently affects whether a booking can be made. Condition, retirement and the bookable flag are all recorded and reported but not enforced — see [[Resource Status]].
