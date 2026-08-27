## Description

Retrieves every notification template.

---

## Endpoint

```json
GET /api/v1/notification-templates
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
<https://api.pleasebookme.com/api/v1/notification-templates> \
-H "Authorization: Bearer xxx"
```
