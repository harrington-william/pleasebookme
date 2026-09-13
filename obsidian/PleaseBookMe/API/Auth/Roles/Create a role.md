## Description

Creates a new RBAC role with a unique `name`. `description` is optional and has no length limit.

---

## Endpoint

```json
POST /api/v1/roles
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
	"description": ""
}
```

## Successful Response

```json
201 Created
```

```json
{
	"roleId": 0,
	"name": "",
	"description": "",
	"createdAt": "",
	"updatedAt": ""
}
```

---

## Possible Errors

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 409 ROLE_NAME_ALREADY_EXISTS
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "ROLE_NAME_ALREADY_EXISTS"
}
```

---

## Example Request

```bash
curl \
-X POST \
<https://api.pleasebookme.com/api/v1/roles> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "name": "STAFF",
	"description": "Front-desk staff with booking and customer management access"
}'
```

---

## Event Produced

- RoleCreated
