# Create widget booking

## Description

Creates a booking and its attendee for the calling widget's organization.

The slot is re-validated inside the write transaction: the endpoint takes a pessimistic lock on the host user, re-runs slot generation for the requested date, and writes only if the exact `[slotStart, slotStart + durationMinutes)` slot is still offered. Otherwise it returns `409`. The host lock is deliberately host-wide, so two visitors booking different services of the same host serialize.

The booking's `status` is `ACCEPTED` when the service's policy auto-confirms, and `AWAITING_HOST` otherwise. Its title is `"<service title> with <attendee name>"`.

`phone` is required and `email` is optional, mirroring `core.attendees`. No customer record, notification, or selected-slot hold is created.

---

## Endpoint

```json
POST /api/v1/widget/services/{serviceSlug}/bookings
```

---

## Authentication

Required **Bearer Token** (widget)

---

## Headers

```json
{
	"Authorization": "JWT Access Token",
	"Content-Type": "application/json"
}
```

## Path Parameters

| Parameter | Required | Description |
|---|---|---|
| `serviceSlug` | Yes | Slug of a service belonging to the widget's organization. |

## Body

```json
{
	"name": "",
	"phone": "",
	"email": "",
	"timezone": "",
	"slotStart": "",
	"notes": ""
}
```

| Field | Required | Description |
|---|---|---|
| `name` | Yes | Attendee name, max 255. |
| `phone` | Yes | Attendee phone, max 50. |
| `email` | No | Attendee email, max 255. |
| `timezone` | No | The visitor's display timezone, max 100. |
| `slotStart` | Yes | Instant that must equal a generated slot start. |
| `notes` | No | Free text; stored as the booking description. |

## Successful Response

```json
201 Created
```

```json
{
	"bookingUid": "",
	"status": "",
	"startTime": "",
	"endTime": "",
	"timezone": "",
	"serviceTitle": "",
	"organizationName": ""
}
```

---

## Possible Errors

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 NOT_FOUND
- 409 SLOT_UNAVAILABLE — "That time is no longer available"
- 429 RATE_LIMIT_EXCEEDED

---

## Example Request

```bash
curl \
-X POST \
<https://api.pleasebookme.app/api/v1/widget/services/consultant-meeting/bookings> \
-H "Authorization: Bearer xxx" \
-H "Content-Type: application/json" \
-d '{
    "name": "Jane Doe",
    "phone": "+61400000000",
    "email": "jane@example.com",
    "timezone": "Australia/Sydney",
    "slotStart": "2026-09-23T23:30:00Z",
    "notes": ""
}'
```
