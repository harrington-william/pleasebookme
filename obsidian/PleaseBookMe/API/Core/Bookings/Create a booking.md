## Description

Creates a booking for a user against a service, for a given start/end time window. `status`, `paid`, `rescheduled`, and `noShowHost` fall back to their platform defaults (`PENDING`, `false`, `false`, `false`) when omitted. `cancelledById`, `rescheduledById`, `deletedById`, `destinationCalendarId`, and `destinationSheetsId` are all optional — each is only resolved and attached when supplied.

---

## Endpoint

```json
POST /api/v1/bookings
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
201 Created
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
- 404 USER_NOT_FOUND
- 404 SERVICE_NOT_FOUND
- 404 DESTINATION_CALENDAR_NOT_FOUND
- 404 DESTINATION_SHEETS_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "SERVICE_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X POST \
<https://api.pleasebookme.com/api/v1/bookings> \
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
	"status": "PENDING",
	"paid": false
}'
```

---

## Event Produced

- BookingCreated
