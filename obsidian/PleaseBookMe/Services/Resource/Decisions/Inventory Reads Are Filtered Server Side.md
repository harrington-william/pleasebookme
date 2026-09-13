# Decision: Inventory Reads Are Filtered Server Side

## Context

The inventory needs filtering by type and condition, text search, ordering, and paging.

## Decision

Resolve all of it in the platform. Never fetch an estate and narrow it afterwards.

## Rationale

Filtering after fetching is correct only while an estate is small, and it fails in the direction that matters: it works perfectly in testing, then degrades as a customer's inventory grows. A filter that quietly examines only the first page is worse than no filter, because it returns a confident, wrong answer.

Paging has the same property. A view that fetches everything and slices locally is not paged; it has a paging-shaped control attached to an unpaged fetch, and it collapses at exactly the scale paging exists to handle.

## Consequences

- The platform is the only place that decides what matches. A consumer cannot widen the result by asking differently.
- Response size is bounded by a maximum page size, so no caller can request an unbounded response.
- Filter state lives in the request rather than in a consumer's memory, which makes a filtered view addressable and shareable.

## Two Non-Obvious Consequences

**Ordering must be restricted to known fields.** Ordering is caller-supplied and reaches the query directly, so an unrestricted field name either fails the request or exposes ordering by data the inventory does not display. Restricting it to displayed fields, and *ignoring* anything else rather than rejecting it, keeps a stale link working instead of breaking it.

**Search text must be treated as literal.** Pattern characters carry meaning to the underlying query. Left unescaped, a search containing one silently widens to match far more than intended — a caller searching for a term with a wildcard character in it would get everything back and have no way to tell why.

## Rejected Alternative

**Fetch the estate once and filter in the consumer.** Simpler, and it is what the first implementation did. It was removed because both failure modes above are invisible until an estate is large enough to matter, at which point they present as "search is wrong" rather than as a design fault.
