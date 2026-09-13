## Description

Creates a resource in the caller's current organization. `slug` must be unique within that organization. `resourceTypeId` and `status` are required; status must be `ACTIVE`, `INACTIVE`, `MAINTENANCE`, or `RETIRED`. `description` and `capacity` are optional. Service assignments are created separately through `/api/v1/resource-services`. `isBookable` defaults to `true` and `isVirtual` defaults to `false` when omitted.

---

## Endpoint

```json
POST /api/v1/resources
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
	"resourceTypeId": 0,
	"name": "",
	"slug": "",
	"description": "",
	"capacity": 0,
	"status": "",
	"isBookable": true,
	"isVirtual": false
}
```

## Successful Response

```json
201 Created
```

```json
{
	"resourceId": 0,
	"resourceUid": "",
	"organizationId": 0,
	"resourceTypeId": 0,
	"name": "",
	"slug": "",
	"description": "",
	"capacity": 0,
	"status": "",
	"isBookable": true,
	"isVirtual": false,
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
- 409 RESOURCE_SLUG_ALREADY_EXISTS
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "RESOURCE_SLUG_ALREADY_EXISTS"
}
```

---

## Example Request

```bash
curl \
-X POST \
<https://api.pleasebookme.com/api/v1/resources> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
	"resourceTypeId": 1,
	"name": "Chair 1",
	"slug": "chair-1",
	"description": "Barber chair by the window",
	"capacity": 1,
	"status": "ACTIVE",
	"isBookable": true,
	"isVirtual": false
}'
```

---

## Event Produced

- ResourceCreated
