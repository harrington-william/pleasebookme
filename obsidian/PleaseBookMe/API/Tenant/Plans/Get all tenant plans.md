## Description

Retrieves every tenant plan.

---

## Endpoint

```json
GET /api/v1/tenant-plans
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
<https://api.pleasebookme.com/api/v1/tenant-plans> \
-H "Authorization: Bearer xxx"
```
