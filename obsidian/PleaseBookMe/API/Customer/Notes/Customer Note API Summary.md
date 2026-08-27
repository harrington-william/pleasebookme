# Overview

The Customer Note API allows application to create, retrieve, update, and delete internal notes recorded against a customer

## Base URL

```bash
/api/v1/customer-notes
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
| POST | /api/v1/customer-notes | [[Create a customer note]] |
| GET | /api/v1/customer-notes/{customerNoteId} | [[Get a customer note]] |
| GET | /api/v1/customer-notes | [[Get all customer notes]] |
| PUT | /api/v1/customer-notes/{customerNoteId} | [[Update a customer note]] |
| DELETE | /api/v1/customer-notes/{customerNoteId} | [[Delete a customer note]] |
