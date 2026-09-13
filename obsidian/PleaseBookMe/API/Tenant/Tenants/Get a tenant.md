## Description

Retrieves a single tenant by its numeric ID.

---

## Endpoint

```json
GET /api/v1/tenants/{tenantId}
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

- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 TENANT_NOT_FOUND
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
-X GET \
<https://api.pleasebookme.com/api/v1/tenants/1> \
-H "Authorization: Bearer xxx"
```
