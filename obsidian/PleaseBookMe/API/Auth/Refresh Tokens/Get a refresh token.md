## Description

Retrieves a single refresh token by its numeric ID.

---

## Endpoint

```json
GET /api/v1/refresh/{refreshTokenId}
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

- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 REFRESH_TOKEN_NOT_FOUND
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
-X GET \
<https://api.pleasebookme.com/api/v1/refresh/1> \
-H "Authorization: Bearer xxx"
```
