# Resource Business Rules

Rules that hold regardless of how the service is called.

## Ownership

1. Every [[Resource]] and every [[Resource Type]] belongs to exactly one organization.
2. Ownership is derived from the caller's accepted membership, never accepted as input — see [[Ownership Is Derived Not Supplied]].
3. Ownership is fixed at creation. An update cannot move an asset between organizations.

## Identity And Uniqueness

4. A resource's slug is unique within its organization, and only within it. Two organizations may each own the same slug.
5. A resource type's name is unique within its organization.
6. A resource–service capability pair is unique. The same capability cannot be recorded twice.

## Classification

7. Every resource has exactly one type. There is no uncategorised state.
8. A type may classify any number of resources, including none.
9. Deleting a type deletes every resource it classifies.

## Capability

10. A resource may serve zero or more bookable services.
11. A service may be served by zero or more resources.
12. Capability is recorded and revoked, never edited — see [[Assignments Are Created and Revoked Never Updated]].
13. Deleting either side of a capability removes it.

## Condition

14. Every resource is in exactly one [[Resource Status]] at all times.
15. Condition must be stated at creation; there is no default — see [[Resource Status Has No Default]].
16. Any condition may transition to any other. No transition is forbidden.
17. **Condition is advisory.** Nothing in the platform currently prevents an asset in any condition from being allocated.

## Descriptive Data

18. A resource's capacity is optional, because capacity is meaningless for many asset classes. Absent capacity means "not applicable", never zero.
19. A resource's description and a type's icon are optional. Neither carries meaning that should block cataloguing an estate.

## Known Gaps

These are true of the service today and are recorded so they are not mistaken for guarantees:

- A resource's type is not verified to belong to the same organization as the resource.
- Rule 5 is enforced only by the persistence layer, so a violation surfaces as a storage error rather than a considered response.
- Rule 17 means status and the bookable flag are, in practice, documentation rather than control.
