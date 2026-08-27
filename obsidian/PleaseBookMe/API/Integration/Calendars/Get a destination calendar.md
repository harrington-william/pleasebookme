## Description

Retrieves a single destination calendar link by its numeric ID.

---

## Endpoint

```json
GET /api/v1/destination-calendars/{destinationCalendarId}
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
	"destinationCalendarId": 0,
	"integrationType": "",
	"externalId": "",
	"userId": 0,
	"serviceId": 0,
	"oauthConnectionId": 0,
	"createdAt": "",
	"updatedAt": ""
}
```

---

## Possible Errors

- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 DESTINATION_CALENDAR_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "DESTINATION_CALENDAR_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X GET \
<https://api.pleasebookme.com/api/v1/destination-calendars/1> \
-H "Authorization: Bearer xxx"
```
