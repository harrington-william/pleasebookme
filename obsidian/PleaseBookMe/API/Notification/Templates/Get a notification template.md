## Description

Retrieves a single notification template by its numeric ID.

---

## Endpoint

```json
GET /api/v1/notification-templates/{notificationTemplateId}
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

- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 NOTIFICATION_TEMPLATE_NOT_FOUND
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
-X GET \
<https://api.pleasebookme.com/api/v1/notification-templates/1> \
-H "Authorization: Bearer xxx"
```
