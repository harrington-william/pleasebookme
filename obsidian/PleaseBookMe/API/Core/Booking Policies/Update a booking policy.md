## Description

Full-replaces an existing booking policy, including re-resolving `serviceId` against `service`. `bookingMode`, `defaultDuration`, `slotInterval`, `beforeBuffer`, `afterBuffer`, `allowOverlap`, `allowMultipleAttendee`, `requiresPayment`, and `autoConfirm` are only overwritten when supplied; a missing field keeps its current stored value.

---

## Endpoint

```json
PUT /api/v1/booking-policies/{bookingPolicyId}
```

---

## Authentication

Required **Bearer Token**

---

## Headers

```json
{
	"Authorization": "JWT Access Token",
	"Content-Type": "application/json"
}
```

## Body

```json
{
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
	"capacity": 0
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
```

---

## Possible Errors

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 BOOKING_POLICY_NOT_FOUND
- 404 SERVICE_NOT_FOUND
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
-X PUT \
<https://api.pleasebookme.com/api/v1/booking-policies/1> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "serviceId": 1,
	"bookingMode": "FIXED",
	"durationType": "FIXED",
	"defaultDuration": 1,
	"minimumDuration": null,
	"maximumDuration": null,
	"minimumNotice": 120,
	"maximumAdvanceBooking": 43200,
	"slotInterval": 30,
	"beforeBuffer": 0,
	"afterBuffer": 0,
	"allowOverlap": false,
	"allowMultipleAttendee": false,
	"requiresPayment": false,
	"autoConfirm": true,
	"bookingWindowType": "ROLLING",
	"capacity": 1
}'
```

---

## Event Produced

- BookingPolicyUpdated
