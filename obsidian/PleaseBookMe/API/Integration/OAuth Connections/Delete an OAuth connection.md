## Description

Deletes an OAuth connection row. This removes the local record only — it does not revoke the grant at the provider. Revoking a live Google grant is handled by the dedicated Google integration disconnect flow (`integrations/google/connections/{uid}`), not by this generic CRUD surface.

---

## Endpoint

```json
DELETE /api/v1/oauth-connections/{oauthConnectionId}
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
204 No Content
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
-X DELETE \
<https://api.pleasebookme.com/api/v1/oauth-connections/1> \
-H "Authorization: Bearer xxx"
```

---

## Event Produced

- OAuthConnectionDeleted
