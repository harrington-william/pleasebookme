## Description

Creates a profile linking a user to an organization under a `username` unique within that organization. A user may only have one profile per organization, and `username` may not collide with another profile's `username` in the same organization.

---

## Endpoint

```json
POST /api/v1/profiles
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
	"userId": 0,
	"organizationId": 0,
	"username": ""
}
```

## Successful Response

```json
201 Created
```

```json
{
	"profileId": 0,
	"profileUid": "",
	"userId": 0,
	"organizationId": 0,
	"username": "",
	"createdAt": "",
	"updatedAt": ""
}
```

---

## Possible Errors

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 USER_NOT_FOUND
- 404 ORGANIZATION_NOT_FOUND
- 409 PROFILE_ALREADY_EXISTS
- 409 PROFILE_USERNAME_ALREADY_EXISTS
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "PROFILE_USERNAME_ALREADY_EXISTS"
}
```

---

## Example Request

```bash
curl \
-X POST \
<https://api.pleasebookme.com/api/v1/profiles> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "userId": 1,
	"organizationId": 1,
	"username": "harrington"
}'
```

---

## Event Produced

- ProfileCreated
