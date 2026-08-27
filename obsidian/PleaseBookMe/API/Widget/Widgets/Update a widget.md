## Description

Full-replace semantics — the caller is expected to send the complete resource. `status`, `type`, and `originValidation` are only overwritten when supplied; a missing field keeps its current stored value. `publicKey` is not re-checked for uniqueness on update.

---

## Endpoint

```json
PUT /api/v1/widgets/{widgetId}
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
	"tenantId": 0,
	"name": "",
	"status": "",
	"type": "",
	"originValidation": false,
	"publicKey": "",
	"secretKey": "",
	"expiresAt": "",
	"lastUsedAt": ""
}
```

## Successful Response

```json
200 OK
```

```json
{
	"widgetId": 0,
	"widgetUid": "",
	"tenantId": 0,
	"name": "",
	"status": "",
	"type": "",
	"originValidation": false,
	"publicKey": "",
	"issuedAt": "",
	"expiresAt": "",
	"lastUsedAt": "",
	"createdAt": "",
	"updatedAt": ""
}
```

---

## Possible Errors

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 WIDGET_NOT_FOUND
- 404 TENANT_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "WIDGET_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X PUT \
<https://api.pleasebookme.com/api/v1/widgets/1> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "tenantId": 1,
	"name": "Barbershop Booking Widget",
	"status": "ACTIVE",
	"type": "EMBEDDED",
	"originValidation": true,
	"publicKey": "pk_live_51H8x2K",
	"secretKey": "sk_live_9f8a7b6c5d4e",
	"expiresAt": "2027-08-24T00:00:00Z"
}'
```

---

## Event Produced

- WidgetUpdated
