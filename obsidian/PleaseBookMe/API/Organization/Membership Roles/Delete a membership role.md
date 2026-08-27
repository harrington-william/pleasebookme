## Description

Revokes a role from a membership by the composite key of `membershipId` and `roleId`.

---

## Endpoint

```json
DELETE /api/v1/membership-roles/{membershipId}/{roleId}
```

---

## Authentication

Required **Bearer Token**

---

## Headers

```json
{
	"Authorization": "JWT Access Token"
}
```

## Successful Response

```json
204 No Content
```

---

## Possible Errors

- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 MEMBERSHIP_ROLE_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "MEMBERSHIP_ROLE_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X DELETE \
<https://api.pleasebookme.com/api/v1/membership-roles/1/2> \
-H "Authorization: Bearer xxx"
```

---

## Event Produced

- MembershipRoleDeleted
