## Description

Returns every slot a customer may choose for the given service on the given calendar date, as of the moment of the call.

`date` is a calendar date **in the service's schedule timezone**, and that zone is echoed back as `timezone`. The slots themselves are UTC instants and may fall on a different UTC calendar date — a 09:00 `Australia/Sydney` slot is `T23:00:00Z` the previous day. Filter and group by the returned `timezone`, never by the UTC date of `slotStart`.

Slot length and spacing come from the service's booking policy (`defaultDuration`, `slotInterval`, in minutes). A slot is removed when its start is earlier than now plus the policy's `minimumNotice` or later than now plus `maximumAdvanceBooking` — so slots that have already started are never returned, and `bookingWindowType` currently has no effect (`FIXED` behaves as `ROLLING`).

A slot is also removed when it overlaps, after applying the policy's `beforeBuffer`/`afterBuffer`, a `PENDING`, `ACCEPTED` or `AWAITING_HOST` booking or an unexpired hold against **any service owned by the same host**, or when it overlaps (unbuffered) an out-of-office period of the host. Cancelled, rejected and soft-deleted bookings, and expired holds, do not block. Touching intervals do not conflict.

An empty `slots` array is a normal answer, not an error: a weekday with no availability window, a fully booked day, and a schedule with no windows all return `200` with `[]`. The only `404`s are an unknown service and a service that has no booking policy.

The endpoint is idempotent and writes nothing. Two calls may differ only because time passed or because bookings, holds, or absences changed in between.

---

## Endpoint

```json
GET /api/v1/slots
```

---

## Authentication

Required **Bearer Token** — a user access token or a widget access token from `POST /api/v1/auth/widget/bootstrap`. No further scoping is applied.

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
| `serviceId` | Yes | The service to compute slots for. |
| `date` | Yes | ISO calendar date (`YYYY-MM-DD`), interpreted in the service's schedule timezone. |

## Successful Response

```json
200 OK
```

```json
{
	"serviceId": 0,
	"date": "",
	"timezone": "",
	"slots": [
		{
			"slotStart": "",
			"slotEnd": ""
		}
	]
}
```

`slots` is sorted ascending by `slotStart` and contains no duplicates. `slotStart`/`slotEnd` are ISO-8601 UTC instants, the same format the Bookings API uses for `startTime`/`endTime`.

---

## Possible Errors

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 404 SERVICE_NOT_FOUND
- 404 BOOKING_POLICY_NOT_FOUND

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
-X GET \
<https://api.pleasebookme.com/api/v1/slots?serviceId=7&date=2026-09-16> \
-H "Authorization: Bearer xxx"
```

Example response for a `Australia/Sydney` schedule open 09:00–17:00 with a 30-minute policy (truncated):

```json
{
	"serviceId": 7,
	"date": "2026-09-16",
	"timezone": "Australia/Sydney",
	"slots": [
		{ "slotStart": "2026-09-15T23:00:00Z", "slotEnd": "2026-09-15T23:30:00Z" },
		{ "slotStart": "2026-09-15T23:30:00Z", "slotEnd": "2026-09-16T00:00:00Z" },
		{ "slotStart": "2026-09-16T00:00:00Z", "slotEnd": "2026-09-16T00:30:00Z" }
	]
}
```

---

## Event Produced

- None
