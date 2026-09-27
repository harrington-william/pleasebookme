# Full Page Widget API Summary

*(Formerly "Public Booking API Summary". The `/api/v1/public` wire contract is unchanged; only the server package and the client route were renamed — see [[Full Page Widget Service]].)*

The public booking API serves the hosted booking page without a bearer token. It is rate-limited by client IP on Spring and exposes no tenant, organization, host, schedule, or profile identifiers.

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/v1/public/{orgSlug}` | Public organization and bookable services |
| `GET` | `/api/v1/public/{orgSlug}/service/{serviceSlug}` | Public service, policy presentation, and calendar hints |
| `GET` | `/api/v1/public/{orgSlug}/service/{serviceSlug}/slots?date=YYYY-MM-DD` | Generated slots for a schedule-zone date |
| `POST` | `/api/v1/public/{orgSlug}/service/{serviceSlug}/bookings` | Revalidate and create a booking |

All endpoints return 404 for unavailable path-owned data. The POST can additionally return 409 when a slot was taken and 429 with `Retry-After` when the write bucket is exhausted.
