## Purpose

A schema navigation file is a lightweight entry point for navigating database documentation.

It MUST NOT explain database design, business rules, table semantics, or implementation details.

Its only responsibilities are:

- Identify the schema.
    
- Index the tables within the schema.
    
- Point to closely related schemas.
    

---

## Structure

```markdown
# `<schema>`

> <One-line description.>

## Tables

| Table | Documentation |
|---|---|
| `<table>` | [View](./tables/<table>.md) |
| `<table>` | [View](./tables/<table>.md) |

## Related Schemas

- [`<schema>`](../<schema>/README.md)
- [`<schema>`](../<schema>/README.md)
```

---

## Conventions

### Schema Description

Use **one short sentence** only.

It should identify the schema's general responsibility without explaining its architecture.

### Tables

List every documented table belonging to the schema.

Only include:

- Table name.
    
- Link to its documentation.
    

Do not describe columns, relationships, lifecycle, or business rules.

### Related Schemas

Include only schemas that an engineer is likely to navigate to when working with the current schema.

Do not list every schema in the database.

### Links

All links MUST point to existing documentation.

Agents MUST NOT invent documentation paths.

If documentation does not exist, omit the link rather than creating a fictional reference.

---

## Generation Rules

When generating the navigation file, the agent MUST:

1. Identify the schema.
    
2. Discover its tables.
    
3. Locate their documentation.
    
4. Generate the navigation index.
    
5. Verify all links.
    
6. Keep the file concise.
    

The agent MUST NOT add architectural explanations or table-level descriptions.

---

## Example

```markdown
# `audit`

> Platform audit and historical activity records.

## Tables

| Table | Documentation |
|---|---|
| `audit_events` | [View](./tables/audit_events.md) |
| `audit_changes` | [View](./tables/audit_changes.md) |

## Related Schemas

- [`auth`](../auth/README.md)
- [`core`](../core/README.md)
```

## Definition of Done

The file is complete when an engineer can use it to quickly answer:

> **"Where do I go next?"**

Nothing more is required.