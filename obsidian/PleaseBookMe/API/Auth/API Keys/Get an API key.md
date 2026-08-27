## Description

Retrieves a single API key by its numeric ID.

---

## Endpoint

```json
GET /api/v1/api-keys/{apiKeyId}
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
	"apiKeyId": 0,
	"apiKeyUid": "",
	"tenantId": 0,
	"ownerUserId": 0,
	"name": "",
	"description": "",
	"publicKey": "",
	"status": "",
	"lastUsedAt": "",
	"expiresAt": "",
	"revokedAt": "",
	"createdAt": "",
	"updatedAt": ""
}
```

---

## Possible Errors

- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 API_KEY_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "API_KEY_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X GET \
<https://api.pleasebookme.com/api/v1/api-keys/1> \
-H "Authorization: Bearer xxx"
```
