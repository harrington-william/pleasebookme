## Description

Retrieves a single booking by its numeric ID.

---

## Endpoint

```json
GET /api/v1/bookings/{bookingId}
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

- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 BOOKING_NOT_FOUND
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
-X GET \
<https://api.pleasebookme.com/api/v1/bookings/1> \
-H "Authorization: Bearer xxx"
```
