## Description

Retrieves a single profile by its numeric ID.

---

## Endpoint

```json
GET /api/v1/profiles/{profileId}
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

- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 PROFILE_NOT_FOUND
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
-X GET \
<https://api.pleasebookme.com/api/v1/profiles/1> \
-H "Authorization: Bearer xxx"
```
