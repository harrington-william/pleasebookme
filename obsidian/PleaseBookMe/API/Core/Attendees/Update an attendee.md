## Description

Full-replaces an existing attendee. `bookingId` is re-resolved on every call, so reassigning an attendee to a different booking is possible. `email`/`locale`/`timezone` are set to whatever the request supplies, including `null`. `noShow` is only overwritten when supplied — a missing field keeps its current stored value.

---

## Endpoint

```json
PUT /api/v1/attendees/{attendeeId}
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
200 OK
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
- 404 ATTENDEE_NOT_FOUND
- 404 BOOKING_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "ATTENDEE_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X PUT \
<https://api.pleasebookme.com/api/v1/attendees/1> \
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
	"noShow": true
}'
```

---

## Event Produced

- AttendeeUpdated
