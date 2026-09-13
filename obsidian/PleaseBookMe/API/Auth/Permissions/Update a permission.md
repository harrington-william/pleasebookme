## Description

Full-replace semantics — the caller is expected to send the complete resource. No uniqueness re-check is performed on `slug` at update time.

---

## Endpoint

```json
PUT /api/v1/permissions/{permissionId}
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
200 OK
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
- 404 PERMISSION_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "PERMISSION_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X PUT \
<https://api.pleasebookme.com/api/v1/permissions/1> \
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

- PermissionUpdated
