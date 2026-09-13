# Workflow: Define a Resource Type

## Intent

Establish a category the organization will use to classify its assets.

## Preconditions

- The caller is authenticated and holds an accepted membership in exactly one organization.

## Steps

1. The caller supplies a name, and optionally a description and an icon.
2. The organization is derived from the caller's membership — it is never supplied.
3. The type is recorded against that organization.

## Postconditions

- A new [[Resource Type]] exists in the caller's organization.
- Resources may now be classified against it.

## Notes

This workflow is a **prerequisite for every resource**. A new organization has no types, and a resource cannot exist without one, so this is unavoidably the first thing an operator does — see [[Resource Lifecycle]].

Because the vocabulary is small and stable, this workflow is expected to run a handful of times during setup and then almost never again.

## Failure Modes

- A name already used in the organization is rejected, though currently as a raw storage failure rather than a considered conflict response — see [[Resource Failure Handling]].
- A caller with no accepted membership, or with memberships in more than one organization, cannot complete the workflow — see [[Resource Authorization Rules]].
