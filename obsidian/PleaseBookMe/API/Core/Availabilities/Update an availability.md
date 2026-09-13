## Description

Full-replaces an existing availability window — the caller is expected to send the complete resource, including `userId`/`scheduleId`, both of which are re-resolved on every update.

---

## Endpoint

```json
PUT /api/v1/availabilities/{availabilityId}
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
200 OK
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
- 404 AVAILABILITY_NOT_FOUND
- 404 USER_NOT_FOUND
- 404 SCHEDULE_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "AVAILABILITY_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X PUT \
<https://api.pleasebookme.com/api/v1/availabilities/1> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "userId": 1,
	"scheduleId": 1,
	"days": [1, 2, 3, 4, 5, 6],
	"startTime": "10:00:00",
	"endTime": "18:00:00"
}'
```

---

## Event Produced

- AvailabilityUpdated
