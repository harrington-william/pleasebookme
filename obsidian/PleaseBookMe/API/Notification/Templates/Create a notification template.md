## Description

Creates a notification template. `tenantId` is optional — a `null` value defines a platform-wide template, otherwise the template is scoped to that tenant. `code` must be unique per `tenantId`. `enabled` falls back to `false` and `version` falls back to `1` when omitted.

---

## Endpoint

```json
POST /api/v1/notification-templates
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
	"tenantId": 0,
	"code": "",
	"name": "",
	"channel": "",
	"subjectTemplate": "",
	"bodyTemplate": "",
	"locale": "en",
	"enabled": true,
	"version": 1
}
```

## Successful Response

```json
201 Created
```

```json
{
	"notificationTemplateId": 0,
	"tenantId": 0,
	"code": "",
	"name": "",
	"channel": "",
	"subjectTemplate": "",
	"bodyTemplate": "",
	"locale": "en",
	"enabled": true,
	"version": 1,
	"createdAt": "",
	"updatedAt": ""
}
```

---

## Possible Errors

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 TENANT_NOT_FOUND
- 409 NOTIFICATION_TEMPLATE_CODE_ALREADY_EXISTS
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "NOTIFICATION_TEMPLATE_CODE_ALREADY_EXISTS"
}
```

---

## Example Request

```bash
curl \
-X POST \
<https://api.pleasebookme.com/api/v1/notification-templates> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "tenantId": 1,
	"code": "BOOKING_CONFIRMED",
	"name": "Booking Confirmed",
	"channel": "EMAIL",
	"subjectTemplate": "Your booking is confirmed",
	"bodyTemplate": "Hi {{name}}, your booking on {{date}} is confirmed.",
	"locale": "en",
	"enabled": true,
	"version": 1
}'
```

---

## Event Produced

- NotificationTemplateCreated
