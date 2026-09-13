# Decision: Resource Status Has No Default

## Context

Most comparable fields on this platform carry a default. Condition does not: creating a [[Resource]] requires stating one.

## Decision

Require the condition explicitly. Do not infer it.

## Rationale

The plausible default is `ACTIVE`, and that is precisely the problem. Defaulting an unstated condition to `ACTIVE` means an asset whose readiness **nobody asserted** enters the estate as ready to use.

The failure is silent and asymmetric. Forgetting to state a condition would produce an asset that looks available; the mistake surfaces later, as a booking against something that was never ready. Requiring the field surfaces the same mistake immediately, at the point where the person creating the record can answer the question.

Condition is also the field most likely to be wrong when unstated. Assets are frequently entered into an inventory *before* they are ready — ordered but not delivered, delivered but not installed. `ACTIVE` is often the wrong answer at creation time, not merely an unverified one.

## Consequences

- Every creation request must state a condition.
- Omitting it is a malformed request, not a silently defaulted one.
- The absence of a default is deliberate and should not be "fixed" for consistency with sibling fields. A future change adding one would reintroduce exactly the silent failure described above.

## Related

Condition is currently recorded but not enforced — see [[Resource Status]]. That does not weaken this decision: an unenforced field that is *accurate* can be enforced later, whereas one silently populated with guesses cannot be trusted when the enforcement arrives.
