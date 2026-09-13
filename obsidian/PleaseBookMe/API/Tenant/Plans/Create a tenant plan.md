## Description

Creates a subscription plan (e.g. FREE, PRO) that tenants can be assigned to. Plans carry the resource quotas (`maxUsers`, `maxServices`, `maxWidgets`, `maxResources`, `maxApiKeys`) copied onto a tenant at provisioning time. `currency` falls back to its platform default (`USD`) when omitted.

---

## Endpoint

```json
POST /api/v1/tenant-plans
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
	"code": "",
	"name": "",
	"price": 0,
	"currency": "",
	"maxUsers": 0,
	"maxServices": 0,
	"maxWidgets": 0,
	"maxResources": 0,
	"maxApiKeys": 0
}
```

## Successful Response

```json
201 Created
```

```json
{
	"tenantPlanId": 0,
	"code": "",
	"name": "",
	"price": 0,
	"currency": "",
	"maxUsers": 0,
	"maxServices": 0,
	"maxWidgets": 0,
	"maxResources": 0,
	"maxApiKeys": 0,
	"createdAt": "",
	"updatedAt": ""
}
```

---

## Possible Errors

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 409 TENANT_PLAN_CODE_ALREADY_EXISTS
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "TENANT_PLAN_CODE_ALREADY_EXISTS"
}
```

---

## Example Request

```bash
curl \
-X POST \
<https://api.pleasebookme.com/api/v1/tenant-plans> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "code": "FREE",
	"name": "Free plan",
	"price": 0.00,
	"currency": "USD",
	"maxUsers": 1,
	"maxServices": 10,
	"maxWidgets": 1,
	"maxResources": 10,
	"maxApiKeys": 1
}'
```

---

## Event Produced

- TenantPlanCreated
