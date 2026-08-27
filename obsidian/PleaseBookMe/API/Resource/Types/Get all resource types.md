## Description

Retrieves every resource type.

---

## Endpoint

```json
GET /api/v1/resource-types
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
		"resourceTypeId": 0,
		"organizationId": 0,
		"name": "",
		"description": "",
		"icon": "",
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
<https://api.pleasebookme.com/api/v1/resource-types> \
-H "Authorization: Bearer xxx"
```
