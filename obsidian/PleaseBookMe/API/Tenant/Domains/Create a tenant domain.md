## Description

Registers a custom domain (e.g. a client's own website domain) against a tenant. `verified`/`isPrimary` fall back to their platform defaults (`false`, `true`) when omitted. `verificationToken` is a DNS-verification value — the client is expected to publish it (e.g. as a DNS TXT record) to prove domain ownership, so unlike other token-shaped fields in this API it is not a live bearer credential and is echoed back in the response rather than excluded.

---

## Endpoint

```json
POST /api/v1/tenant-domains
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
201 Created
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
- 404 TENANT_NOT_FOUND
- 409 TENANT_DOMAIN_ALREADY_EXISTS
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "TENANT_DOMAIN_ALREADY_EXISTS"
}
```

---

## Example Request

```bash
curl \
-X POST \
<https://api.pleasebookme.com/api/v1/tenant-domains> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "tenantId": 1,
	"domain": "harringtonsbarbershop.com",
	"verified": false,
	"isPrimary": true,
	"verificationToken": "pbm-verify-8f3c1a2e9b7d4f56"
}'
```

---

## Event Produced

- TenantDomainCreated
