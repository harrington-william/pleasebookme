## Description

Assigns a permission to a role. `roleId` and `permissionId` together form the composite primary key, so assigning the same permission to the same role twice is rejected rather than silently accepted.

---

## Endpoint

```json
POST /api/v1/role-permissions
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
	"roleId": 0,
	"permissionId": 0
}
```

## Successful Response

```json
201 Created
```

```json
{
	"roleId": 0,
	"permissionId": 0
}
```

---

## Possible Errors

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 ROLE_NOT_FOUND
- 404 PERMISSION_NOT_FOUND
- 409 PERMISSION_ALREADY_ASSIGNED
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "PERMISSION_ALREADY_ASSIGNED"
}
```

---

## Example Request

```bash
curl \
-X POST \
<https://api.pleasebookme.com/api/v1/role-permissions> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "roleId": 1,
	"permissionId": 3
}'
```

---

## Event Produced

- RolePermissionCreated
