## Description

Creates a resource type owned by an organization. `description` is optional free text; there is no uniqueness constraint on `name` — two resource types in the same organization may share a name.

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
	"organizationId": 0,
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
- 404 ORGANIZATION_NOT_FOUND
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
    "organizationId": 1,
	"name": "Barber Chair",
	"description": "A single-seat barber station",
	"icon": "chair"
}'
```

---

## Event Produced

- ResourceTypeCreated
