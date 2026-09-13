# Overview

The Availability Ruleset API allows an application to create a schedule and its recurring availability windows in a single atomic request. It exists to replace the previous client-side pattern of a `POST /schedules` call followed by one `POST /availabilities` call per window, which had no rollback if a window failed partway through.

Unlike the Schedule and Availability APIs, this resource never accepts an ownership field — the schedule and every window it creates are always attributed to the authenticated caller, derived server-side.

## Base URL

```bash
/api/v1/availability-rulesets
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
| POST | /api/v1/availability-rulesets | [[Create an availability ruleset]] |
