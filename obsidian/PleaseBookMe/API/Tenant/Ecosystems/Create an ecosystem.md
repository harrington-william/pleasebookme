## Description

Creates an ecosystem — a business-category lookup value (e.g. barbershop, rental, court booking) that a tenant is classified under. `status` falls back to its platform default (`REVIEWING`) when omitted.

---

## Endpoint

```json
POST /api/v1/ecosystems
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
201 Created
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
- 409 ECOSYSTEM_CODE_ALREADY_EXISTS
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "ECOSYSTEM_CODE_ALREADY_EXISTS"
}
```

---

## Example Request

```bash
curl \
-X POST \
<https://api.pleasebookme.com/api/v1/ecosystems> \
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

- EcosystemCreated
