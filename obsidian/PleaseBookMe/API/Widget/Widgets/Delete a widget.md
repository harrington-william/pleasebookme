# Delete a widget

## Description

Soft-deletes a widget by setting its status to `REVOKED`. The widget row and registered origins remain for direct reads and audit history, while the widget disappears from list results and the active total.

The operation is idempotent: deleting an already revoked widget returns `204` without another write.

---

## Endpoint

```json
DELETE /api/v1/widgets/{widgetId}
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
204 No Content
```

---

## Possible Errors

- 401 unauthenticated
- 404 widget not found

---

## Event Produced

- WidgetRevoked
