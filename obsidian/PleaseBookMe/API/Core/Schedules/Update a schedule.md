## Description

Full-replaces an existing schedule. `userId` is re-resolved on every update, so a schedule can be reassigned to a different owner. `timezone` is only overwritten when supplied; a missing value keeps its current stored value.

---

## Endpoint

```json
PUT /api/v1/schedules/{scheduleId}
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
200 OK
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
- 404 SCHEDULE_NOT_FOUND
- 404 USER_NOT_FOUND
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
-X PUT \
<https://api.pleasebookme.com/api/v1/schedules/1> \
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

- ScheduleUpdated
