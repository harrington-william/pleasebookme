## Description

Creates a new organization with a unique `slug`. `isPrivate`, `timezone`, and `weekStart` fall back to their platform defaults (`false`, `Australia/Sydney`, `MONDAY`) when omitted.

---

## Endpoint

```json
POST /api/v1/organizations
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
	"name": "",
	"slug": "",
	"logoUrl": "",
	"bannerUrl": "",
	"bio": "",
	"isPrivate": false,
	"timezone": "",
	"weekStart": ""
}
```

## Successful Response

```json
201 Created
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

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 409 ORGANIZATION_SLUG_ALREADY_EXISTS
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "ORGANIZATION_SLUG_ALREADY_EXISTS"
}
```

---

## Example Request

```bash
curl \
-X POST \
<https://api.pleasebookme.com/api/v1/organizations> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "name": "Harrington Barbershop",
	"slug": "harrington-barbershop",
	"logoUrl": "",
	"bannerUrl": "",
	"bio": "",
	"isPrivate": false,
	"timezone": "Australia/Sydney",
	"weekStart": "MONDAY"
}'
```

---

## Event Produced

- OrganizationCreated
