## Description

Retrieves the authenticated caller's services for the requested organization. The `organizationId` query parameter must match the caller's server-derived organization, resolved from the current user principal and profile. If the requested organization does not match the caller's organization, the endpoint returns an empty array instead of `403` or `404` so callers cannot probe whether another organization exists or has services.

Callers with profiles in more than one organization currently receive `409 AMBIGUOUS_SERVICE_OWNER`, matching the create/update service ownership resolution behavior.

---

## Endpoint

```json
GET /api/v1/services?organizationId={organizationId}
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

## Body

```json
{}
```

## Successful Response

```json
200 OK
```

```json
[
	{
		"serviceId": 0,
		"title": "",
		"slug": "",
		"description": "",
		"interfaceLanguage": "en",
		"location": "",
		"userId": 0,
		"profileId": 0,
		"organizationId": 0,
		"scheduleId": 0,
		"periodType": "",
		"timezone": "",
		"minPrice": 0,
		"maxPrice": 0,
		"currency": "USD",
		"disableCancelling": false,
		"disableRescheduling": false,
		"successRedirectUrl": "",
		"isInstantService": false,
		"maxActiveBookingPerBooker": 0,
		"destinationCalendarId": 0,
		"destinationSheetsId": 0,
		"bookingPolicy": null,
		"createdAt": "",
		"updatedAt": ""
	}
]
```

A valid request for a non-matching organization also returns `200 OK` with an empty array:

```json
[]
```

---

## Possible Errors

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 409 AMBIGUOUS_SERVICE_OWNER
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
<https://api.pleasebookme.com/api/v1/services?organizationId=7> \
-H "Authorization: Bearer xxx"
```

---

## Event Produced

- None
