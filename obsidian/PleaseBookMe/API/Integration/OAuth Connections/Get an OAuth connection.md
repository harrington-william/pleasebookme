## Description

Retrieves a single OAuth connection by its numeric ID. The encrypted `accessToken`/`refreshToken` are never returned — only connection metadata (provider, granted scopes, expiry, status).

---

## Endpoint

```json
GET /api/v1/oauth-connections/{oauthConnectionId}
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
	"oauthConnectionId": 0,
	"oauthConnectionUid": "",
	"userId": 0,
	"provider": "",
	"providerAccountId": "",
	"providerEmail": "",
	"scopes": [],
	"tokenKeyVersion": 0,
	"tokenExpiresAt": "",
	"status": "",
	"connectedAt": "",
	"lastRefreshedAt": "",
	"lastUsedAt": "",
	"revokedAt": "",
	"createdAt": "",
	"updatedAt": ""
}
```

---

## Possible Errors

- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 OAUTH_CONNECTION_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "OAUTH_CONNECTION_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X GET \
<https://api.pleasebookme.com/api/v1/oauth-connections/1> \
-H "Authorization: Bearer xxx"
```
