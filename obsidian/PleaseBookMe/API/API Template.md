>**Agents read this to generate API documentation**

## Description

Longer prose explaining behavior, side effects, and any business rules a client needs to know

---

## Endpoint

```json
METHOD /api/v1/<resource>
```

---

## Authentication

Required **Bearer Token** / permitAll / etc.

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
	"field1": "",
	"field2": ""
}
```

## Success Response

```json
<status code> <status name>
```

```json
{
	"response": ""
}
```

---

## Possible Errors

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 RESOURCE_NOT_FOUND
- 409 CONFLICT
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "UNAUTHORIZED"
}
```

---

## Example Request

```bash
curl \
-X METHOD \
<https://api.pleasebookme.com/api/v1/<resource>> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "field1": "value1",
	"field2": "value2"
}'
```

---

## Event Produced

- <ResourceAction>
