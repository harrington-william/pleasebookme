# Overview

The Destination Drive API allows application to create, retrieve, update, and delete the link between a service and an external Drive folder (e.g. Google Drive) that booking-related files sync to.

## Base URL

```bash
/api/v1/destination-drives
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
| POST | /api/v1/destination-drives | [[Create a destination drive]] |
| GET | /api/v1/destination-drives/{destinationDriveId} | [[Get a destination drive]] |
| GET | /api/v1/destination-drives | [[Get all destination drives]] |
| PUT | /api/v1/destination-drives/{destinationDriveId} | [[Update a destination drive]] |
| DELETE | /api/v1/destination-drives/{destinationDriveId} | [[Delete a destination drive]] |
