# Get widget organization

## Description

Returns the organization the calling widget belongs to, with every bookable service. Ownership comes from the token alone — there is no slug or id in the request.

`ecosystem` is the `tenant.ecosystems.code` of the widget's tenant (`GENERAL`, `BARBERSHOP`, …). The widget library uses it to pick which ecosystem's component to render.

Services without a booking policy are omitted; they are not bookable. Services are sorted by title.

---

## Endpoint

```json
GET /api/v1/widget/organization
```

---

## Authentication

Required **Bearer Token** (widget)

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
	"name": "",
	"slug": "",
	"logoUrl": "",
	"bannerUrl": "",
	"bio": "",
	"timezone": "",
	"weekStart": "",
	"ecosystem": "",
	"services": [
		{
			"slug": "",
			"title": "",
			"description": "",
			"location": "",
			"durationMinutes": 0,
			"minPrice": 0,
			"maxPrice": 0,
			"currency": "",
			"autoConfirm": true
		}
	]
}
```

---

## Possible Errors

- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Example Request

```bash
curl \
-X GET \
<https://api.pleasebookme.app/api/v1/widget/organization> \
-H "Authorization: Bearer xxx"
```
