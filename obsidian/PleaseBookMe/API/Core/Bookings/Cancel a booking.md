## Description

Marks a booking as cancelled by setting `status` to `CANCELLED`, and returns the updated booking.

The operation is **idempotent** — cancelling a booking that is already `CANCELLED` returns `200 OK` with the row unchanged rather than a conflict. Callers may retry safely.

Two fields are deliberately **not** written by this endpoint:

- `cancelledBy` is left null. Attributing the cancellation requires resolving the calling principal, which this endpoint does not yet do. It will be populated once the booking authorization tier is wired up.
- `cancellationReason` is left untouched. There is no request body; supplying a reason still requires a full-replace `PUT`.

Cancelling is the only status transition with a dedicated endpoint. Every other transition (`ACCEPTED`, `REJECTED`, `AWAITING_HOST`) still goes through `PUT /api/v1/bookings/{bookingId}` with the complete resource.

> **Note on the verb.** This endpoint mutates state on a `GET`, which is unconventional — browser prefetch, link crawlers and caching proxies can trigger a `GET` without a user acting. It is specified this way deliberately. Do not expose this URL as a plain link in a browser context.

---

## Endpoint

```json
GET /api/v1/bookings/{bookingId}/cancel
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
	"bookingId": 0,
	"bookingUid": "",
	"idempotencyKey": "",
	"userId": 0,
	"title": "",
	"description": "",
	"startTime": "",
	"endTime": "",
	"serviceId": 0,
	"location": "",
	"status": "CANCELLED",
	"paid": false,
	"cancelledById": null,
	"cancellationReason": "",
	"rejectionReason": "",
	"rescheduled": false,
	"rescheduledById": null,
	"noShowHost": false,
	"deletedAt": null,
	"deletedById": null,
	"destinationCalendarId": null,
	"destinationSheetsId": null,
	"createdAt": "",
	"updatedAt": ""
}
```

---

## Possible Errors

- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 RESOURCE_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "RESOURCE_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X GET \
<https://api.pleasebookme.com/api/v1/bookings/1/cancel> \
-H "Authorization: Bearer xxx"
```

---

## Event Produced

- BookingCancelled
