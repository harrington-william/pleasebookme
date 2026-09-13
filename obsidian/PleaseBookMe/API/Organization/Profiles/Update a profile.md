## Description

Full-replace semantics — the caller is expected to send the complete resource. `userId` and `organizationId` are re-resolved on every update (a profile can be reassigned to a different user/organization). No uniqueness re-check is performed on the `(user, organization)` or `(username, organization)` constraints at update time.

---

## Endpoint

```json
PUT /api/v1/profiles/{profileId}
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
200 OK
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
- 404 PROFILE_NOT_FOUND
- 404 USER_NOT_FOUND
- 404 ORGANIZATION_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "PROFILE_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X PUT \
<https://api.pleasebookme.com/api/v1/profiles/1> \
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

- ProfileUpdated
