## Description

Retrieves a single organization by its numeric ID.

---

## Endpoint

```json
GET /api/v1/organizations/{organizationId}
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
	"organizationId": 0,
	"name": "",
	"slug": "",
	"logoUrl": "",
	"bannerUrl": "",
	"bio": "",
	"isPrivate": false,
	"timezone": "",
	"weekStart": "",
	"createdAt": "",
	"updatedAt": ""
}
```

---

## Possible Errors

- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 ORGANIZATION_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "ORGANIZATION_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X GET \
<https://api.pleasebookme.com/api/v1/organizations/1> \
-H "Authorization: Bearer xxx"
```
