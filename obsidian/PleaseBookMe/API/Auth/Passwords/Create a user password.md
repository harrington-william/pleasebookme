## Description

Creates the password credential for an existing user. A user may only have one password row — `userId` doubles as this table's primary key, so a second `create` call for the same user is rejected rather than overwritten.

---

## Endpoint

```json
POST /api/v1/passwords
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
	"password": ""
}
```

## Successful Response

```json
201 Created
```

```json
{
	"userId": 0,
	"hash": "",
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
- 409 PASSWORD_ALREADY_EXISTS
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "PASSWORD_ALREADY_EXISTS"
}
```

---

## Example Request

```bash
curl \
-X POST \
<https://api.pleasebookme.com/api/v1/passwords> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "userId": 1,
	"password": "S3cur3P@ssword"
}'
```

---

## Event Produced

- UserPasswordCreated
