## Description

Retrieves every customer.

---

## Endpoint

```json
GET /api/v1/customers
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
		"customerId": 0,
		"customerUid": "",
		"tenantId": 0,
		"organizationId": 0,
		"email": "",
		"phone": "",
		"name": "",
		"avatarUrl": "",
		"locale": "",
		"timezone": "",
		"birthday": "",
		"gender": "",
		"status": "",
		"marketingConsent": false,
		"notes": "",
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
<https://api.pleasebookme.com/api/v1/customers> \
-H "Authorization: Bearer xxx"
```
