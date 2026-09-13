## Description

Full-replace semantics — the caller is expected to send the complete resource. `isPrivate`, `timezone`, and `weekStart` are only overwritten when supplied; a missing field keeps its current stored value. No uniqueness re-check is performed on `slug` at update time.

---

## Endpoint

```json
PUT /api/v1/organizations/{organizationId}
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

- 400 INVALID_REQUEST
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
-X PUT \
<https://api.pleasebookme.com/api/v1/organizations/1> \
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

- OrganizationUpdated
