## Description

Retrieves a single attendee by its numeric ID.

---

## Endpoint

```json
GET /api/v1/attendees/{attendeeId}
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

- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 ATTENDEE_NOT_FOUND
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
-X GET \
<https://api.pleasebookme.com/api/v1/attendees/1> \
-H "Authorization: Bearer xxx"
```
