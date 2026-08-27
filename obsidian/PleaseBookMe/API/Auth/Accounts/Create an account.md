## Description

Links an OAuth provider account (e.g. Google) to an existing user. `type` falls back to `oauth` when omitted. The `(provider, providerAccountId)` pair must be unique — the same provider account cannot be linked twice.

---

## Endpoint

```json
POST /api/v1/accounts
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
	"type": "",
	"provider": "",
	"providerAccountId": "",
	"providerEmail": ""
}
```

## Successful Response

```json
201 Created
```

```json
{
	"accountId": 0,
	"userId": 0,
	"type": "",
	"provider": "",
	"providerAccountId": "",
	"providerEmail": ""
}
```

---

## Possible Errors

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 USER_NOT_FOUND
- 409 ACCOUNT_ALREADY_LINKED
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "ACCOUNT_ALREADY_LINKED"
}
```

---

## Example Request

```bash
curl \
-X POST \
<https://api.pleasebookme.com/api/v1/accounts> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "userId": 1,
	"type": "oauth",
	"provider": "google",
	"providerAccountId": "104839201223948",
	"providerEmail": "harrington@gmail.com"
}'
```

---

## Event Produced

- AccountCreated
