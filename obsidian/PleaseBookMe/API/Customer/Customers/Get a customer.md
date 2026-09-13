## Description

Retrieves a single customer by its numeric ID.

---

## Endpoint

```json
GET /api/v1/customers/{customerId}
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
```

---

## Possible Errors

- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 CUSTOMER_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "CUSTOMER_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X GET \
<https://api.pleasebookme.com/api/v1/customers/1> \
-H "Authorization: Bearer xxx"
```
