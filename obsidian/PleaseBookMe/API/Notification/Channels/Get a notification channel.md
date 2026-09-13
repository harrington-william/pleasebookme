## Description

Retrieves a single notification channel by its numeric ID.

---

## Endpoint

```json
GET /api/v1/notification-channels/{notificationChannelId}
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
	"notificationChannelId": 0,
	"code": "",
	"name": "",
	"enabled": true,
	"createdAt": ""
}
```

---

## Possible Errors

- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 NOTIFICATION_CHANNEL_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "NOTIFICATION_CHANNEL_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X GET \
<https://api.pleasebookme.com/api/v1/notification-channels/1> \
-H "Authorization: Bearer xxx"
```
