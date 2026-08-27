## Description

Retrieves every resource.

---

## Endpoint

```json
GET /api/v1/resources
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
<https://api.pleasebookme.com/api/v1/resources> \
-H "Authorization: Bearer xxx"
```
