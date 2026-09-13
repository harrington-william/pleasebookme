## Description

Creates a membership linking a user to an organization. A user may only have one membership per organization. `accepted` falls back to `false` when omitted — self-membership (a user joining their own organization) typically supplies `true` since it needs no invite/acceptance step.

---

## Endpoint

```json
POST /api/v1/memberships
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
	"userId": 0,
	"accepted": false
}
```

## Successful Response

```json
201 Created
```

```json
{
	"membershipId": 0,
	"organizationId": 0,
	"userId": 0,
	"accepted": false,
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
- 404 USER_NOT_FOUND
- 409 MEMBERSHIP_ALREADY_EXISTS
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "MEMBERSHIP_ALREADY_EXISTS"
}
```

---

## Example Request

```bash
curl \
-X POST \
<https://api.pleasebookme.com/api/v1/memberships> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "organizationId": 1,
	"userId": 1,
	"accepted": true
}'
```

---

## Event Produced

- MembershipCreated
