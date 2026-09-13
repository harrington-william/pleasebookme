## Description

Retrieves every notification queue entry.

---

## Endpoint

```json
GET /api/v1/notification-queue
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
[
	{
		"notificationQueueId": 0,
		"notificationId": 0,
		"status": "QUEUED",
		"availableAt": "",
		"attempts": 0,
		"lastAttemptAt": null,
		"createdAt": ""
	}
]
```

---

## Possible Errors

- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "UNAUTHORIZED"
}
```

---

## Example Request

```bash
curl \
-X GET \
<https://api.pleasebookme.com/api/v1/notification-queue> \
-H "Authorization: Bearer xxx"
```
