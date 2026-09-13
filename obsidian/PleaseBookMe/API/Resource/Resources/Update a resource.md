## Description

Full-replaces an existing resource in the caller's current organization. `resourceTypeId` is re-resolved; service assignments remain separate in `/api/v1/resource-services`. `description` and `capacity` may be null. The organization-scoped slug constraint is not re-checked before update.

---

## Endpoint

```json
PUT /api/v1/resources/{resourceId}
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
200 OK
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
- 404 RESOURCE_NOT_FOUND
- 404 RESOURCE_TYPE_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "RESOURCE_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X PUT \
<https://api.pleasebookme.com/api/v1/resources/1> \
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

- ResourceUpdated
