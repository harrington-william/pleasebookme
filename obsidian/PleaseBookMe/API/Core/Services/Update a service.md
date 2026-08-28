## Description

Full-replace semantics — the caller is expected to send the complete service resource. `userId`, `profileId`, and `organizationId` are server-derived on every update from `CurrentPrincipalProvider.requireUser().userId()` and the caller's `Profile`; they are no longer request fields. Current binding ignores legacy ownership fields if a client still sends them, but they do not affect the updated row.

`scheduleId` is still supplied by the client and re-resolved on update. `destinationCalendarId`/`destinationSheetsId` are cleared when omitted, not left unchanged. No uniqueness re-check is performed on `slug` at update time. Booking policy updates stay on `PUT /api/v1/booking-policies/{bookingPolicyId}`; the nested `bookingPolicy` field is only meaningful for service creation.

The caller must belong to the organization that already owns the service — the derived organization is only used to authorize the update, never to reassign it. A caller from a different organization gets `404 SERVICE_NOT_FOUND`, identical to updating a service that doesn't exist at all; the two cases are deliberately indistinguishable so the endpoint can't be used to probe which service IDs exist.

Known limitation: profile/organization derivation requires the caller to have exactly one `Profile`. A caller with profiles in more than one organization gets `409 AMBIGUOUS_SERVICE_OWNER` — the endpoint has no request-scoped organization context to disambiguate which one this request concerns, until the platform adds one.

---

## Endpoint

```json
PUT /api/v1/services/{serviceId}
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
	"title": "",
	"slug": "",
	"description": "",
	"interfaceLanguage": "",
	"location": "",
	"scheduleId": 0,
	"periodType": "",
	"timezone": "",
	"minPrice": 0,
	"maxPrice": 0,
	"currency": "",
	"requiresConfirmation": false,
	"disableCancelling": false,
	"disableRescheduling": false,
	"successRedirectUrl": "",
	"isInstantService": false,
	"maxActiveBookingPerBooker": 0,
	"destinationCalendarId": 0,
	"destinationSheetsId": 0
}
```

## Successful Response

```json
200 OK
```

```json
{
	"serviceId": 0,
	"title": "",
	"slug": "",
	"description": "",
	"interfaceLanguage": "",
	"location": "",
	"userId": 0,
	"profileId": 0,
	"organizationId": 0,
	"scheduleId": 0,
	"periodType": "",
	"timezone": "",
	"minPrice": 0,
	"maxPrice": 0,
	"currency": "",
	"requiresConfirmation": false,
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
```

---

## Possible Errors

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 SERVICE_NOT_FOUND
- 404 USER_NOT_FOUND
- 404 PROFILE_NOT_FOUND
- 404 SCHEDULE_NOT_FOUND
- 404 DESTINATION_CALENDAR_NOT_FOUND
- 404 DESTINATION_SHEETS_NOT_FOUND
- 409 AMBIGUOUS_SERVICE_OWNER
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"status": "error",
	"message": "Service not found: 1",
	"data": null,
	"client": "127.0.0.1",
	"timestamp": "2026-08-29T00:00:00Z",
	"path": "/api/v1/services/1"
}
```

---

## Example Request

```bash
curl \
-X PUT \
<https://api.pleasebookme.com/api/v1/services/1> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
	"title": "Haircut & Style",
	"slug": "haircut-and-style",
	"description": "A classic cut and style session",
	"interfaceLanguage": "en",
	"location": "123 Main Street",
	"scheduleId": 1,
	"periodType": "UNLIMITED",
	"timezone": "Australia/Sydney",
	"minPrice": 20.00,
	"maxPrice": 50.00,
	"currency": "USD",
	"requiresConfirmation": false,
	"disableCancelling": false,
	"disableRescheduling": false,
	"successRedirectUrl": "https://barbershop.com/thank-you",
	"isInstantService": false,
	"maxActiveBookingPerBooker": 1,
	"destinationCalendarId": null,
	"destinationSheetsId": null
}'
```

---

## Event Produced

- ServiceUpdated
