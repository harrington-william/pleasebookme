## Description

Retrieves a single membership by its numeric ID.

---

## Endpoint

```json
GET /api/v1/memberships/{membershipId}
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

- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 MEMBERSHIP_NOT_FOUND
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
-X GET \
<https://api.pleasebookme.com/api/v1/memberships/1> \
-H "Authorization: Bearer xxx"
```
