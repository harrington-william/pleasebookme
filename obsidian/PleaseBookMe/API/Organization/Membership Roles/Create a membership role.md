## Description

Assigns a role to a membership. Rejected if that membership already holds that role.

---

## Endpoint

```json
POST /api/v1/membership-roles
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
	"membershipId": 0,
	"roleId": 0
}
```

## Successful Response

```json
201 Created
```

```json
{
	"membershipId": 0,
	"roleId": 0,
	"assignedAt": ""
}
```

---

## Possible Errors

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 MEMBERSHIP_NOT_FOUND
- 404 ROLE_NOT_FOUND
- 409 MEMBERSHIP_ROLE_ALREADY_EXISTS
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "MEMBERSHIP_ROLE_ALREADY_EXISTS"
}
```

---

## Example Request

```bash
curl \
-X POST \
<https://api.pleasebookme.com/api/v1/membership-roles> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "membershipId": 1,
	"roleId": 2
}'
```

---

## Event Produced

- MembershipRoleCreated
