## Description

Creates a recurring availability window for a schedule — the days of the week (`days`) and the time range (`startTime`/`endTime`) it applies to.

---

## Endpoint

```json
POST /api/v1/availabilities
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
	"scheduleId": 0,
	"days": [],
	"startTime": "",
	"endTime": ""
}
```

## Successful Response

```json
201 Created
```

```json
{
	"availabilityId": 0,
	"userId": 0,
	"scheduleId": 0,
	"days": [],
	"startTime": "",
	"endTime": "",
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
<https://api.pleasebookme.com/api/v1/availabilities> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "userId": 1,
	"scheduleId": 1,
	"days": [1, 2, 3, 4, 5],
	"startTime": "09:00:00",
	"endTime": "17:00:00"
}'
```

---

## Event Produced

- AvailabilityCreated
