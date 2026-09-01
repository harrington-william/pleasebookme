# Workflow: Create a Resource

## Intent

Add one allocatable asset to the organization's inventory.

## Preconditions

- The caller is authenticated and holds an accepted membership in exactly one organization.
- At least one [[Resource Type]] already exists in that organization.

The second precondition is a hard gate, not a convenience. A resource must be classified, so an organization with an empty vocabulary cannot create anything until it defines a type first.

## Steps

1. The caller supplies a name, a slug, a type, and a [[Resource Status]]; optionally a description, a capacity, and the bookable/virtual flags.
2. The organization is derived from the caller's membership.
3. The slug is checked for uniqueness within that organization. A collision stops the workflow before anything is written.
4. The type is resolved.
5. The resource is recorded.

The uniqueness check precedes all other work deliberately: a rejected create should not have resolved relationships or done partial work first.

## Postconditions

- A new [[Resource]] exists, owned by the caller's organization, classified, and in a stated condition.
- The resource serves no services yet. Capability is recorded separately — see [[Assign a Resource to Services]].

## Notes

Ownership is not part of the request. A caller cannot create a resource in an organization they do not belong to, because there is no field through which to try — see [[Ownership Is Derived Not Supplied]].

Status must be stated explicitly; there is no default — see [[Resource Status Has No Default]].

## Failure Modes

- A slug already used in the organization is rejected as a conflict.
- An unknown type is rejected as not found.
- An unrecognised status value is rejected as a malformed request.
- Missing membership or ambiguous organization stops the workflow — see [[Resource Authorization Rules]].
