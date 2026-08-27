## Description

Full-replaces an existing resource type. `organizationId` is re-resolved on every update, so a resource type can be moved to a different organization.

---

## Endpoint

```json
PUT /api/v1/resource-types/{resourceTypeId}
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
200 OK
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
- 404 RESOURCE_TYPE_NOT_FOUND
- 404 ORGANIZATION_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "RESOURCE_TYPE_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X PUT \
<https://api.pleasebookme.com/api/v1/resource-types/1> \
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

- ResourceTypeUpdated
