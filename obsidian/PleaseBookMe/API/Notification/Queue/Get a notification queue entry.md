## Description

Retrieves a single notification queue entry by its numeric ID.

---

## Endpoint

```json
GET /api/v1/notification-queue/{notificationQueueId}
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

- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 NOTIFICATION_QUEUE_NOT_FOUND
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
-X GET \
<https://api.pleasebookme.com/api/v1/notification-queue/1> \
-H "Authorization: Bearer xxx"
```
