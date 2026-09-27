# Get widget slots

## Description

Returns the slots currently offered for one service on one calendar date, delegating unchanged to the slot engine ([[Slot Service]]).

`date` is a calendar date **in the service's schedule timezone**, not the visitor's. The returned `slotStart`/`slotEnd` instants are UTC and may therefore fall on an adjacent calendar date — a 09:00 Sydney slot is `T23:00:00Z` the day before. Consumers must not filter the response by UTC date.

A visitor's local day can overlap two schedule-zone dates. The widget library handles that in the browser: it computes the one or two schedule dates the visitor's day touches, calls this endpoint once per date, merges the responses and keeps the instants that belong to the chosen local day.

An empty `slots` array is a normal `200`: a closed weekday, a fully booked day and a schedule with no availability rows all look the same.

---

## Endpoint

```json
GET /api/v1/widget/services/{serviceSlug}/slots?date=YYYY-MM-DD
```

---

## Authentication

Required **Bearer Token** (widget)

---

## Path Parameters

| Parameter | Required | Description |
|---|---|---|
| `serviceSlug` | Yes | Slug of a service belonging to the widget's organization. |

## Query Parameters

| Parameter | Required | Description |
|---|---|---|
| `date` | Yes | `YYYY-MM-DD`, interpreted in the service's `scheduleTimezone`. |

## Successful Response

```json
200 OK
```

```json
{
	"serviceId": 0,
	"date": "",
	"timezone": "",
	"slots": [
		{
			"slotStart": "",
			"slotEnd": ""
		}
	]
}
```

---

## Possible Errors

- 400 INVALID_REQUEST — malformed `date`
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Example Request

```bash
curl \
-X GET \
"<https://api.pleasebookme.app/api/v1/widget/services/consultant-meeting/slots?date=2026-09-24>" \
-H "Authorization: Bearer xxx"
```
