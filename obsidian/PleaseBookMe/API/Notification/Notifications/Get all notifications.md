## Description

Retrieves every notification.

---

## Endpoint

```json
GET /api/v1/notifications
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
<https://api.pleasebookme.com/api/v1/notifications> \
-H "Authorization: Bearer xxx"
```
