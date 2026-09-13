## Description

Full-replaces an existing notification template. `enabled` and `version` are only overwritten when supplied; a missing field keeps its current stored value. Every other field, including `tenantId`, is re-resolved and overwritten unconditionally. No uniqueness re-check is performed on `(tenantId, code)` at update time.

---

## Endpoint

```json
PUT /api/v1/notification-templates/{notificationTemplateId}
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
	"version": 2
}
```

## Successful Response

```json
200 OK
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
	"version": 2,
	"createdAt": "",
	"updatedAt": ""
}
```

---

## Possible Errors

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 NOTIFICATION_TEMPLATE_NOT_FOUND
- 404 TENANT_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "NOTIFICATION_TEMPLATE_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X PUT \
<https://api.pleasebookme.com/api/v1/notification-templates/1> \
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
	"version": 2
}'
```

---

## Event Produced

- NotificationTemplateUpdated
