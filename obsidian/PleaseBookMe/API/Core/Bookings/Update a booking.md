## Description

Full-replaces an existing booking. The caller is expected to send the complete resource. Unlike create, `userId` and `serviceId` are re-resolved and re-validated on every update — reassigning either is a supported mutation. `cancelledById`, `rescheduledById`, and `deletedById` are re-resolved when supplied and cleared when omitted. `destinationCalendarId`/`destinationSheetsId` are likewise cleared (set to `null`) when omitted, rather than left untouched.

---

## Endpoint

```json
PUT /api/v1/bookings/{bookingId}
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
	"idempotencyKey": "",
	"userId": 0,
	"title": "",
	"description": "",
	"startTime": "",
	"endTime": "",
	"serviceId": 0,
	"location": "",
	"status": "",
	"paid": false,
	"cancelledById": null,
	"cancellationReason": "",
	"rejectionReason": "",
	"rescheduled": false,
	"rescheduledById": null,
	"noShowHost": false,
	"deletedAt": null,
	"deletedById": null,
	"destinationCalendarId": null,
	"destinationSheetsId": null
}
```

## Successful Response

```json
200 OK
```

```json
{
	"bookingId": 0,
	"bookingUid": "",
	"idempotencyKey": "",
	"userId": 0,
	"title": "",
	"description": "",
	"startTime": "",
	"endTime": "",
	"serviceId": 0,
	"location": "",
	"status": "",
	"paid": false,
	"cancelledById": null,
	"cancellationReason": "",
	"rejectionReason": "",
	"rescheduled": false,
	"rescheduledById": null,
	"noShowHost": false,
	"deletedAt": null,
	"deletedById": null,
	"destinationCalendarId": null,
	"destinationSheetsId": null,
	"createdAt": "",
	"updatedAt": ""
}
```

---

## Possible Errors

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 BOOKING_NOT_FOUND
- 404 USER_NOT_FOUND
- 404 SERVICE_NOT_FOUND
- 404 DESTINATION_CALENDAR_NOT_FOUND
- 404 DESTINATION_SHEETS_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "BOOKING_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X PUT \
<https://api.pleasebookme.com/api/v1/bookings/1> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "idempotencyKey": "123456",
	"userId": 1,
	"title": "Haircut with Harrington",
	"description": "",
	"startTime": "2026-08-25T02:00:00Z",
	"endTime": "2026-08-25T02:30:00Z",
	"serviceId": 1,
	"location": "",
	"status": "ACCEPTED",
	"paid": true
}'
```

---

## Event Produced

- BookingUpdated
