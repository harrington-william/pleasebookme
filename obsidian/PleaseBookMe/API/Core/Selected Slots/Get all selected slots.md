## Description

Retrieves every selected slot.

---

## Endpoint

```json
GET /api/v1/selected-slots
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
		"selectedSlotId": 0,
		"selectedSlotUid": "",
		"serviceId": 0,
		"userId": 0,
		"slotStart": "",
		"slotEnd": "",
		"releaseAt": "",
		"isSeat": false,
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
<https://api.pleasebookme.com/api/v1/selected-slots> \
-H "Authorization: Bearer xxx"
```
