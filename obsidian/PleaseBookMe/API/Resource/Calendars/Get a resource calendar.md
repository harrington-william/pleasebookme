## Description

Retrieves a single resource calendar link by its numeric ID.

---

## Endpoint

```json
GET /api/v1/resource-calendars/{resourceCalendarId}
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
	"resourceCalendarId": 0,
	"resourceId": 0,
	"scheduleId": 0,
	"createdAt": "",
	"updatedAt": ""
}
```

---

## Possible Errors

- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 RESOURCE_CALENDAR_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "RESOURCE_CALENDAR_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X GET \
<https://api.pleasebookme.com/api/v1/resource-calendars/1> \
-H "Authorization: Bearer xxx"
```
