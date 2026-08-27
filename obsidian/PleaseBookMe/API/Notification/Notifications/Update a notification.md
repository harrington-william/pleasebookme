## Description

Full-replaces an existing notification. `status` and `priority` are only overwritten when supplied; a missing field keeps its current stored value. All four foreign keys (`tenantId`, `organizationId`, `templateId`, `channelId`) are re-resolved and overwritten unconditionally.

---

## Endpoint

```json
PUT /api/v1/notifications/{notificationId}
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
	"status": "SENT",
	"priority": "NORMAL",
	"subject": "",
	"content": "",
	"locale": "en",
	"scheduledAt": "",
	"sentAt": ""
}
```

## Successful Response

```json
200 OK
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
	"status": "SENT",
	"priority": "NORMAL",
	"subject": "",
	"content": "",
	"locale": "en",
	"scheduledAt": "",
	"sentAt": "",
	"createdAt": "",
	"updatedAt": ""
}
```

---

## Possible Errors

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 NOTIFICATION_NOT_FOUND
- 404 TENANT_NOT_FOUND
- 404 ORGANIZATION_NOT_FOUND
- 404 NOTIFICATION_TEMPLATE_NOT_FOUND
- 404 NOTIFICATION_CHANNEL_NOT_FOUND
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
-X PUT \
<https://api.pleasebookme.com/api/v1/notifications/1> \
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
	"status": "SENT",
	"priority": "NORMAL",
	"subject": "Your booking is confirmed",
	"content": "Hi Harrington, your booking on 2026-08-24 is confirmed.",
	"locale": "en",
	"scheduledAt": "2026-08-24T09:00:00Z",
	"sentAt": "2026-08-24T09:00:03Z"
}'
```

---

## Event Produced

- NotificationUpdated
