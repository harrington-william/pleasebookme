## Description

Full-replaces an existing notification delivery record. Unlike most resources in this domain, every field here — including `status` and `attempt` — has no SQL default, so `update` overwrites all of them unconditionally; there is no optional-field/`@Builder.Default` guard on this endpoint.

---

## Endpoint

```json
PUT /api/v1/notification-deliveries/{notificationDeliveryId}
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
	"status": "FAILED",
	"attempt": 2,
	"errorMessage": "",
	"sentAt": null
}
```

## Successful Response

```json
200 OK
```

```json
{
	"notificationDeliveryId": 0,
	"notificationId": 0,
	"provider": "",
	"providerMessageId": "",
	"status": "FAILED",
	"attempt": 2,
	"errorMessage": "",
	"sentAt": null,
	"createdAt": ""
}
```

---

## Possible Errors

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 NOTIFICATION_DELIVERY_NOT_FOUND
- 404 NOTIFICATION_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "NOTIFICATION_DELIVERY_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X PUT \
<https://api.pleasebookme.com/api/v1/notification-deliveries/1> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "notificationId": 1,
	"provider": "sendgrid",
	"providerMessageId": "smtp-id-abc123",
	"status": "FAILED",
	"attempt": 2,
	"errorMessage": "connection timeout"
}'
```

---

## Event Produced

- NotificationDeliveryUpdated
