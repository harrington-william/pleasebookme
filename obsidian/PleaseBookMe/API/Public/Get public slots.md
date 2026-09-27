# Get public slots

## Endpoint

`GET /api/v1/public/{orgSlug}/service/{serviceSlug}/slots?date=YYYY-MM-DD`

## Authentication

None. Rate limit: 120 public reads per IP per minute by default.

## Response

```json
{
  "serviceId": 10,
  "date": "2026-09-24",
  "timezone": "Australia/Sydney",
  "slots": [
    {
      "slotStart": "2026-09-23T23:00:00Z",
      "slotEnd": "2026-09-23T23:30:00Z"
    }
  ]
}
```

The response is the shared `AvailableSlotsResponse`. Malformed dates return 400; unavailable path-owned data returns 404; exhausted limits return 429 with `Retry-After`.
