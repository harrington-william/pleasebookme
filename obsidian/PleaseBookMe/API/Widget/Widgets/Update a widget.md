# Update a widget

## Description

Replaces the caller-managed widget configuration. `name` is required; a missing `type` keeps the stored type. Only `ACTIVE` and `DISABLED` are applied from `status`; `REGISTERING`, `REVOKED`, and `null` leave the current status unchanged.

Credentials are optional. Omitting them keeps `publicKey`, the stored secret hash, and `issuedAt` unchanged. Supplying them rotates both keys and advances `issuedAt`. A null or blank `origin` removes every registered origin and disables origin validation; a supplied origin is normalized and upserted. Revoked widgets cannot be updated.

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
	"name": "Popup Widget",
	"type": "POPUP",
	"status": "DISABLED",
	"origin": "https://example.com/path",
	"credentials": null
}
```

## Successful Response

```json
200 OK
```

The response uses the same field-for-field `WidgetResponse` shape documented in [[Get a widget]]. It never contains `secretKey`.

---

## Possible Errors

- 400 invalid request or origin
- 401 unauthenticated
- 404 widget not found
- 409 widget is revoked or rotated public key already exists

---

## Event Produced

- WidgetUpdated
