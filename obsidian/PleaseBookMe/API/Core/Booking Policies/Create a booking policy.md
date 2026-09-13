## Description

Creates a booking policy for an existing service. `bookingMode`, `defaultDuration`, `slotInterval`, `beforeBuffer`, `afterBuffer`, `allowOverlap`, `allowMultipleAttendee`, `requiresPayment`, and `autoConfirm` fall back to their platform defaults (`FIXED`, `1`, `30`, `0`, `0`, `false`, `false`, `false`, `true`) when omitted. `serviceId` is unique at the database level and pre-checked by the service layer — a service may have at most one booking policy, and creating a second returns a typed `409`.

---

## Endpoint

```json
POST /api/v1/booking-policies
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
201 Created
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

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 SERVICE_NOT_FOUND
- 409 BOOKING_POLICY_ALREADY_EXISTS
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"status": "error",
	"message": "Booking policy already exists for service: 1",
	"data": null,
	"client": "127.0.0.1",
	"timestamp": "2026-08-29T00:00:00Z",
	"path": "/api/v1/booking-policies"
}
```

---

## Example Request

```bash
curl \
-X POST \
<https://api.pleasebookme.com/api/v1/booking-policies> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "serviceId": 1,
	"bookingMode": "FIXED",
	"defaultDuration": 1,
	"minimumDuration": null,
	"maximumDuration": null,
	"minimumNotice": 60,
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

- BookingPolicyCreated
