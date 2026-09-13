## Description

Deletes a booking.

---

## Endpoint

```json
DELETE /api/v1/bookings/{bookingId}
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
204 No Content
```

---

## Possible Errors

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
-X DELETE \
<https://api.pleasebookme.com/api/v1/bookings/1> \
-H "Authorization: Bearer xxx"
```

---

## Event Produced

- BookingDeleted
