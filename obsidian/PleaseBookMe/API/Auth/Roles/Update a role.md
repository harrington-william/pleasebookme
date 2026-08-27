## Description

Full-replace semantics — the caller is expected to send the complete resource. No uniqueness re-check is performed on `name` at update time.

---

## Endpoint

```json
PUT /api/v1/roles/{roleId}
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
200 OK
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
- 404 ROLE_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "ROLE_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X PUT \
<https://api.pleasebookme.com/api/v1/roles/1> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "name": "STAFF",
	"description": "Front-desk staff with booking, customer, and resource management access"
}'
```

---

## Event Produced

- RoleUpdated
