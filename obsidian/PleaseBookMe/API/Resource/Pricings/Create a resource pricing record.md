## Description

Creates a price record for a resource, effective from a given instant and optionally until another. `currency` falls back to `USD` when omitted. There is no uniqueness constraint — a resource may have overlapping price records, the caller is responsible for avoiding conflicting windows.

---

## Endpoint

```json
POST /api/v1/resource-pricing
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
	"resourceId": 0,
	"price": 0,
	"currency": "",
	"effectiveFrom": "",
	"effectiveUntil": ""
}
```

## Successful Response

```json
201 Created
```

```json
{
	"resourcePricingId": 0,
	"resourceId": 0,
	"price": 0,
	"currency": "",
	"effectiveFrom": "",
	"effectiveUntil": "",
	"createdAt": "",
	"updatedAt": ""
}
```

---

## Possible Errors

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 RESOURCE_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "RESOURCE_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X POST \
<https://api.pleasebookme.com/api/v1/resource-pricing> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "resourceId": 1,
	"price": 45.00,
	"currency": "USD",
	"effectiveFrom": "2026-09-01T00:00:00Z",
	"effectiveUntil": null
}'
```

---

## Event Produced

- ResourcePricingCreated
