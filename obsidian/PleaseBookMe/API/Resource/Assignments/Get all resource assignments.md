## Description

Retrieves every resource assignment.

---

## Endpoint

```json
GET /api/v1/resource-assignments
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
		"resourceAssignmentId": 0,
		"resourceId": 0,
		"membershipId": 0,
		"assignedAt": "",
		"releasedAt": "",
		"assignedById": 0,
		"releasedById": 0,
		"isPrimary": true
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
<https://api.pleasebookme.com/api/v1/resource-assignments> \
-H "Authorization: Bearer xxx"
```
