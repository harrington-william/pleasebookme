## Description

Full-replaces an existing refresh token. `userId` is re-resolved on every update, so ownership can be reassigned to a different user in the same call. No uniqueness re-check is performed on `secret` at update time.

---

## Endpoint

```json
PUT /api/v1/refresh/{refreshTokenId}
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
	"userId": 0,
	"secret": "",
	"owner": "",
	"deviceName": "",
	"oauthClientId": "",
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
	"refreshTokenId": 0,
	"owner": "",
	"userId": 0,
	"deviceName": "",
	"oauthClientId": "",
	"createdAt": "",
	"expiresAt": "",
	"revokedAt": ""
}
```

---

## Possible Errors

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 REFRESH_TOKEN_NOT_FOUND
- 404 USER_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "REFRESH_TOKEN_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X PUT \
<https://api.pleasebookme.com/api/v1/refresh/1> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "userId": 1,
	"secret": "a1b2c3d4e5f6",
	"owner": "USER",
	"deviceName": "Chrome on macOS",
	"oauthClientId": "",
	"expiresAt": "2026-09-23T00:00:00Z",
	"revokedAt": null
}'
```

---

## Event Produced

- RefreshTokenUpdated
