## Description

Full-replace semantics — the caller is expected to send the complete resource. `organizationId` and `userId` are re-resolved on every update. `accepted` is only overwritten when supplied. No uniqueness re-check is performed on the `(user, organization)` constraint at update time.

---

## Endpoint

```json
PUT /api/v1/memberships/{membershipId}
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
200 OK
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
- 404 MEMBERSHIP_NOT_FOUND
- 404 ORGANIZATION_NOT_FOUND
- 404 USER_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "MEMBERSHIP_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X PUT \
<https://api.pleasebookme.com/api/v1/memberships/1> \
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

- MembershipUpdated
