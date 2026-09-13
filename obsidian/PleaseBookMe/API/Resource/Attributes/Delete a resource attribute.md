## Description

Deletes a resource attribute by its composite key.

---

## Endpoint

```json
DELETE /api/v1/resource-attributes/{resourceId}/{key}
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
- 404 RESOURCE_ATTRIBUTE_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "RESOURCE_ATTRIBUTE_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X DELETE \
<https://api.pleasebookme.com/api/v1/resource-attributes/1/color> \
-H "Authorization: Bearer xxx"
```

---

## Event Produced

- ResourceAttributeDeleted
