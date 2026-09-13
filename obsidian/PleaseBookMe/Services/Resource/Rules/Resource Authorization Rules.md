# Resource Authorization Rules

Who may act, and on what.

> **Read this section fully before assuming the service is protected.** Authorization here is partial by explicit decision, and the gaps are recorded rather than implied.

## The Caller's Organization

Every operation resolves the caller to a single organization by way of an **accepted** membership.

Two conditions stop the resolution:

- **No accepted membership.** The caller is authenticated but is not entitled to act anywhere. An unaccepted invitation is not membership.
- **Memberships in more than one organization.** The service refuses to guess which one the request concerns, rather than silently picking one.

The acceptance requirement matters: it is what stops an unaccepted invitation from behaving like a real membership.

## Scope Mismatch Returns Nothing, Not An Error

When a caller asks for an organization that is not their own, the collection views and the estate summary return an **empty result** rather than a refusal.

This is deliberate. A refusal distinguishable from "no results" would let a caller probe which organization identifiers exist and roughly how large each estate is. An empty answer is indistinguishable between "not yours" and "yours, but empty", which is exactly the property wanted.

## What Is Protected Today

| Operation | Scoped to caller's organization? |
|---|---|
| Create a resource or type | Yes — ownership is derived, not supplied |
| Update a resource or type | Yes — a foreign record reports as not found |
| List resources or types | Yes — foreign organization yields an empty result |
| Estate summary | Yes — foreign organization yields zero counts |
| Read one resource or type by identifier | **No** |
| Delete a resource or type | **No** |
| Any assignment operation | **No** |

## Known Gaps

These are accepted, deliberate, and deferred — not oversights:

1. **Single-record read and delete are unscoped.** Any authenticated caller who knows or guesses an identifier can read or delete another organization's resource. Because identifiers are sequential, guessing is not difficult. Deleting a *type* this way destroys every resource classified by it.
2. **Assignment operations are unscoped**, so capabilities can be recorded or revoked across organization boundaries.
3. **A resource's type is not checked** to belong to the caller's organization.
4. **No permission model is enforced.** Permission vocabulary exists for this domain, but no operation consults it. "Authenticated" is the only real gate; there is no notion of a member who may read but not delete.

Closing these is a coherent piece of work in its own right and should be done across the service at once rather than endpoint by endpoint.
