## Description

Creates a resource type in the caller's current organization. `description` and `icon` are optional. `(organization_id, name)` is unique in Postgres; a pre-flush duplicate handler is intentionally deferred, so a duplicate currently surfaces from the database constraint.

---

## Endpoint

```json
POST /api/v1/resource-types
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
	"icon": ""
}
```

## Successful Response

```json
201 Created
```

```json
{
	"resourceTypeId": 0,
	"organizationId": 0,
	"name": "",
	"description": "",
	"icon": "",
	"createdAt": "",
	"updatedAt": ""
}
```

---

## Possible Errors

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "ORGANIZATION_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X POST \
<https://api.pleasebookme.com/api/v1/resource-types> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
	"name": "Barber Chair",
	"description": "A single-seat barber station",
	"icon": "chair"
}'
```

---

## Event Produced

- ResourceTypeCreated
