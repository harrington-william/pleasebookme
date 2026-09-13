## Description

Full-replace semantics — the caller is expected to send the complete resource. `status` is only overwritten when supplied; a missing field keeps its current stored value. No uniqueness re-check is performed on `code` at update time.

---

## Endpoint

```json
PUT /api/v1/ecosystems/{ecosystemId}
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
	"code": "",
	"name": "",
	"description": "",
	"icon": "",
	"status": ""
}
```

## Successful Response

```json
200 OK
```

```json
{
	"ecosystemId": 0,
	"code": "",
	"name": "",
	"description": "",
	"icon": "",
	"status": "",
	"createdAt": "",
	"updatedAt": ""
}
```

---

## Possible Errors

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 ECOSYSTEM_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "ECOSYSTEM_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X PUT \
<https://api.pleasebookme.com/api/v1/ecosystems/1> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "code": "BARBERSHOP",
	"name": "Barbershop",
	"description": "Barbershops and hair salons offering appointment-based services.",
	"icon": "scissors",
	"status": "ACTIVE"
}'
```

---

## Event Produced

- EcosystemUpdated
