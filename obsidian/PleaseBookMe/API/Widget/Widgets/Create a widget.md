## Description

Registers a new embeddable widget for a tenant. `publicKey` must be unique across every widget. `status` defaults to `REGISTERING`, `type` defaults to `EMBEDDED`, and `originValidation` defaults to `false` when omitted.

---

## Endpoint

```json
POST /api/v1/widgets
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
201 Created
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
- 404 TENANT_NOT_FOUND
- 409 WIDGET_PUBLIC_KEY_ALREADY_EXISTS
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "WIDGET_PUBLIC_KEY_ALREADY_EXISTS"
}
```

---

## Example Request

```bash
curl \
-X POST \
<https://api.pleasebookme.com/api/v1/widgets> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "tenantId": 1,
	"name": "Barbershop Booking Widget",
	"status": "REGISTERING",
	"type": "EMBEDDED",
	"originValidation": true,
	"publicKey": "pk_live_51H8x2K",
	"secretKey": "sk_live_9f8a7b6c5d4e",
	"expiresAt": "2027-08-24T00:00:00Z"
}'
```

---

## Event Produced

- WidgetCreated
