## Description

Retrieves the booking policy for a service by `serviceId`. A service can have at most one booking policy, so this endpoint returns an array with either one policy or no policies. A service without a booking policy returns `200 OK` with an empty array, not `404`.

This endpoint is a narrower filter on the existing `GET /api/v1/booking-policies` list behavior. It does not add ownership validation while the no-param list endpoint remains unscoped.

---

## Endpoint

```json
GET /api/v1/booking-policies?serviceId={serviceId}
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

## Body

```json
{}
```

## Successful Response

```json
200 OK
```

```json
[
	{
		"bookingPolicyId": 0,
		"serviceId": 0,
		"bookingMode": "FIXED",
		"defaultDuration": 0,
		"minimumDuration": 0,
		"maximumDuration": 0,
		"minimumNotice": 0,
		"maximumAdvanceBooking": 0,
		"slotInterval": 0,
		"beforeBuffer": 0,
		"afterBuffer": 0,
		"allowOverlap": false,
		"allowMultipleAttendee": false,
		"requiresPayment": false,
		"autoConfirm": false,
		"bookingWindowType": "",
		"capacity": 0,
		"createdAt": "",
		"updatedAt": ""
	}
]
```

A service with no booking policy returns:

```json
[]
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
	"code": "UNAUTHORIZED"
}
```

---

## Example Request

```bash
curl \
-X GET \
<https://api.pleasebookme.com/api/v1/booking-policies?serviceId=15> \
-H "Authorization: Bearer xxx"
```

---

## Event Produced

- None
