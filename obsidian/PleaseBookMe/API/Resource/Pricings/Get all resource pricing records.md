## Description

Retrieves every resource pricing record.

---

## Endpoint

```json
GET /api/v1/resource-pricing
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
		"resourcePricingId": 0,
		"resourceId": 0,
		"price": 0,
		"currency": "",
		"effectiveFrom": "",
		"effectiveUntil": "",
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
<https://api.pleasebookme.com/api/v1/resource-pricing> \
-H "Authorization: Bearer xxx"
```
