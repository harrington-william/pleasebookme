## Description

Replaces the `value` of an existing resource attribute. The composite key `(resourceId, key)` is taken from the path and cannot be changed — only `value` is mutable.

---

## Endpoint

```json
PUT /api/v1/resource-attributes/{resourceId}/{key}
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
	"key": "",
	"value": ""
}
```

## Successful Response

```json
200 OK
```

```json
{
	"resourceId": 0,
	"key": "",
	"value": "",
	"createdAt": "",
	"updatedAt": ""
}
```

---

## Possible Errors

- 400 INVALID_REQUEST
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
-X PUT \
<https://api.pleasebookme.com/api/v1/resource-attributes/1/color> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "resourceId": 1,
	"key": "color",
	"value": "matte black"
}'
```

---

## Event Produced

- ResourceAttributeUpdated
