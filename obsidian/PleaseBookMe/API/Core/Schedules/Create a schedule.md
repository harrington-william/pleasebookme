## Description

Creates a schedule owned by a user. `timezone` falls back to the platform default (`Australia/Sydney`) when omitted.

---

## Endpoint

```json
POST /api/v1/schedules
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
	"userId": 0,
	"title": "",
	"timezone": ""
}
```

## Successful Response

```json
201 Created
```

```json
{
	"scheduleId": 0,
	"userId": 0,
	"title": "",
	"timezone": "",
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
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "USER_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X POST \
<https://api.pleasebookme.com/api/v1/schedules> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "userId": 1,
	"title": "Working Hours",
	"timezone": "Australia/Sydney"
}'
```

---

## Event Produced

- ScheduleCreated
