## Description

Retrieves every ecosystem.

---

## Endpoint

```json
GET /api/v1/ecosystems
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
		"ecosystemId": 0,
		"code": "",
		"name": "",
		"description": "",
		"icon": "",
		"status": "",
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
<https://api.pleasebookme.com/api/v1/ecosystems> \
-H "Authorization: Bearer xxx"
```
