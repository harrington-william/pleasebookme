## Description

Creates a bookable service under the authenticated user's organization. `userId`, `profileId`, and `organizationId` are server-derived from `CurrentPrincipalProvider.requireUser().userId()` and the caller's `Profile`; they are no longer request fields. Current binding ignores legacy ownership fields if a client still sends them, but they do not affect the created row.

`slug` must be unique within the derived organization. `interfaceLanguage`, `periodType`, `timezone`, `currency`, `requiresConfirmation`, `disableCancelling`, `disableRescheduling`, and `isInstantService` fall back to their platform defaults (`en`, `UNLIMITED`, `Australia/Sydney`, `USD`, `false`, `false`, `false`, `false`) when omitted. `destinationCalendarId`/`destinationSheetsId` are optional and only resolved when supplied.

When `bookingPolicy` is supplied, the service and booking policy are persisted in one transaction. If policy creation fails, neither row commits. The nested booking policy shape intentionally omits `serviceId`; the server links it to the newly created service.

Known limitation: profile/organization derivation requires the caller to have exactly one `Profile`. A caller with profiles in more than one organization gets `409 AMBIGUOUS_SERVICE_OWNER` — the endpoint has no request-scoped organization context to disambiguate which one this request concerns, until the platform adds one.

---

## Endpoint

```json
POST /api/v1/services
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
	"destinationSheetsId": 0,
	"bookingPolicy": {
		"bookingMode": "FIXED",
		"defaultDuration": 0,
		"minimumDuration": 0,
		"maximumDuration": 0,
		"minimumNotice": 0,
		"maximumAdvanceBooking": 0,
		"slotInterval": 0,
		"beforeBuffer": 0,
		"afterBuffer": 0,
		"allowOverlap": false,
		"allowMultipleAttendee": false,
		"requiresPayment": false,
		"autoConfirm": true,
		"bookingWindowType": "",
		"capacity": 0
	}
}
```

`bookingPolicy` may be omitted or set to `null` to create only the service.

## Successful Response

```json
201 Created
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
	"bookingPolicy": {
		"bookingPolicyId": 0,
		"serviceId": 0,
		"bookingMode": "FIXED",
		"defaultDuration": 0,
		"minimumDuration": 0,
		"maximumDuration": 0,
		"minimumNotice": 0,
		"maximumAdvanceBooking": 0,
		"slotInterval": 0,
		"beforeBuffer": 0,
		"afterBuffer": 0,
		"allowOverlap": false,
		"allowMultipleAttendee": false,
		"requiresPayment": false,
		"autoConfirm": true,
		"bookingWindowType": "",
		"capacity": 0,
		"createdAt": "",
		"updatedAt": ""
	},
	"createdAt": "",
	"updatedAt": ""
}
```

`bookingPolicy` is `null` when no nested policy was supplied.

---

## Possible Errors

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 USER_NOT_FOUND
- 404 PROFILE_NOT_FOUND
- 404 SCHEDULE_NOT_FOUND
- 404 DESTINATION_CALENDAR_NOT_FOUND
- 404 DESTINATION_SHEETS_NOT_FOUND
- 409 SERVICE_SLUG_ALREADY_EXISTS
- 409 BOOKING_POLICY_ALREADY_EXISTS
- 409 AMBIGUOUS_SERVICE_OWNER
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"status": "error",
	"message": "Slug already exists for organization: haircut-and-style",
	"data": null,
	"client": "127.0.0.1",
	"timestamp": "2026-08-29T00:00:00Z",
	"path": "/api/v1/services"
}
```

---

## Example Request

```bash
curl \
-X POST \
<https://api.pleasebookme.com/api/v1/services> \
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
	"maxPrice": 45.00,
	"currency": "USD",
	"requiresConfirmation": false,
	"disableCancelling": false,
	"disableRescheduling": false,
	"successRedirectUrl": "https://barbershop.com/thank-you",
	"isInstantService": false,
	"maxActiveBookingPerBooker": 1,
	"destinationCalendarId": null,
	"destinationSheetsId": null,
	"bookingPolicy": {
		"bookingMode": "FIXED",
		"defaultDuration": 60,
		"minimumDuration": null,
		"maximumDuration": null,
		"minimumNotice": 60,
		"maximumAdvanceBooking": 43200,
		"slotInterval": 30,
		"beforeBuffer": 0,
		"afterBuffer": 0,
		"allowOverlap": false,
		"allowMultipleAttendee": false,
		"requiresPayment": false,
		"autoConfirm": true,
		"bookingWindowType": "ROLLING",
		"capacity": 1
	}
}'
```

---

## Event Produced

- ServiceCreated
