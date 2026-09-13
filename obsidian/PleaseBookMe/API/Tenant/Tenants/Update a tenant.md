## Description

Full-replace semantics — the caller is expected to send the complete resource. All four foreign keys (`organizationId`, `ownerUserId`, `ecosystemId`, `planId`) are re-resolved on every call, so a request can 404 on any of them exactly as `create` can. `defaultTimezone`/`defaultLocale` are only overwritten when supplied. No uniqueness re-check is performed on `slug` at update time.

---

## Endpoint

```json
PUT /api/v1/tenants/{tenantId}
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
200 OK
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
- 404 TENANT_NOT_FOUND
- 404 ORGANIZATION_NOT_FOUND
- 404 USER_NOT_FOUND
- 404 ECOSYSTEM_NOT_FOUND
- 404 TENANT_PLAN_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "TENANT_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X PUT \
<https://api.pleasebookme.com/api/v1/tenants/1> \
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
	"planId": 2,
	"region": "VN",
	"defaultTimezone": "Asia/Ho_Chi_Minh",
	"defaultLocale": "vi",
	"maxUsers": 3,
	"maxServices": 25,
	"maxWidgets": 2
}'
```

---

## Event Produced

- TenantUpdated
