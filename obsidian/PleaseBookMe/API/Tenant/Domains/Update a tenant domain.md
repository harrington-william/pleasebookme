## Description

Full-replace semantics — the caller is expected to send the complete resource. `tenantId` is re-resolved on every call, so an invalid value 404s exactly as `create` does. `verified`/`isPrimary` are only overwritten when supplied. No uniqueness re-check is performed on `domain` at update time.

---

## Endpoint

```json
PUT /api/v1/tenant-domains/{tenantDomainId}
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
	"tenantId": 0,
	"domain": "",
	"verified": false,
	"isPrimary": true,
	"verificationToken": ""
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

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 TENANT_DOMAIN_NOT_FOUND
- 404 TENANT_NOT_FOUND
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
-X PUT \
<https://api.pleasebookme.com/api/v1/tenant-domains/1> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "tenantId": 1,
	"domain": "harringtonsbarbershop.com",
	"verified": true,
	"isPrimary": true,
	"verificationToken": "pbm-verify-8f3c1a2e9b7d4f56"
}'
```

---

## Event Produced

- TenantDomainUpdated
