## Description

Links a resource to a schedule. Both `resourceId` and `scheduleId` must reference existing rows. There is no uniqueness constraint — a resource may be linked to more than one schedule.

---

## Endpoint

```json
POST /api/v1/resource-calendars
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
201 Created
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
- 404 RESOURCE_NOT_FOUND
- 404 SCHEDULE_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "SCHEDULE_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X POST \
<https://api.pleasebookme.com/api/v1/resource-calendars> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "resourceId": 1,
	"scheduleId": 1
}'
```

---

## Event Produced

- ResourceCalendarCreated
