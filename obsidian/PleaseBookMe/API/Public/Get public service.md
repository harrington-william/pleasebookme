# Get public service

## Endpoint

`GET /api/v1/public/{orgSlug}/service/{serviceSlug}`

## Authentication

None. Rate limit: 120 public reads per IP per minute by default.

## Response

`200 OK` returns organization display fields, service title/description/location/prices, `durationMinutes`, `scheduleTimezone`, booking-window minutes, `autoConfirm`, nullable `successRedirectUrl`, and `availableWeekdays` as sorted distinct ISO weekday numbers (`1` Monday through `7` Sunday).

`scheduleTimezone` is authoritative for the slot endpoint's `date`. `availableWeekdays` is a calendar hint, not a promise that slots exist. Unknown services and services without a booking policy return `404 ApiErrorResponse`.
