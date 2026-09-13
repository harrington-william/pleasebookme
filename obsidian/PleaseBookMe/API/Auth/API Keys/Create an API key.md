## Description

Creates an API key scoped to a tenant and owned by a user. `publicKey` must be unique across every API key. `status` falls back to the platform default (`ACTIVE`) when omitted.

---

## Endpoint

```json
POST /api/v1/api-keys
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
	"ownerUserId": 0,
	"name": "",
	"description": "",
	"publicKey": "",
	"secretHash": "",
	"status": "",
	"lastUsedAt": "",
	"expiresAt": "",
	"revokedAt": ""
}
```

## Successful Response

```json
201 Created
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

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 USER_NOT_FOUND
- 409 PUBLIC_KEY_ALREADY_EXISTS
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "PUBLIC_KEY_ALREADY_EXISTS"
}
```

---

## Example Request

```bash
curl \
-X POST \
<https://api.pleasebookme.com/api/v1/api-keys> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "tenantId": 1,
	"ownerUserId": 1,
	"name": "Server integration key",
	"description": "Used by the booking widget backend",
	"publicKey": "pk_live_abc123",
	"secretHash": "sh_hashed_value",
	"status": "ACTIVE",
	"lastUsedAt": null,
	"expiresAt": null,
	"revokedAt": null
}'
```

---

## Event Produced

- ApiKeyCreated
