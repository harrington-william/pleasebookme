## Description

Retrieves every attendee.

---

## Endpoint

```json
GET /api/v1/attendees
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
[
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
]
```

---

## Possible Errors

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
<https://api.pleasebookme.com/api/v1/attendees> \
-H "Authorization: Bearer xxx"
```
