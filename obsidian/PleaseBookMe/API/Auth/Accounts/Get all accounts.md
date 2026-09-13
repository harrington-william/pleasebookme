## Description

Retrieves every linked account.

---

## Endpoint

```json
GET /api/v1/accounts
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
		"accountId": 0,
		"userId": 0,
		"type": "",
		"provider": "",
		"providerAccountId": "",
		"providerEmail": ""
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
<https://api.pleasebookme.com/api/v1/accounts> \
-H "Authorization: Bearer xxx"
```
