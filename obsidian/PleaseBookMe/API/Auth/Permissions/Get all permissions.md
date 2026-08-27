## Description

Retrieves every permission.

---

## Endpoint

```json
GET /api/v1/permissions
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
		"permissionId": 0,
		"name": "",
		"description": "",
		"resource": "",
		"action": "",
		"slug": "",
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
<https://api.pleasebookme.com/api/v1/permissions> \
-H "Authorization: Bearer xxx"
```
