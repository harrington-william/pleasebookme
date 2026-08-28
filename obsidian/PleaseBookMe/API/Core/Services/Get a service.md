## Description

Retrieves a single service by its numeric ID.

---

## Endpoint

```json
GET /api/v1/services/{serviceId}
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
	"serviceId": 0,
	"title": "",
	"slug": "",
	"description": "",
	"interfaceLanguage": "",
	"location": "",
	"userId": 0,
	"profileId": 0,
	"organizationId": 0,
	"scheduleId": 0,
	"periodType": "",
	"timezone": "",
	"minPrice": 0,
	"maxPrice": 0,
	"currency": "",
	"requiresConfirmation": false,
	"disableCancelling": false,
	"disableRescheduling": false,
	"successRedirectUrl": "",
	"isInstantService": false,
	"maxActiveBookingPerBooker": 0,
	"destinationCalendarId": 0,
	"destinationSheetsId": 0,
	"bookingPolicy": null,
	"createdAt": "",
	"updatedAt": ""
}
```

---

## Possible Errors

- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 SERVICE_NOT_FOUND
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
-X GET \
<https://api.pleasebookme.com/api/v1/services/1> \
-H "Authorization: Bearer xxx"
```
