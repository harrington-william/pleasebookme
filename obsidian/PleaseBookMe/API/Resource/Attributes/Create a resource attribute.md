## Description

Creates a key/value attribute for a resource. The pair `(resourceId, key)` is this table's primary key — a second `create` call for the same pair is rejected rather than overwritten.

---

## Endpoint

```json
POST /api/v1/resource-attributes
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
201 Created
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
- 404 RESOURCE_NOT_FOUND
- 409 RESOURCE_ATTRIBUTE_ALREADY_EXISTS
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "RESOURCE_ATTRIBUTE_ALREADY_EXISTS"
}
```

---

## Example Request

```bash
curl \
-X POST \
<https://api.pleasebookme.com/api/v1/resource-attributes> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "resourceId": 1,
	"key": "color",
	"value": "black"
}'
```

---

## Event Produced

- ResourceAttributeCreated
