## Description

Creates a refresh token row for a user. `secret` must be unique across every refresh token. `owner`, `deviceName`, and `oauthClientId` are optional context about who/what the token was issued to; `revokedAt` may be supplied pre-set, but is normally left null at creation time.

---

## Endpoint

```json
POST /api/v1/refresh
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
201 Created
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
- 404 USER_NOT_FOUND
- 409 SECRET_ALREADY_EXISTS
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "SECRET_ALREADY_EXISTS"
}
```

---

## Example Request

```bash
curl \
-X POST \
<https://api.pleasebookme.com/api/v1/refresh> \
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

- RefreshTokenCreated
