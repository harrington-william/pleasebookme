# ADR-0001: The hosted public booking page is not a widget

Status: Accepted (amended 2026-09-22 — see Amendment)  
Date: 2026-09-17

## Context

PleaseBookMe has two delivery surfaces: a first-party hosted booking page on the platform domain, and credentialed widget components embedded on a tenant's own site. Treating both as `widget.widgets` rows would force every tenant to create credentials and origin rules for a page that the platform itself hosts.

## Decision

The hosted page is keyed directly by `organization.slug` and exists by default at `/booking/{organizationSlug}` (originally `/{organizationSlug}`; see Amendment). It does not create a widget row, key pair, origin, widget principal, or bootstrap token. `WidgetType` therefore contains only `INLINE`, `POPUP`, and `EMBEDDED`.

The `/api/v1/public/**` namespace is unauthenticated by design. Visitors have no platform account, so identity and ownership are derived only from URL-resolved organization and service rows. Redis IP rate limiting and transactional host-lock slot revalidation protect the initial launch surface.

## Rejected alternative

A credential-less guest JWT was rejected. It authenticates no person or installation, adds issuance and expiry ceremony, and does not improve authorization over the URL path. Abuse controls belong at the anonymous edge instead.

## Consequences

- Every provisioned active/trial tenant immediately has a hosted booking URL.
- ~~Root application paths must be reserved against organization slugs.~~ Withdrawn by the Amendment: the `/booking` prefix removes the collision, so `ReservedOrganizationSlugs` no longer gates reads and protects brand names at creation time only.
- Embedded widgets retain their existing credentials, origins, and widget identity model.
- hCaptcha, blacklists, POST idempotency, customer upsert, notifications, attendee cancel/reschedule, and dashboard discoverability remain follow-up work.

## Amendment — 2026-09-22

**The decision is unchanged. The vocabulary and two paths are.**

The hosted page is now *called* the **full-page widget**: it is a booking surface, it does what every booking surface does, and the platform's other surfaces are widgets. Calling it "public booking" implied a separate product where there is one product with several delivery channels.

What this amendment does **not** change: the hosted page still creates no `widget.widgets` row, no key pair, no `widget_origins` entry, no `WidgetPrincipal` and no bootstrap token; `WidgetType` still contains only `INLINE`, `POPUP`, `EMBEDDED`; `/api/v1/public/**` is still `permitAll`; the guest-JWT alternative is still rejected. The title of this ADR reads oddly against the new name and is kept as-is because the ADR number is the stable identifier — read "is not a widget" as "is not a `widget.widgets` row".

What changed:

| | Before | After |
|---|---|---|
| Server package | `service/publicbooking` | `service/widget/fullpage` — one subpackage per booking channel under `service/widget/` |
| Classes | `PublicBookingController`, `PublicBookingService(Impl)`, `Public*` DTOs | `FullPageWidgetController`, `FullPageWidgetService(Impl)`, `Widget*` DTOs |
| Client route | `/{organizationSlug}` | `/booking/{organizationSlug}` |
| Server path | `/api/v1/public/**` | unchanged |
| Reserved slugs | 404'd by the resolver before any query | not checked on reads; `ReservedOrganizationSlugs` guards organization creation only |

**Why the prefix.** A root-level slug namespace forces every future marketing route (`/pricing`, `/blog`) to be added to a reserved list *and* makes that list capable of colliding with a slug a tenant already owns. The prefix removes the class of problem rather than managing it, and keeps `/booking/*`, `/dashboard/*` and a future `/embed/*` visibly separate. Vanity root URLs are a competitor's product shape, not this platform's: a tenant's primary surface is their own site with an embedded widget, and the hosted page is the default rather than the brand.
