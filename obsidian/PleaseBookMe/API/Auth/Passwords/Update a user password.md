## Description

Replaces a user's existing password. The new `password` must differ from the currently stored value — submitting the same value is rejected as a conflict rather than silently accepted.

---

## Endpoint

```json
PUT /api/v1/passwords/{userId}
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
200 OK
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
- 404 USER_PASSWORD_NOT_FOUND
- 409 PASSWORD_UNCHANGED
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "PASSWORD_UNCHANGED"
}
```

---

## Example Request

```bash
curl \
-X PUT \
<https://api.pleasebookme.com/api/v1/passwords/1> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "userId": 1,
	"password": "N3wS3cur3P@ssword"
}'
```

---

## Event Produced

- UserPasswordUpdated
