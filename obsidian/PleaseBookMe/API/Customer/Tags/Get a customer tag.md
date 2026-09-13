## Description

Retrieves a single customer tag by its numeric ID.

---

## Endpoint

```json
GET /api/v1/customer-tags/{customerTagId}
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
	"customerTagId": 0,
	"customerId": 0,
	"tag": "",
	"createdAt": "",
	"updatedAt": ""
}
```

---

## Possible Errors

- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 CUSTOMER_TAG_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "CUSTOMER_TAG_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X GET \
<https://api.pleasebookme.com/api/v1/customer-tags/1> \
-H "Authorization: Bearer xxx"
```
