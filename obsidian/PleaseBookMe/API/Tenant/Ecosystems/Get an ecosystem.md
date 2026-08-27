## Description

Retrieves a single ecosystem by its numeric ID.

---

## Endpoint

```json
GET /api/v1/ecosystems/{ecosystemId}
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
	"ecosystemId": 0,
	"code": "",
	"name": "",
	"description": "",
	"icon": "",
	"status": "",
	"createdAt": "",
	"updatedAt": ""
}
```

---

## Possible Errors

- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 ECOSYSTEM_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "ECOSYSTEM_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X GET \
<https://api.pleasebookme.com/api/v1/ecosystems/1> \
-H "Authorization: Bearer xxx"
```
