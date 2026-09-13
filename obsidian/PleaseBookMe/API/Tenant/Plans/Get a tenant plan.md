## Description

Retrieves a single tenant plan by its numeric ID.

---

## Endpoint

```json
GET /api/v1/tenant-plans/{tenantPlanId}
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
-X GET \
<https://api.pleasebookme.com/api/v1/tenant-plans/1> \
-H "Authorization: Bearer xxx"
```
