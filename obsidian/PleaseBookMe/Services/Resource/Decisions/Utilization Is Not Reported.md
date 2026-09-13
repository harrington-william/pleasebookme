# Decision: Utilization Is Not Reported

## Context

The inventory summary reports how many assets exist and how many are in each condition. The interface design also called for a **utilization rate** — a single percentage describing how heavily the estate is used.

## Decision

Do not report it. Show the measure as unavailable, and say why.

## Rationale

**The data does not exist.** Utilization is a ratio of time used to time available. Time used requires booking history; time available requires each asset's availability. Neither is connected to this service — see [[Resource Service Dependencies]].

**The definition does not exist either.** Utilization over what window — a day, a week, a quarter? Against calendar hours or opening hours? Are assets under maintenance excluded from the denominator, or counted as fully unused? Do virtual assets, which have no scarcity, participate at all? These are business questions with materially different answers, and none has been decided.

**A plausible number would be worse than none.** The counts already available could easily produce a percentage — active over total, say. It would render convincingly, and it would measure the composition of the estate rather than its usage. Someone would eventually make a purchasing decision on it. A visibly absent measure invites the right question; a wrong one does not get asked about.

## Consequences

- The summary reports counts by condition, which are exactly and unambiguously true.
- The utilization measure is presented as unavailable with its reason stated, not silently omitted — a missing tile reads as a bug, an explained one reads as a decision.
- Implementing it later requires deciding the definition first, then joining booking history against availability. It is a genuine feature, not a formatting change.

## Generalisation

Where a measure is asked for but its inputs or its definition are missing, showing it as unavailable is the honest answer. The cost of an absent number is a question; the cost of a wrong number is a decision.
