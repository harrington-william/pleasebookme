## Description

Retrieves every booking policy.

---

## Endpoint

```json
GET /api/v1/booking-policies
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
[
	{
		"bookingPolicyId": 0,
		"serviceId": 0,
		"bookingMode": "",
		"durationType": "",
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

---

## Possible Errors

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
<https://api.pleasebookme.com/api/v1/booking-policies> \
-H "Authorization: Bearer xxx"
```
