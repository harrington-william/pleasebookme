## Description

Retrieves every booking.

---

## Endpoint

```json
GET /api/v1/bookings
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
<https://api.pleasebookme.com/api/v1/bookings> \
-H "Authorization: Bearer xxx"
```
