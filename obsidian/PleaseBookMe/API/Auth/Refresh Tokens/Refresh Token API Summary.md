# Overview

The Refresh Token API allows application to create, retrieve, update, and delete refresh tokens

## Base URL

```bash
/api/v1/refresh
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
| POST | /api/v1/refresh | [[Create a refresh token]] |
| GET | /api/v1/refresh/{refreshTokenId} | [[Get a refresh token]] |
| GET | /api/v1/refresh | [[Get all refresh tokens]] |
| PUT | /api/v1/refresh/{refreshTokenId} | [[Update a refresh token]] |
| DELETE | /api/v1/refresh/{refreshTokenId} | [[Delete a refresh token]] |
