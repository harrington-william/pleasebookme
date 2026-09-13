## Description

Retrieves a single selected slot by its numeric ID.

---

## Endpoint

```json
GET /api/v1/selected-slots/{selectedSlotId}
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
```

---

## Possible Errors

- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 SELECTED_SLOT_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "SELECTED_SLOT_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X GET \
<https://api.pleasebookme.com/api/v1/selected-slots/1> \
-H "Authorization: Bearer xxx"
```
