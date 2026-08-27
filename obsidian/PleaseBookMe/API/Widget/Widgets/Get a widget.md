## Description

Retrieves a single widget by its numeric ID.

---

## Endpoint

```json
GET /api/v1/widgets/{widgetId}
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
	"widgetId": 0,
	"widgetUid": "",
	"tenantId": 0,
	"name": "",
	"status": "",
	"type": "",
	"originValidation": false,
	"publicKey": "",
	"issuedAt": "",
	"expiresAt": "",
	"lastUsedAt": "",
	"createdAt": "",
	"updatedAt": ""
}
```

---

## Possible Errors

- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 WIDGET_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "WIDGET_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X GET \
<https://api.pleasebookme.com/api/v1/widgets/1> \
-H "Authorization: Bearer xxx"
```
