## Description

Creates a bookable service under an organization. `slug` must be unique within the owning organization. `interfaceLanguage`, `periodType`, `timezone`, `currency`, `requiresConfirmation`, `disableCancelling`, `disableRescheduling`, and `isInstantService` fall back to their platform defaults (`en`, `UNLIMITED`, `Australia/Sydney`, `USD`, `false`, `false`, `false`, `false`) when omitted. `destinationCalendarId`/`destinationSheetsId` are optional — only resolved when supplied.

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
	"destinationSheetsId": 0
}
```

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
	"createdAt": "",
	"updatedAt": ""
}
```

---

## Possible Errors

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 USER_NOT_FOUND
- 404 PROFILE_NOT_FOUND
- 404 ORGANIZATION_NOT_FOUND
- 404 SCHEDULE_NOT_FOUND
- 404 DESTINATION_CALENDAR_NOT_FOUND
- 404 DESTINATION_SHEETS_NOT_FOUND
- 409 SERVICE_SLUG_ALREADY_EXISTS
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "SERVICE_SLUG_ALREADY_EXISTS"
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
	"userId": 1,
	"profileId": 1,
	"organizationId": 1,
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
	"destinationSheetsId": null
}'
```

---

## Event Produced

- ServiceCreated
