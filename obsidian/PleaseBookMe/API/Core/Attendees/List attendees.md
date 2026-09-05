## Description

Retrieves attendees scoped either to a single booking or to a whole organization.

This endpoint replaced the former unscoped "get all attendees" listing. Exactly one of `bookingId` or `organizationId` must be supplied — a request with neither returns `400`, and there is no way to retrieve every attendee on the platform.

- `bookingId` returns the attendees attached to that one booking. This is the shape a booking detail view uses.
- `organizationId` returns every attendee across the organization's bookings, resolved through `attendee → booking → service → organization` since `core.attendees` carries no direct organization column. This exists so a list view can fetch attendee names for a whole page of bookings in **one** request instead of one request per row.

When both are supplied, `bookingId` wins.

Attendees are a **per-booking contact snapshot**, not a CRM record. An attendee row captures the contact details as given at booking time and does not change if the person's `customer.customers` profile is later updated. The two are deliberately separate concepts and there is no foreign key between them, so this endpoint cannot be used to look up a customer's booking history.

> **Scaling note.** The `organizationId` form is unpaginated and grows with the organization's total booking count. It is sized for the current dashboard, not for bulk export.

---

## Endpoint

```json
GET /api/v1/attendees
```

---

## Authentication

Required **Bearer Token**

---

## Headers

```json
{
	"Authorization": "JWT Access Token"
}
```

## Query Parameters

| Parameter | Required | Description |
|---|---|---|
| `bookingId` | Conditional | Attendees on this booking. Takes precedence over `organizationId`. |
| `organizationId` | Conditional | Every attendee across this organization's bookings. |

One of the two is required.

## Successful Response

```json
200 OK
```

```json
[
	{
		"attendeeId": 0,
		"bookingId": 0,
		"email": "",
		"phone": "",
		"name": "",
		"locale": "",
		"timezone": "",
		"noShow": false,
		"createdAt": ""
	}
]
```

---

## Possible Errors

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "INVALID_REQUEST"
}
```

---

## Example Request

```bash
curl \
-X GET \
<https://api.pleasebookme.com/api/v1/attendees?bookingId=1> \
-H "Authorization: Bearer xxx"
```
