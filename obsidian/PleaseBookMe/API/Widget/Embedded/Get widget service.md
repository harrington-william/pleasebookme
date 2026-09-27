# Get widget service

## Description

Returns one bookable service of the calling widget's organization, with the policy values the booking UI needs and the weekdays its schedule has availability on.

`scheduleTimezone` is the zone the slot engine interprets dates in, and is not necessarily the service's display timezone. `availableWeekdays` is the ISO 1–7 union of the schedule's availability days; it is a calendar hint only — slot generation remains authoritative.

A service without a booking policy is not bookable and returns `404`.

---

## Endpoint

```json
GET /api/v1/widget/services/{serviceSlug}
```

---

## Authentication

Required **Bearer Token** (widget)

---

## Path Parameters

| Parameter | Required | Description |
|---|---|---|
| `serviceSlug` | Yes | Slug of a service belonging to the widget's organization. |

## Successful Response

```json
200 OK
```

```json
{
	"organization": {
		"name": "",
		"slug": "",
		"logoUrl": "",
		"timezone": "",
		"weekStart": ""
	},
	"slug": "",
	"title": "",
	"description": "",
	"location": "",
	"durationMinutes": 0,
	"minPrice": 0,
	"maxPrice": 0,
	"currency": "",
	"scheduleTimezone": "",
	"minimumNotice": 0,
	"maximumAdvanceBooking": 0,
	"autoConfirm": true,
	"successRedirectUrl": "",
	"availableWeekdays": [1]
}
```

---

## Possible Errors

- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 NOT_FOUND — unknown service, or a service with no booking policy
- 429 RATE_LIMIT_EXCEEDED

---

## Example Request

```bash
curl \
-X GET \
<https://api.pleasebookme.app/api/v1/widget/services/consultant-meeting> \
-H "Authorization: Bearer xxx"
```
