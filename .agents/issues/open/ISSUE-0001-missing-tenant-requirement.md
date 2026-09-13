# Tenant Requirement

## Every user IS a tenant. No exceptions, no edge cases.

PleaseBookMe is Calendly for a broader set of service businesses — salons,
rentals, courts, equipment hire. The entire interface is built for **tenant
admins** who manage a service catalog and their bookings. There is no
consumer-side account: attendees book through a widget or public page without
ever holding a platform account.

Two consequences, which override anything written elsewhere in the repo:

1. **A `UserPrincipal` with no tenant is a data-integrity error, not a valid
   state.** It should be treated exactly the way `DefaultUserIdentityLoader`
   already treats a missing Membership or Profile — `orElseThrow`, never
   `orElse(null)`.
2. **The product is unusable without Google Calendar access.** Calendar sync is
   not an optional add-on; it is core to the value proposition, the same way it
   is for Calendly. Onboarding should treat connecting Google as part of signup,
   not as an afterthought in settings.

### ⚠ This contradicts SECURITY.md, which is now stale

`SECURITY.md` → **"Tenant Optionality"** currently states that
`UserPrincipal.tenantUid` is nullable and that "a user without one simply hasn't
subscribed to a plan yet, which is an expected, common state, not an error."
**That is no longer the product rule.**

It has deliberately not been rewritten yet, because the *code* it describes has
not changed either — and right now the stale doc accurately describes the stale
code. The two must be corrected together:

- `DefaultUserIdentityLoader.build()` —
  `tenantRepository.findByOrganizationOrganizationId(...).orElse(null)`
- `UserPrincipal.tenantUid()` — documented as nullable throughout SECURITY.md §5

**Do not flip these to non-null before provisioning actually creates tenants**,
or every existing account — all of which have no tenant row — fails to
authenticate at the loader.

## ⚠ The Google one-shot flow now depends on this, and it is already built

The **Google one-shot registration flow** (server: `docs/services/auth/GOOGLE_ONE_SHOT_BACKEND.md`,
frontend plan: `client/features/auth/GOOGLE_ONE_SHOT_FLOW.md`) provisions an
account from inside the OAuth callback. It inherits whatever provisioning does
or fails to do — including every gap listed above.

The earlier note here said "do not build that flow before tenant provisioning
exists". It has since been built anyway, deliberately: it routes through the
**same** `UserProvisioningService` via a shared `GoogleAccountResolver` rather
than duplicating provisioning, so it is one code path, not two. Fixing
provisioning still fixes both.

What that means in practice: **every blocker above now breaks registration, not
just calendar sync.** A `NOT NULL` violation surfacing from inside the callback
presents as a `302 …?google=error` and a stack trace in the server log, several
frames from the actual cause. Verify provisioning through the plain
`POST /api/v1/auth/register` endpoint first, where the failure is legible.