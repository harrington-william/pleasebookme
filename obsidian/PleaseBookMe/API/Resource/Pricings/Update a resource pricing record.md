## Description

Full-replaces an existing resource pricing record. `resourceId` is re-resolved on every update.

---

## Endpoint

```json
PUT /api/v1/resource-pricing/{resourcePricingId}
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
200 OK
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
- 404 RESOURCE_PRICING_NOT_FOUND
- 404 RESOURCE_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "RESOURCE_PRICING_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X PUT \
<https://api.pleasebookme.com/api/v1/resource-pricing/1> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "resourceId": 1,
	"price": 50.00,
	"currency": "USD",
	"effectiveFrom": "2026-09-01T00:00:00Z",
	"effectiveUntil": null
}'
```

---

## Event Produced

- ResourcePricingUpdated
