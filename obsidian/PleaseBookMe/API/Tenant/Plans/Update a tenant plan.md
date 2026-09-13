## Description

Full-replace semantics — the caller is expected to send the complete resource. `currency` is only overwritten when supplied; a missing field keeps its current stored value. No uniqueness re-check is performed on `code` at update time.

---

## Endpoint

```json
PUT /api/v1/tenant-plans/{tenantPlanId}
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
200 OK
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
- 404 TENANT_PLAN_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "TENANT_PLAN_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X PUT \
<https://api.pleasebookme.com/api/v1/tenant-plans/1> \
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

- TenantPlanUpdated
