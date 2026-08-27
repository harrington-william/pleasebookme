## Description

Retrieves every tenant domain.

---

## Endpoint

```json
GET /api/v1/tenant-domains
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
		"tenantDomainId": 0,
		"tenantId": 0,
		"domain": "",
		"verified": false,
		"isPrimary": true,
		"verificationToken": "",
		"createdAt": "",
		"updatedAt": ""
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
<https://api.pleasebookme.com/api/v1/tenant-domains> \
-H "Authorization: Bearer xxx"
```
