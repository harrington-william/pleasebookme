## Description

Creates a tenant — the active-subscription record (plan, ecosystem, region, quotas) that upgrades an organization from a free self-serve account into a paying workspace. Resolves four foreign keys: `organizationId`, `ownerUserId`, `ecosystemId`, and `planId`. `defaultTimezone`/`defaultLocale` fall back to their platform defaults (`Australia/Sydney`, `en`) when omitted.

---

## Endpoint

```json
POST /api/v1/tenants
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
	"organizationId": 0,
	"ownerUserId": 0,
	"ecosystemId": 0,
	"name": "",
	"slug": "",
	"status": "",
	"planId": 0,
	"region": "",
	"defaultTimezone": "",
	"defaultLocale": "",
	"maxUsers": 0,
	"maxServices": 0,
	"maxWidgets": 0
}
```

## Successful Response

```json
201 Created
```

```json
{
	"tenantId": 0,
	"tenantUid": "",
	"organizationId": 0,
	"ownerUserId": 0,
	"ecosystemId": 0,
	"name": "",
	"slug": "",
	"status": "",
	"planId": 0,
	"region": "",
	"defaultTimezone": "",
	"defaultLocale": "",
	"maxUsers": 0,
	"maxServices": 0,
	"maxWidgets": 0,
	"createdAt": "",
	"updatedAt": ""
}
```

---

## Possible Errors

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 ORGANIZATION_NOT_FOUND
- 404 USER_NOT_FOUND
- 404 ECOSYSTEM_NOT_FOUND
- 404 TENANT_PLAN_NOT_FOUND
- 409 TENANT_SLUG_ALREADY_EXISTS
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "TENANT_SLUG_ALREADY_EXISTS"
}
```

---

## Example Request

```bash
curl \
-X POST \
<https://api.pleasebookme.com/api/v1/tenants> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "organizationId": 1,
	"ownerUserId": 1,
	"ecosystemId": 1,
	"name": "Harrington'"'"'s Barbershop",
	"slug": "harringtons-barbershop",
	"status": "ACTIVE",
	"planId": 1,
	"region": "VN",
	"defaultTimezone": "Asia/Ho_Chi_Minh",
	"defaultLocale": "vi",
	"maxUsers": 1,
	"maxServices": 10,
	"maxWidgets": 1
}'
```

---

## Event Produced

- TenantCreated
