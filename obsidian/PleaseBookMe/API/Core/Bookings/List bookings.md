## Description

Retrieves one page of bookings belonging to a single organization.

This endpoint replaced the former unscoped "get all bookings" listing. `organizationId` is now **required** — there is no way to retrieve every booking on the platform. Scope is resolved through the booking's service (`booking → service → organization`), because `core.bookings` carries no direct organization column.

Results are filtered by a **tab**, which is a view over the booking's status and time window rather than a stored field. `tab` defaults to `UPCOMING` when omitted, and an unrecognised value silently falls back to `UPCOMING` rather than erroring.

| Tab | Included when |
|---|---|
| `UPCOMING` | `endTime >= now` and status is `PENDING`, `ACCEPTED` or `AWAITING_HOST` |
| `PENDING` | status is `PENDING` or `AWAITING_HOST`, regardless of time |
| `PAST` | `endTime < now` and status is `PENDING`, `ACCEPTED` or `AWAITING_HOST` |
| `CANCELLED` | status is `CANCELLED` or `REJECTED` |
| `ALL` | no constraint |

The tabs are deliberately **views, not a partition** — a pending booking next week appears under both `UPCOMING` and `PENDING`. There is no `COMPLETED` status in `core.booking_status`, which is why `PAST` is a time predicate over `endTime` rather than a status check.

`serviceId`, `resourceId` and `q` are optional and compose with each other and with the tab. `q` is a case-insensitive contains-match on `title` only; `%`, `_` and `\` in the search text are escaped and matched literally. `resourceId` matches bookings that have the given resource attached through `core.booking_resources`, primary or not.

Sorting accepts `startTime`, `endTime`, `title`, `status`, `createdAt` and `updatedAt`. Any other property is **ignored rather than rejected**, so a hand-edited `?sort=` cannot produce a 500. Omitting `sort` yields `startTime` descending. Page size defaults to 20 and is capped at 100.

---

## Endpoint

```json
GET /api/v1/bookings
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

## Query Parameters

| Parameter | Required | Description |
|---|---|---|
| `organizationId` | Yes | The organization whose bookings to list. |
| `tab` | No | `UPCOMING` (default), `PENDING`, `PAST`, `CANCELLED`, `ALL`. |
| `serviceId` | No | Only bookings against this service. |
| `resourceId` | No | Only bookings this resource is attached to. |
| `q` | No | Case-insensitive contains-match on `title`. |
| `page` | No | Zero-based page index. Defaults to `0`. |
| `size` | No | Page size. Defaults to `20`, capped at `100`. |
| `sort` | No | `<property>,<asc\|desc>`. Defaults to `startTime,desc`. |

## Successful Response

```json
200 OK
```

```json
{
	"content": [
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
			"status": "",
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
	],
	"page": 0,
	"size": 20,
	"totalElements": 0,
	"totalPages": 0
}
```

---

## Possible Errors

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
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
<https://api.pleasebookme.com/api/v1/bookings?organizationId=1&tab=UPCOMING&page=0&size=20> \
-H "Authorization: Bearer xxx"
```
