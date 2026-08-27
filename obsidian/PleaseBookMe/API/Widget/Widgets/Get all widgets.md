## Description

Retrieves every widget.

---

## Endpoint

```json
GET /api/v1/widgets
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
<https://api.pleasebookme.com/api/v1/widgets> \
-H "Authorization: Bearer xxx"
```
