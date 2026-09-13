## Description

Creates a delivery channel (e.g. email, SMS, push). `code` must be unique across all channels. `enabled` falls back to `false` when omitted.

---

## Endpoint

```json
POST /api/v1/notification-channels
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
201 Created
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
- 409 NOTIFICATION_CHANNEL_CODE_ALREADY_EXISTS
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "NOTIFICATION_CHANNEL_CODE_ALREADY_EXISTS"
}
```

---

## Example Request

```bash
curl \
-X POST \
<https://api.pleasebookme.com/api/v1/notification-channels> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "code": "EMAIL",
	"name": "Email",
	"enabled": true
}'
```

---

## Event Produced

- NotificationChannelCreated
