## Description

Full-replaces an existing notification queue entry. `status` and `attempts` are only overwritten when supplied; a missing field keeps its current stored value. `notificationId` is re-resolved and overwritten unconditionally.

---

## Endpoint

```json
PUT /api/v1/notification-queue/{notificationQueueId}
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
	"notificationId": 0,
	"status": "PROCESSING",
	"availableAt": "",
	"attempts": 1,
	"lastAttemptAt": ""
}
```

## Successful Response

```json
200 OK
```

```json
{
	"notificationQueueId": 0,
	"notificationId": 0,
	"status": "PROCESSING",
	"availableAt": "",
	"attempts": 1,
	"lastAttemptAt": "",
	"createdAt": ""
}
```

---

## Possible Errors

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 NOTIFICATION_QUEUE_NOT_FOUND
- 404 NOTIFICATION_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "NOTIFICATION_QUEUE_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X PUT \
<https://api.pleasebookme.com/api/v1/notification-queue/1> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "notificationId": 1,
	"status": "PROCESSING",
	"availableAt": "2026-08-24T09:00:00Z",
	"attempts": 1,
	"lastAttemptAt": "2026-08-24T09:00:05Z"
}'
```

---

## Event Produced

- NotificationQueueUpdated
