## Description

Full-replaces an existing resource calendar link, re-resolving both `resourceId` and `scheduleId`.

---

## Endpoint

```json
PUT /api/v1/resource-calendars/{resourceCalendarId}
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
	"resourceId": 0,
	"scheduleId": 0
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

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 RESOURCE_CALENDAR_NOT_FOUND
- 404 RESOURCE_NOT_FOUND
- 404 SCHEDULE_NOT_FOUND
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
-X PUT \
<https://api.pleasebookme.com/api/v1/resource-calendars/1> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "resourceId": 1,
	"scheduleId": 2
}'
```

---

## Event Produced

- ResourceCalendarUpdated
