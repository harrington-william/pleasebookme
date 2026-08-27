## Description

Adds an attendee to an existing booking. `email` and `timezone` are optional; `locale` is optional and has no platform default — a missing value stays unset. `noShow` defaults to `false` when omitted.

---

## Endpoint

```json
POST /api/v1/attendees
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
	"bookingId": 0,
	"email": "",
	"phone": "",
	"name": "",
	"locale": "",
	"timezone": "",
	"noShow": false
}
```

## Successful Response

```json
201 Created
```

```json
{
	"attendeeId": 0,
	"bookingId": 0,
	"email": "",
	"phone": "",
	"name": "",
	"locale": "",
	"timezone": "",
	"noShow": false,
	"createdAt": ""
}
```

---

## Possible Errors

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 BOOKING_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "BOOKING_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X POST \
<https://api.pleasebookme.com/api/v1/attendees> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "bookingId": 1,
	"email": "harrington@gmail.com",
	"phone": "1742092018",
	"name": "Harrington William",
	"locale": "en",
	"timezone": "America/Los_Angeles",
	"noShow": false
}'
```

---

## Event Produced

- AttendeeCreated
