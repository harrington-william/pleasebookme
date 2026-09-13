## Description

Full-replaces an existing notification channel. `enabled` is only overwritten when supplied; a missing field keeps its current stored value. No uniqueness re-check is performed on `code` at update time.

---

## Endpoint

```json
PUT /api/v1/notification-channels/{notificationChannelId}
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
	"code": "",
	"name": "",
	"enabled": true
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

- 400 INVALID_REQUEST
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
-X PUT \
<https://api.pleasebookme.com/api/v1/notification-channels/1> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "code": "EMAIL",
	"name": "Email",
	"enabled": false
}'
```

---

## Event Produced

- NotificationChannelUpdated
