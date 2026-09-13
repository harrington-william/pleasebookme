# Workflow: Browse the Resource Inventory

## Intent

Answer operational questions about the estate: what do we have, what condition is it in, what does each asset serve.

## Preconditions

- The caller is authenticated and holds an accepted membership in an organization.

## Steps

1. The caller requests the inventory for an organization, optionally narrowing by type, by [[Resource Status]], or by a text search over name and slug.
2. The requested organization is checked against the caller's own. A mismatch yields an empty result rather than an error — see [[Resource Authorization Rules]].
3. A single page of matching resources is returned, together with the total count.
4. Separately, the estate is summarised into counts by condition.

## Narrowing The Result

All filters are optional and combine conjunctively — a request for "rooms under maintenance" applies both constraints. Filtering, searching, ordering and paging are all resolved by the platform, never by the caller after the fact; the reasoning is in [[Inventory Reads Are Filtered Server Side]].

Ordering is restricted to fields the inventory actually displays. A request to order by anything else is ignored rather than rejected, so a hand-edited or stale link degrades to a sensible default instead of failing.

Page size is capped. A caller cannot request the entire estate in one response.

## Postconditions

- No state changes. This workflow is purely a read.

## Summary Counts

The summary reports how many resources exist and how many are in each condition. It answers "how much of the estate is available" and "how much is out of service" at a glance.

It deliberately does **not** report utilization — see [[Utilization Is Not Reported]].

## Notes

Assignments are resolved for the whole organization in one operation and matched to the listed resources, rather than asked for one resource at a time. This is a deliberate property of the workflow, not an incidental optimisation: the per-resource shape made the cost of viewing the inventory grow with the size of the estate.
