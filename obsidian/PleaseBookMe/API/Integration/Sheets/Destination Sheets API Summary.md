# Overview

The Destination Sheets API allows application to create, retrieve, update, and delete the link between a service and an external spreadsheet (e.g. Google Sheets) that its bookings sync to.

## Base URL

```bash
/api/v1/destination-sheets
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
| POST | /api/v1/destination-sheets | [[Create a destination sheets]] |
| GET | /api/v1/destination-sheets/{destinationSheetsId} | [[Get a destination sheets]] |
| GET | /api/v1/destination-sheets | [[Get all destination sheets]] |
| PUT | /api/v1/destination-sheets/{destinationSheetsId} | [[Update a destination sheets]] |
| DELETE | /api/v1/destination-sheets/{destinationSheetsId} | [[Delete a destination sheets]] |
