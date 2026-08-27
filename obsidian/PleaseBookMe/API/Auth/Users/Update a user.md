## Description

Full-replace semantics — the caller is expected to send the complete resource. `locale`, `timezone`, `theme`, and `weekStart` are only overwritten when supplied; a missing field keeps its current stored value. No uniqueness re-check is performed on `username`/`email`/`phone` at update time.

---

## Endpoint

```json
PUT /api/v1/users/{userId}
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
	"username": "",
	"name": "",
	"email": "",
	"phone": "",
	"bio": "",
	"avatarUrl": "",
	"locale": "",
	"timezone": "",
	"theme": "",
	"weekStart": ""
}
```

## Successful Response

```json
200 OK
```

```json
{
	"userId": 0,
	"userUid": "",
	"username": "",
	"name": "",
	"email": "",
	"phone": "",
	"bio": "",
	"avatarUrl": "",
	"locale": "",
	"timezone": "",
	"theme": "",
	"weekStart": "",
	"accountStatus": "",
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
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "USER_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X PUT \
<https://api.pleasebookme.com/api/v1/users/1> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "username": "harrington",
		"name": "Harrington William",
		"email": "harrington@gmail.com",
		"phone": "1742092018",
		"bio": "",
		"avatarUrl": "",
		"locale": "en",
		"timezone": "America/Los_Angeles",
		"theme": "DARK",
		"weekStart": "MONDAY"
}'
```

---

## Event Produced

- UserUpdated
