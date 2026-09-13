## Description

Retrieves a single role assignment by the composite key of `userId` and `roleId`.

---

## Endpoint

```json
GET /api/v1/user-roles/{userId}/{roleId}
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
	"userId": 0,
	"roleId": 0,
	"assignedAt": ""
}
```

---

## Possible Errors

- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 USER_ROLE_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "USER_ROLE_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X GET \
<https://api.pleasebookme.com/api/v1/user-roles/1/2> \
-H "Authorization: Bearer xxx"
```
