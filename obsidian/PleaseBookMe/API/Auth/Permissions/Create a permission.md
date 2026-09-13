## Description

Creates a new permission with a unique `slug`. Permissions are the atomic RBAC capabilities assigned to roles via the Role Permission API.

---

## Endpoint

```json
POST /api/v1/permissions
```

---

## Authentication

Required **Bearer Token**

---

## Headers

```json
{
	"Authorization": "JWT Access Token",
	"Content-Type": "application/json"
}
```

## Body

```json
{
	"name": "",
	"description": "",
	"resource": "",
	"action": "",
	"slug": ""
}
```

## Successful Response

```json
201 Created
```

```json
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
```

---

## Possible Errors

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 409 PERMISSION_SLUG_ALREADY_EXISTS
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "PERMISSION_SLUG_ALREADY_EXISTS"
}
```

---

## Example Request

```bash
curl \
-X POST \
<https://api.pleasebookme.com/api/v1/permissions> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "name": "Create User",
	"description": "Allows creating a new user",
	"resource": "USER",
	"action": "CREATE",
	"slug": "USER.CREATE"
}'
```

---

## Event Produced

- PermissionCreated
