# Resource Failure Handling

How the service behaves when a request cannot be satisfied.

## Principles

1. **Reject before writing.** Uniqueness is checked before relationships are resolved or records built, so a rejected request leaves nothing behind.
2. **A failure must be recognisable as what it is.** A caller must be able to tell a malformed request from a missing record from a real conflict.
3. **Failures must never be mistakable for authentication failures.** This one was learned the hard way — see below.

## Failure Categories

| Situation | Nature of the failure |
|---|---|
| Slug already used in the organization | Conflict |
| Resource–service capability already recorded | Conflict |
| Type name already used in the organization | Conflict, but currently raised by the persistence layer rather than the service |
| Unknown resource, type, or service | Not found |
| Unrecognised status value | Malformed request |
| Missing required field | Malformed request |
| Unrecognised ordering field | **Not a failure** — ignored, and the default ordering is used |
| Caller has no accepted membership | Refused |
| Caller belongs to several organizations | Conflict — the request is ambiguous, not forbidden |
| Requested organization is not the caller's | **Not a failure** — an empty result |

## Two Deliberate Non-Failures

Two situations that look like errors are answered successfully, on purpose:

- **A foreign organization yields an empty result**, so the service cannot be used to discover which organizations exist — see [[Resource Authorization Rules]].
- **An unusable ordering request is ignored**, so a stale link degrades instead of breaking.

## Ambiguity Is Not Refusal

A caller who belongs to several organizations is not forbidden from acting — the request simply does not say which organization it concerns. Treating this as a refusal would misdescribe it. The resolution is for the request to name the organization, which is a capability the service is designed to gain without restructuring.

## The Masked-Failure Class

An earlier defect is worth recording, because the failure mode generalises well beyond this service.

An unhandled error inside the inventory read surfaced to callers as an **authentication failure with an empty body** rather than as a server error. Client software could not distinguish it from an expired session, so it responded the only way it knew how — by signing the user out. Every visit to the inventory logged the user out, while the underlying session was perfectly valid and the real fault was a query construction error.

Two lessons hold generally:

1. **An error that impersonates an authentication failure is worse than the error itself.** It sends every consumer down a recovery path that cannot possibly work, and it hides the real fault.
2. **Callers treat authentication failures as terminal.** Anything that can emit one must do so only when it is true.

The service now surfaces internal faults as internal faults. Any future work in this area should preserve that property, and treat "the user got signed out" as a signal to look for a masked error rather than a session problem.

## What Callers Should Not Infer

A successful write does **not** imply the asset is usable for booking. Condition is recorded but unenforced — see [[Resource Status]].
