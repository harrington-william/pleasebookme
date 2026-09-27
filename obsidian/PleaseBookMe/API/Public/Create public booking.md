# Create public booking

## Endpoint

`POST /api/v1/public/{orgSlug}/service/{serviceSlug}/bookings`

## Authentication

None. Rate limit: 5 public writes per IP per minute by default.

## Request

```json
{
  "name": "Jane Doe",
  "phone": "+61400000000",
  "email": "jane@example.com",
  "timezone": "America/New_York",
  "slotStart": "2026-09-23T23:00:00Z",
  "notes": "Window seat"
}
```

`name`, `phone`, and `slotStart` are required. Email and timezone are nullable. The server derives all ownership, policy, duration, location, and status fields from the URL-resolved rows.

## Response

`201 Created`

```json
{
  "bookingUid": "05282076-b69d-405c-8d23-470e00c5101f",
  "status": "ACCEPTED",
  "startTime": "2026-09-23T23:00:00Z",
  "endTime": "2026-09-23T23:30:00Z",
  "timezone": "Australia/Sydney",
  "serviceTitle": "Consultation",
  "organizationName": "Acme Studio"
}
```

The host is locked and the exact slot is regenerated before either row is inserted. A stale slot returns 409. A sixth write inside the window returns 429 and `Retry-After`.
