## Description

Retrieves a single customer source record by its numeric ID.

---

## Endpoint

```json
GET /api/v1/customer-sources/{customerSourceId}
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
	"customerSourceId": 0,
	"customerId": 0,
	"source": "",
	"createdAt": ""
}
```

---

## Possible Errors

- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 CUSTOMER_SOURCE_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "CUSTOMER_SOURCE_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X GET \
<https://api.pleasebookme.com/api/v1/customer-sources/1> \
-H "Authorization: Bearer xxx"
```
