## Description

Enqueues a notification for asynchronous delivery. `status` falls back to `QUEUED` and `attempts` falls back to `0` when omitted.

---

## Endpoint

```json
POST /api/v1/notification-queue
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
	"status": "QUEUED",
	"availableAt": "",
	"attempts": 0,
	"lastAttemptAt": null
}
```

## Successful Response

```json
201 Created
```

```json
{
	"notificationQueueId": 0,
	"notificationId": 0,
	"status": "QUEUED",
	"availableAt": "",
	"attempts": 0,
	"lastAttemptAt": null,
	"createdAt": ""
}
```

---

## Possible Errors

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 NOTIFICATION_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "NOTIFICATION_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X POST \
<https://api.pleasebookme.com/api/v1/notification-queue> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "notificationId": 1,
	"status": "QUEUED",
	"availableAt": "2026-08-24T09:00:00Z",
	"attempts": 0
}'
```

---

## Event Produced

- NotificationQueueCreated
