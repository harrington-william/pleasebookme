# Embedded Widget API Summary

The embedded widget API serves widgets pasted into a tenant's own website. It is the Bearer-authenticated sibling of the [[Full Page Widget API Summary|full-page widget API]]: same booking flow, same response shapes, different way of deciding whose data is served.

## Base

```json
/api/v1/widget
```

## Authentication

Required **Bearer Token**, minted by [[Bootstrap widget]] from the widget's public/secret key pair. The token's actor type must be `WIDGET`; a user's access token is rejected with `403`.

## Scoping

No endpoint accepts a tenant, organization, user or widget identifier. `EmbeddedWidgetOrganizationResolver` reads `WidgetPrincipal.tenantId()` from the token, loads that tenant, and serves its organization. A widget cannot request another tenant's data because no input expresses it.

Only `ACTIVE` and `TRIAL` tenants are served. A suspended tenant, a missing tenant and an unknown service all return the same `404`, with no tenant or widget identifier in the message.

## Endpoints

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/organization` | The widget's organization plus its bookable services. See [[Get widget organization]]. |
| `GET` | `/services/{serviceSlug}` | One bookable service with its policy and availability weekdays. See [[Get widget service]]. |
| `GET` | `/services/{serviceSlug}/slots?date=` | Offered slots for one calendar date. See [[Get widget slots]]. |
| `POST` | `/services/{serviceSlug}/bookings` | Create a booking and its attendee. See [[Create widget booking]]. |

## Shared behaviour

- **Rate limiting.** Redis fixed windows per client IP, shared with the public channel: 120 reads/minute, 5 writes/minute. Exhaustion returns `429` with `Retry-After`. A Redis failure fails closed as a `500`.
- **Active widget required on every call.** A widget revoked or disabled after its token was minted is refused at its next request with `401`, not at token expiry.
- **Responses expose no internal identifiers** — no tenant, organization, host, schedule or profile ids.
- **Bookable means policy-backed.** A service without a `core.booking_policies` row is omitted from the organization response and `404`s on direct access.

## Errors

- 401 UNAUTHORIZED — missing, malformed or expired token; widget not active; widget expired
- 403 FORBIDDEN — a non-widget actor (for example a user's access token)
- 404 NOT_FOUND — tenant not served, unknown service, service without a booking policy
- 409 SLOT_UNAVAILABLE — the requested slot is no longer offered
- 429 RATE_LIMIT_EXCEEDED — with `Retry-After`

## Not enforced here

The authorization engine is not invoked on this channel; `WidgetCapabilityPolicy` and `WidgetTenantIsolationPolicy` are unreached. The capability surface is these four endpoints by construction. See `SECURITY.md` → "Embedded widget channel".
