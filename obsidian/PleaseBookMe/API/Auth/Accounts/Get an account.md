## Description

Retrieves a single linked account by its numeric ID.

---

## Endpoint

```json
GET /api/v1/accounts/{accountId}
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

- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 ACCOUNT_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "ACCOUNT_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X GET \
<https://api.pleasebookme.com/api/v1/accounts/1> \
-H "Authorization: Bearer xxx"
```
