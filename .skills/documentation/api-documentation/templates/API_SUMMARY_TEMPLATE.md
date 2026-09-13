# API Summary Template

# User API Summary

# Overview

The User API allows application to create, retrieve, update, and delete users

## Base URL

```bash
/api/v1/users
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

| Method | Endpoint                    | Description                |
| ------ | --------------------------- | -------------------------- |
| POST   | /api/v1/users               | [[Create a user]]          |
| GET    | /api/v1/users/{userId}      | [[Get user by numeric ID]] |
| GET    | /api/v1/users/email/{email} | [[Get user by email]]      |
| GET    | /api/v1/users/phone/{phone} | [[Get user by phone]]      |
| GET    | /api/v1/users               | [[Get all users]]          |
| PUT    | /api/v1/users/{userId}      | [[Update a user]]          |
| DELETE | /api/v1/users/{userId}      | [[Delete a user]]          |
