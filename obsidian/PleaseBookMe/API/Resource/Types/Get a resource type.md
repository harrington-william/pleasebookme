## Description

Retrieves a single resource type by its numeric ID.

---

## Endpoint

```json
GET /api/v1/resource-types/{resourceTypeId}
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
	"resourceTypeId": 0,
	"organizationId": 0,
	"name": "",
	"description": "",
	"icon": "",
	"createdAt": "",
	"updatedAt": ""
}
```

---

## Possible Errors

- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 RESOURCE_TYPE_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "RESOURCE_TYPE_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X GET \
<https://api.pleasebookme.com/api/v1/resource-types/1> \
-H "Authorization: Bearer xxx"
```
