## Description

Retrieves every refresh token.

---

## Endpoint

```json
GET /api/v1/refresh
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
		"refreshTokenId": 0,
		"owner": "",
		"userId": 0,
		"deviceName": "",
		"oauthClientId": "",
		"createdAt": "",
		"expiresAt": "",
		"revokedAt": ""
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
<https://api.pleasebookme.com/api/v1/refresh> \
-H "Authorization: Bearer xxx"
```
