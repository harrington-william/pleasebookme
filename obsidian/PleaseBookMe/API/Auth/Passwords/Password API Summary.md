# Overview

The User Password API allows application to create, retrieve, update, and delete a user's password credential. There is no list/bulk-read endpoint — a password row is only ever addressed by its owning `userId`, never enumerated.

## Base URL

```bash
/api/v1/passwords
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
| POST | /api/v1/passwords | [[Create a user password]] |
| GET | /api/v1/passwords/{userId} | [[Get user password]] |
| PUT | /api/v1/passwords/{userId} | [[Update a user password]] |
| DELETE | /api/v1/passwords/{userId} | [[Delete a user password]] |
