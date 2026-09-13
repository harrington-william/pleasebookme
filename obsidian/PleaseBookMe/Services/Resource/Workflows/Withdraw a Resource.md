# Workflow: Withdraw a Resource

## Intent

Take an asset out of circulation — either temporarily, permanently, or completely.

## Three Different Withdrawals

The service distinguishes three, and they are not interchangeable:

| Action | Effect | History |
|---|---|---|
| Set condition to `INACTIVE` or `MAINTENANCE` | Temporarily out of service | Fully preserved |
| Set condition to `RETIRED` | Permanently out of service | Fully preserved |
| Delete | Ceases to exist | Destroyed, along with its assignments |

Retirement is almost always the right choice for an asset that has genuinely gone away. Deletion is for records that should never have existed — a mistyped duplicate, a test entry.

## Steps For Retirement

1. The caller updates the resource with a condition of `RETIRED`.
2. The resource remains listed, remains assigned, and remains countable in the estate summary under its new condition.

## Steps For Deletion

1. The caller deletes the resource.
2. Its service assignments are removed with it.

## Postconditions

- After retirement: the resource still exists and still reports its relationships.
- After deletion: the resource and its assignments are gone.

## Important Caveat

Neither retirement nor the bookable flag currently prevents the asset from being allocated, because no consumer reads either — see [[Resource Status]]. Until a booking layer honours condition, withdrawal is a bookkeeping statement rather than an enforced constraint.

## Related

Deleting a [[Resource Type]] deletes every resource classified by it. That is a far broader withdrawal than deleting a single resource, and it is easy to trigger by accident.
