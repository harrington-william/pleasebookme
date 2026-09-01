# Workflow: Assign a Resource to Services

## Intent

Record that an asset is capable of serving a particular bookable service.

## Preconditions

- Both the [[Resource]] and the bookable service already exist.

## Steps

1. The caller names a resource and a service.
2. The pair is checked for prior existence. An already-recorded capability is rejected.
3. Both sides are resolved.
4. The capability is recorded, along with when it was recorded.

The existence check precedes resolution deliberately, and its absence would be more damaging than it looks: because the pair itself is the identity, re-recording an existing pair would silently overwrite rather than fail. See [[Assignments Are Created and Revoked Never Updated]].

## Revoking

A capability is revoked by removing the pair. There is no edit operation — reassigning a service from one asset to another is a revoke followed by an assign, because that is what actually happened.

## Postconditions

- The resource is listed among those able to serve the service, and vice versa.
- No schedule or booking is affected. This records capability only.

## Notes

Assignments are also removed implicitly when either side is deleted, since a capability statement about a deleted asset has no meaning.

## Failure Modes

- An already-recorded pair is rejected as a conflict.
- An unknown resource or unknown service is rejected as not found.
