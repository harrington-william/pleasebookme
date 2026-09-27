# Embedded Widget Service

## Purpose

`service/widget/embedded` is the channel adapter that lets a widget embedded on a tenant's own website reach the shared [[Barbershop Booking Service]]. It contributes exactly one thing of its own: deciding which organization a widget's token is allowed to see.

## Boundary

- Base path: `/api/v1/widget`
- Authentication: Bearer, actor type `WIDGET`, from [[Bootstrap widget]]
- Organization identity: `WidgetPrincipal.tenantId()` — never the path, query or body
- Served tenants: `ACTIVE` and `TRIAL`
- Rate limiting: the same Redis IP buckets as the hosted page

## Scoping, by construction

`EmbeddedWidgetOrganizationResolver` is the only place this channel decides what is served:

1. `CurrentPrincipalProvider.requireWidget()` — `401` with no principal, `403` for a user principal.
2. `WidgetPrincipal.isActive()` — `401` (`WidgetNotActiveException`) otherwise, so revoking a widget stops its existing token at the next request rather than at expiry.
3. Load the tenant by the principal's tenant id, apply the served-status gate, return its organization and ecosystem code.

No endpoint accepts a tenant, organization, user or widget identifier, so cross-tenant access is not a check that could be forgotten — there is no input that could express it. The `404` message contains no tenant or widget identifier.

This channel deliberately does **not** invoke the authorization engine, so the active-status check it makes directly is the one `ActorStatusPolicy` would otherwise make. See `SECURITY.md` → "Embedded widget channel" for the full deferred list.

## Transactions

The three read operations are `@Transactional(readOnly = true)`. `createBooking` is deliberately **not** annotated on this service, so the resolver runs before the flow opens its write transaction and the host lock is taken inside that transaction rather than held across the resolve.

Consequence to know: the resolver therefore reads lazy `tenant` associations outside a transaction of its own and depends on Spring's `open-in-view` default. Disabling `spring.jpa.open-in-view` would break `createBooking` on this channel and on the hosted page — see `SERVER_AGENTS.md` → "Widget channel".

## Operations

| Operation | Effect |
|---|---|
| Get organization | Resolve from the token, then delegate. |
| Get service | Resolve, then delegate with the service slug. |
| Get slots | Resolve, then delegate with the slug and date. |
| Create booking | Resolve, then delegate; the flow owns the lock and the write. |
