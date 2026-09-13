## Description

Creates a notification record scheduled for delivery. `tenantId`, `organizationId`, `templateId`, and `channelId` must all reference existing rows. `status` falls back to `PENDING` and `priority` falls back to `NORMAL` when omitted.

---

## Endpoint

```json
POST /api/v1/notifications
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
	"organizationId": 0,
	"recipientType": "USER",
	"recipientUid": "",
	"templateId": 0,
	"channelId": 0,
	"status": "PENDING",
	"priority": "NORMAL",
	"subject": "",
	"content": "",
	"locale": "en",
	"scheduledAt": "",
	"sentAt": null
}
```

## Successful Response

```json
201 Created
```

```json
{
	"notificationId": 0,
	"notificationUid": "",
	"tenantId": 0,
	"organizationId": 0,
	"recipientType": "USER",
	"recipientUid": "",
	"templateId": 0,
	"channelId": 0,
	"status": "PENDING",
	"priority": "NORMAL",
	"subject": "",
	"content": "",
	"locale": "en",
	"scheduledAt": "",
	"sentAt": null,
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
- 404 ORGANIZATION_NOT_FOUND
- 404 NOTIFICATION_TEMPLATE_NOT_FOUND
- 404 NOTIFICATION_CHANNEL_NOT_FOUND
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
-X POST \
<https://api.pleasebookme.com/api/v1/notifications> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "tenantId": 1,
	"organizationId": 1,
	"recipientType": "USER",
	"recipientUid": "1",
	"templateId": 1,
	"channelId": 1,
	"status": "PENDING",
	"priority": "NORMAL",
	"subject": "Your booking is confirmed",
	"content": "Hi Harrington, your booking on 2026-08-24 is confirmed.",
	"locale": "en",
	"scheduledAt": "2026-08-24T09:00:00Z"
}'
```

---

## Event Produced

- NotificationCreated
