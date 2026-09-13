## Description

Records a delivery attempt of a notification through a provider (e.g. an email/SMS gateway). Unlike `Notification`/`NotificationQueue`, `status` and `attempt` have no SQL default and must always be supplied.

---

## Endpoint

```json
POST /api/v1/notification-deliveries
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
	"provider": "",
	"providerMessageId": "",
	"status": "SENT",
	"attempt": 1,
	"errorMessage": null,
	"sentAt": ""
}
```

## Successful Response

```json
201 Created
```

```json
{
	"notificationDeliveryId": 0,
	"notificationId": 0,
	"provider": "",
	"providerMessageId": "",
	"status": "SENT",
	"attempt": 1,
	"errorMessage": null,
	"sentAt": "",
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
<https://api.pleasebookme.com/api/v1/notification-deliveries> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "notificationId": 1,
	"provider": "sendgrid",
	"providerMessageId": "smtp-id-abc123",
	"status": "SENT",
	"attempt": 1,
	"sentAt": "2026-08-24T09:00:03Z"
}'
```

---

## Event Produced

- NotificationDeliveryCreated
