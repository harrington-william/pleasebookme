## Description

Full-replace semantics — the caller is expected to send the complete resource. `status` is only overwritten when supplied; a missing value keeps its current stored value. No uniqueness re-check is performed on `publicKey` at update time.

---

## Endpoint

```json
PUT /api/v1/api-keys/{apiKeyId}
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

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 API_KEY_NOT_FOUND
- 404 USER_NOT_FOUND
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
-X PUT \
<https://api.pleasebookme.com/api/v1/api-keys/1> \
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
	"status": "REVOKED",
	"lastUsedAt": "2026-08-20T10:00:00Z",
	"expiresAt": null,
	"revokedAt": "2026-08-24T09:00:00Z"
}'
```

---

## Event Produced

- ApiKeyUpdated
