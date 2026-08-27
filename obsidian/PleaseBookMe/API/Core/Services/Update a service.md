## Description

Full-replace semantics — the caller is expected to send the complete resource. `userId`, `profileId`, `organizationId`, and `scheduleId` are re-resolved on every update. `destinationCalendarId`/`destinationSheetsId` are cleared when omitted, not left unchanged — there is no "preserve the current value" fallback for these two. No uniqueness re-check is performed on `slug` at update time.

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
- 404 ORGANIZATION_NOT_FOUND
- 404 SCHEDULE_NOT_FOUND
- 404 DESTINATION_CALENDAR_NOT_FOUND
- 404 DESTINATION_SHEETS_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "SERVICE_NOT_FOUND"
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
	"userId": 1,
	"profileId": 1,
	"organizationId": 1,
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
