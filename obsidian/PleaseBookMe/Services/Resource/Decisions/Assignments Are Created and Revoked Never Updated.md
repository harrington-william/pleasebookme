# Decision: Assignments Are Created and Revoked Never Updated

## Context

A [[Resource Service Assignment]] records that an asset can serve a service. The pair itself is the identity — there is no separate identifier and no other attribute beyond when it was recorded.

## Decision

Expose creation and revocation. Do not expose an update.

## Rationale

**There is nothing to update.** The record consists of its two identifying halves plus an immutable timestamp. An "update" could only change one half — which is not an edit of this fact but the deletion of one fact and the creation of another.

Modelling that as an update would misdescribe it. "Room A serves Workshops" becoming "Room B serves Workshops" is not one capability changing; it is one capability ending and a different one beginning, each with its own moment of record.

Offering an operation with no legitimate meaning also invites misuse, and would have to be implemented as something — most likely the silent overwrite described below.

## The Overwrite Hazard

This is the part worth understanding, because it is not obvious.

When a record's identity is supplied by the caller rather than generated, a naive "save" of an already-existing identity **updates it silently instead of failing**. A duplicate assignment would therefore appear to succeed while quietly replacing the original, including its recorded timestamp — destroying the audit of when the capability was actually established, with no error anywhere.

An explicit existence check before creating is therefore **required, not defensive**. It is the only thing standing between a duplicate request and silent data loss, and it is covered by a regression test for exactly that reason.

## Consequences

- Duplicate assignment is a conflict, reported as one.
- Reassignment is expressed honestly as revoke-then-assign.
- The recorded timestamp is trustworthy, because nothing can overwrite it.

## Generalisation

This applies to any record on this platform whose identity is caller-supplied rather than generated. The absence of an existence check there is not a missing nicety — it is silent corruption.
