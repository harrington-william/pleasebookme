## Description

Assigns an organization membership to a resource. `assignedById`/`releasedById` are optional — when supplied they must reference an existing user. `isPrimary` defaults to `true` when omitted.

**Known gap**: `membershipId` is **not** validated against `organization.memberships` at request time — no `MembershipRepository` exists yet for this domain, so the FK is written directly without an existence check. Supplying an unknown `membershipId` will not produce a clean 404; it surfaces as a raw database foreign-key-violation error instead.

---

## Endpoint

```json
POST /api/v1/resource-assignments
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
201 Created
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
- 404 RESOURCE_NOT_FOUND
- 404 USER_NOT_FOUND
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
-X POST \
<https://api.pleasebookme.com/api/v1/resource-assignments> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "resourceId": 1,
	"membershipId": 1,
	"releasedAt": null,
	"assignedById": 2,
	"releasedById": null,
	"isPrimary": true
}'
```

---

## Event Produced

- ResourceAssignmentCreated
