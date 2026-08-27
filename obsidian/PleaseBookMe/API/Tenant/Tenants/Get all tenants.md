## Description

Retrieves every tenant.

---

## Endpoint

```json
GET /api/v1/tenants
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
<https://api.pleasebookme.com/api/v1/tenants> \
-H "Authorization: Bearer xxx"
```
