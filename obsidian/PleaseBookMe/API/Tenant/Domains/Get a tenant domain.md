## Description

Retrieves a single tenant domain by its numeric ID.

---

## Endpoint

```json
GET /api/v1/tenant-domains/{tenantDomainId}
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
	"tenantDomainId": 0,
	"tenantId": 0,
	"domain": "",
	"verified": false,
	"isPrimary": true,
	"verificationToken": "",
	"createdAt": "",
	"updatedAt": ""
}
```

---

## Possible Errors

- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 TENANT_DOMAIN_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "TENANT_DOMAIN_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X GET \
<https://api.pleasebookme.com/api/v1/tenant-domains/1> \
-H "Authorization: Bearer xxx"
```
