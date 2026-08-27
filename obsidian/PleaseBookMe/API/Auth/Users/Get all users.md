## Description

---

## Endpoint

```json
GET /api/v1/users
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
200 OK
```

```json
[
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
]
```

---

## Possible Errors

- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "UNAUTHORIZED"
}
```

---

## Example Request

```bash
curl \
-X GET \
<https://api.pleasebookme.com/api/v1/users> \
-H "Authorization: Bearer xxx"
```
