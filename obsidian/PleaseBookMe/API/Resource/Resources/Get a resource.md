## Description

Retrieves a single resource by its numeric ID.

---

## Endpoint

```json
GET /api/v1/resources/{resourceId}
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
	"resourceId": 0,
	"resourceUid": "",
	"organizationId": 0,
	"serviceId": 0,
	"resourceTypeId": 0,
	"name": "",
	"slug": "",
	"description": "",
	"capacity": 0,
	"status": "",
	"isBookable": true,
	"isVirtual": false,
	"createdAt": "",
	"updatedAt": ""
}
```

---

## Possible Errors

- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 RESOURCE_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "RESOURCE_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X GET \
<https://api.pleasebookme.com/api/v1/resources/1> \
-H "Authorization: Bearer xxx"
```
