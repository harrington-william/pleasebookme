## Description

Full-replaces an existing resource assignment. `resourceId` is re-resolved against `resource.resources`; `assignedById`/`releasedById` are re-resolved against `auth.users` when supplied. Same known gap as create: `membershipId` is not existence-checked.

---

## Endpoint

```json
PUT /api/v1/resource-assignments/{resourceAssignmentId}
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
	"resourceId": 0,
	"membershipId": 0,
	"releasedAt": "",
	"assignedById": 0,
	"releasedById": 0,
	"isPrimary": true
}
```

## Successful Response

```json
200 OK
```

```json
{
	"resourceAssignmentId": 0,
	"resourceId": 0,
	"membershipId": 0,
	"assignedAt": "",
	"releasedAt": "",
	"assignedById": 0,
	"releasedById": 0,
	"isPrimary": true
}
```

---

## Possible Errors

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 RESOURCE_ASSIGNMENT_NOT_FOUND
- 404 RESOURCE_NOT_FOUND
- 404 USER_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "RESOURCE_ASSIGNMENT_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X PUT \
<https://api.pleasebookme.com/api/v1/resource-assignments/1> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "resourceId": 1,
	"membershipId": 1,
	"releasedAt": "2026-09-10T18:00:00Z",
	"assignedById": 2,
	"releasedById": 2,
	"isPrimary": false
}'
```

---

## Event Produced

- ResourceAssignmentUpdated
