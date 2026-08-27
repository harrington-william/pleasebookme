## Description

Retrieves every service.

---

## Endpoint

```json
GET /api/v1/services
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
<https://api.pleasebookme.com/api/v1/services> \
-H "Authorization: Bearer xxx"
```
