## Description

Revokes a permission from a role.

---

## Endpoint

```json
DELETE /api/v1/role-permissions/{roleId}/{permissionId}
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
204 No Content
```

---

## Possible Errors

- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 ROLE_PERMISSION_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "ROLE_PERMISSION_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X DELETE \
<https://api.pleasebookme.com/api/v1/role-permissions/1/3> \
-H "Authorization: Bearer xxx"
```

---

## Event Produced

- RolePermissionDeleted
