# Overview

The Customer API allows application to create, retrieve, update, and delete a tenant's customers

## Base URL

```bash
/api/v1/customers
```

## Protocol

```bash
HTTPS
```

## Response Format

```bash
application/json
```

## Character Encoding

```bash
UTF-8
```

## Authentication

```bash
Every endpoint in this resource requires a **Bearer Token**
```

---

# Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | /api/v1/customers | [[Create a customer]] |
| GET | /api/v1/customers/{customerId} | [[Get a customer]] |
| GET | /api/v1/customers | [[Get all customers]] |
| PUT | /api/v1/customers/{customerId} | [[Update a customer]] |
| DELETE | /api/v1/customers/{customerId} | [[Delete a customer]] |
