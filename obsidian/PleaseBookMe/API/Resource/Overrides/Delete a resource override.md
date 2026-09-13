## Description

Deletes a resource override.

---

## Endpoint

```json
DELETE /api/v1/resource-overrides/{resourceOverrideId}
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

- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 RESOURCE_OVERRIDE_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "RESOURCE_OVERRIDE_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X DELETE \
<https://api.pleasebookme.com/api/v1/resource-overrides/1> \
-H "Authorization: Bearer xxx"
```

---

## Event Produced

- ResourceOverrideDeleted
