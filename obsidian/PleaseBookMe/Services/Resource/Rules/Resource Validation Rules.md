# Resource Validation Rules

What the service accepts as well-formed input. These are correctness guardrails, not authorization — see [[Resource Authorization Rules]] for who may act.

## Resource

| Field | Rule |
|---|---|
| Name | Required, bounded length |
| Slug | Required, bounded length, unique within the organization |
| Type | Required, must identify an existing type |
| Status | Required, must be one of the known conditions |
| Description | Optional, unbounded |
| Capacity | Optional whole number |
| Bookable / Virtual | Optional; sensible defaults applied when omitted |

An unrecognised status value is a malformed request, not a not-found: the caller supplied something outside the closed set of conditions, which is a shape error rather than a missing record.

## Resource Type

| Field | Rule |
|---|---|
| Name | Required, bounded length |
| Description | Optional |
| Icon | Optional, bounded length |

## Assignment

Both the resource and the service must be identified, and both must exist.

## Inventory Queries

- Filters are optional and combine conjunctively.
- Text search matches name and slug, case-insensitively, and treats the caller's input as literal text. Pattern characters in a search term match themselves rather than acting as wildcards, so searching for a term containing them does not silently widen the query to everything.
- Ordering is restricted to fields the inventory displays. An unrecognised ordering request is **ignored**, not rejected — a stale or hand-edited link degrades to the default ordering rather than failing.
- Page size is capped, so no caller can request an unbounded response.

## Ownership Fields Are Never Accepted

No request accepts an organization. Attempting to act on another organization's estate is not a validation failure because there is no field in which to express it — see [[Ownership Is Derived Not Supplied]].

## Client-Side Validation Is Not A Control

Any validation performed by a user interface is a convenience for the person typing. The platform re-validates everything, and its answer is the only one that counts.
