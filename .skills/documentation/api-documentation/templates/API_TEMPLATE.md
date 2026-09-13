# API Template

# Create a user

## Description

Creates a user with a unique username, email, and phone. `locale`, `timezone`, `theme`, and `weekStart` fall back to their platform defaults (`en`, `Australia/Sydney`, `LIGHT`, `MONDAY`) when omitted.

---

## Endpoint

```json
POST /api/v1/users
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
201 Created
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
- 409 USERNAME_ALREADY_EXISTS
- 409 EMAIL_ALREADY_EXISTS
- 409 PHONE_ALREADY_EXISTS
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "EMAIL_ALREADY_EXISTS"
}
```

---

## Example Request

```bash
curl \
-X POST \
<https://api.pleasebookme.com/api/v1/users> \
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

- UserCreated