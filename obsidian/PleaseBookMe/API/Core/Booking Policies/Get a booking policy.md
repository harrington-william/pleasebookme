## Description

Retrieves a single booking policy by its numeric ID.

---

## Endpoint

```json
GET /api/v1/booking-policies/{bookingPolicyId}
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

## Successful Response

```json
200 OK
```

```json
{
	"bookingPolicyId": 0,
	"serviceId": 0,
	"bookingMode": "",
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
```

---

## Possible Errors

- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 BOOKING_POLICY_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "BOOKING_POLICY_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X GET \
<https://api.pleasebookme.com/api/v1/booking-policies/1> \
-H "Authorization: Bearer xxx"
```
