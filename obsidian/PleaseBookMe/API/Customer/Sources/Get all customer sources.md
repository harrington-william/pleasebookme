## Description

Retrieves every customer source record.

---

## Endpoint

```json
GET /api/v1/customer-sources
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
		"customerSourceId": 0,
		"customerId": 0,
		"source": "",
		"createdAt": ""
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
<https://api.pleasebookme.com/api/v1/customer-sources> \
-H "Authorization: Bearer xxx"
```
