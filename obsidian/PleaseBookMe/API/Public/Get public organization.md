# Get public organization

## Endpoint

`GET /api/v1/public/{orgSlug}`

## Authentication

None. Rate limit: 120 public reads per IP per minute by default.

## Response

`200 OK`

```json
{
  "name": "Acme Studio",
  "slug": "acme-studio",
  "logoUrl": null,
  "bannerUrl": null,
  "bio": "Appointments by request",
  "timezone": "Australia/Sydney",
  "weekStart": "MONDAY",
  "services": [
    {
      "slug": "consultation",
      "title": "Consultation",
      "description": null,
      "location": null,
      "durationMinutes": 30,
      "minPrice": null,
      "maxPrice": null,
      "currency": "AUD",
      "autoConfirm": true
    }
  ]
}
```

Only services with a booking policy are listed. Unknown, reserved, or unserved organizations return `404 ApiErrorResponse`.
