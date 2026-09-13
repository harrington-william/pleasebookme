# 1. Purpose

This document defines the standard for documenting and validating the architecture-level design of a database.

AI agents MUST read and follow this convention before generating, reviewing, or validating database architecture documentation.

The purpose of architecture-level database documentation is to explain:

- How the database is organized.
    
- Why the database is structured this way.
    
- What boundaries exist between schemas and domains.
    
- Where data ownership resides.
    
- How data flows through the system.
    
- What consistency and integrity guarantees exist.
    
- What architectural rules govern database usage.
    
- How the database interacts with application modules and external systems.
    

Architecture documentation MUST describe the **system-level model**, not individual table semantics.

---

# 2. Scope

Architecture-level database documentation covers:

- Database topology
    
- Database technology
    
- Logical schema organization
    
- Domain/data ownership
    
- Bounded contexts
    
- Cross-schema relationships
    
- Data access boundaries
    
- Transaction boundaries
    
- Consistency model
    
- Integrity strategy
    
- Persistence patterns
    
- Data lifecycle
    
- Security model
    
- Auditing
    
- Migration strategy
    
- Performance strategy
    
- Availability and reliability
    
- Backup and recovery
    
- Integration boundaries
    
- Architectural constraints
    

It MUST NOT become a complete table catalog.

Individual table semantics belong to table-level documentation.

---

# 3. Documentation Principles

## 3.1 Architecture Before Implementation

Describe architectural intent before implementation details.

Good:

> The `auth` schema owns identity and authorization data.

Less useful:

> The application uses `AuthRepository` to query the `auth.users` table.

The first describes architecture.

The second describes implementation.

Implementation details belong in application or infrastructure documentation unless they are architecturally significant.

---

## 3.2 Explain Why, Not Only What

Architecture documentation should answer:

> Why was the database organized this way?

For example:

> Database schemas are aligned with logical ownership boundaries so that authorization, booking operations, resource management, and auditing remain independently understandable and enforceable.

Avoid merely listing:

```text
auth
core
resource
audit
```

The architecture document must explain what those boundaries mean.

---

## 3.3 Establish Explicit Ownership

Every major data area MUST have an identified owner.

Ownership should be expressed in terms of:

- Domain
    
- Module
    
- Subsystem
    
- Bounded context
    

The documentation MUST distinguish between:

- Data ownership
    
- Data access
    
- Data reference
    

A module referencing another module's table does not necessarily mean it owns that data.

---

## 3.4 Treat Boundaries as Contracts

A schema/domain boundary should define what other components may and may not do.

For example:

> The `audit` subsystem owns audit records. Other domains may emit audit events but must not directly modify existing audit records.

This is stronger and more useful than:

> The audit schema contains audit tables.

---

## 3.5 Prefer Explicit Data Flow

When data crosses boundaries, document:

```text
Producer
    ↓
Boundary
    ↓
Consumer
```

Explain whether the interaction is:

- Direct relational reference
    
- Application service
    
- Domain event
    
- Integration event
    
- Asynchronous message
    
- API
    
- Batch process
    
- External integration
    

Do not assume that a foreign key is the only form of architectural coupling.

---

## 3.6 Document Consistency Deliberately

The architecture document MUST describe where the system expects:

- Strong consistency
    
- Eventual consistency
    
- Transactional consistency
    
- Idempotent processing
    
- Asynchronous processing
    

Do not claim eventual consistency merely because events exist.

Do not claim strong consistency merely because PostgreSQL transactions are used.

Describe consistency according to actual system behavior.

---

# 4. Architecture Variants

The architecture document MUST identify which architectural model applies.

Supported variants:

1. `MODULAR_MONOLITH`
    
2. `MICROSERVICES`
    
3. `SERVICE_ORIENTED`
    
4. `SHARED_DATABASE`
    
5. `DATABASE_PER_SERVICE`
    
6. `HYBRID`
    
7. `SINGLE_APPLICATION_DATABASE`
    

A system MAY combine multiple characteristics.

For example:

```text
Architecture:
HYBRID

Characteristics:
- Modular monolith application
- Shared PostgreSQL database
- Schema-based ownership boundaries
- Redis used for ephemeral state
- External systems accessed through integration boundaries
```

Do not classify the architecture based solely on folder structure.

---

# 5. Required Architecture Sections

Every architecture document MUST contain the following sections:

1. Overview
    
2. Architectural Model
    
3. Database Technology
    
4. Logical Organization
    
5. Data Ownership
    
6. Domain / Schema Boundaries
    
7. Data Relationships and Coupling
    
8. Transaction and Consistency Model
    
9. Data Integrity Strategy
    
10. Data Access Rules
    
11. Security Architecture
    
12. Audit Architecture
    
13. Data Lifecycle
    
14. Migration Strategy
    
15. Reliability and Recovery
    
16. Performance Considerations
    
17. Architectural Constraints
    
18. Validation Criteria
    

Sections may contain "Not applicable" when the concept genuinely does not apply.

Agents MUST NOT fabricate missing architecture decisions.

---

# 6. Standard Architecture Document

The document MUST follow this structure.

```markdown
# Database Architecture

## 1. Overview

<High-level description of the database and its role in the system.>

## 2. Architectural Model

- **Architecture:** <architecture variant>
- **Database Model:** <shared / isolated / hybrid>
- **Primary Database:** <technology>
- **System of Record:** <database/system>
- **Consistency Model:** <description>

<Explain the architectural model and its implications.>

## 3. Database Technology

### Primary Database

<Database technology and version if architecturally relevant.>

### Supporting Data Stores

<List Redis, object storage, search engines, queues, etc. if applicable.>

For each supporting store explain:

- Purpose
- Data ownership
- Durability expectations
- Whether it is authoritative or derived

## 4. Logical Organization

<Explain how the database is divided into schemas, domains, or bounded contexts.>

### Schema Overview

| Schema | Owner | Responsibility | Scope |
|---|---|---|---|
| `<schema>` | `<owner>` | `<responsibility>` | `<scope>` |

## 5. Data Ownership

<Explain ownership rules across domains and schemas.>

## 6. Domain / Schema Boundaries

<Describe each major boundary.>

### `<schema/domain>`

- **Owner:** <owner>
- **Responsibility:** <responsibility>
- **Owns:** <data categories>
- **May reference:** <allowed dependencies>
- **Must not modify:** <restricted data>

## 7. Data Relationships and Coupling

<Describe important cross-domain relationships.>

### Direct Relationships

<Describe relational dependencies.>

### Indirect Relationships

<Describe events, APIs, queues, integrations, etc.>

### Coupling Rules

<Explain permitted and prohibited coupling.>

## 8. Transaction and Consistency Model

<Explain transaction boundaries.>

### Transaction Boundaries

<Which operations must be atomic?>

### Consistency

<Strong/eventual/etc. and where each applies.>

### Idempotency

<Where duplicate operations are prevented or tolerated.>

## 9. Data Integrity Strategy

<Explain how integrity is enforced.>

### Database-Level Integrity

- Primary keys
- Foreign keys
- Unique constraints
- Check constraints
- Not-null constraints

### Application-Level Integrity

<Rules that cannot or should not be enforced by the database.>

### Domain Integrity

<Important business invariants.>

## 10. Data Access Rules

<Describe how application components access persistent data.>

### Ownership Rules

<Which module owns writes?>

### Read Rules

<How data may be queried across boundaries?>

### Write Rules

<How mutations are performed?>

### Cross-Boundary Access

<Rules governing access to another domain's data.>

## 11. Security Architecture

<Describe database security architecture.>

### Isolation

<Tenant/organization/etc. isolation.>

### Authentication

<How database/application access is authenticated.>

### Authorization

<How access is controlled.>

### Sensitive Data

<Encryption, hashing, masking, secrets, etc.>

### Privileged Access

<Administrative/database-level access rules.>

## 12. Audit Architecture

<Explain what is audited and how audit data is produced.>

### Audit Sources

<Actors, applications, integrations, system processes.>

### Audit Scope

<Operations/resources covered.>

### Immutability

<Whether audit data is append-only.>

### Audit Boundary

<Relationship between audit data and operational data.>

## 13. Data Lifecycle

<Explain lifecycle strategy.>

### Creation

<How persistent records enter the system.>

### Modification

<How operational records change.>

### Deletion

<Physical deletion rules.>

### Archival

<Archival strategy if applicable.>

### Retention

<Retention policy if defined.>

## 14. Migration Strategy

<Explain how schema changes are introduced.>

### Migration Tool

<Tool/process.>

### Versioning

<Migration versioning strategy.>

### Deployment

<How migrations interact with deployments.>

### Backward Compatibility

<Compatibility expectations during rolling deployments.>

## 15. Reliability and Recovery

<Explain how the database remains available and recoverable.>

### Backup

<Backup strategy.>

### Recovery

<Recovery strategy and objectives if defined.>

### Failure Handling

<Database failure behavior.>

### Availability

<High availability / replication / failover if applicable.>

## 16. Performance Considerations

<Describe architectural performance strategy.>

### Indexing

<General indexing principles.>

### Query Patterns

<Important query characteristics.>

### Caching

<Caching architecture and ownership.>

### Partitioning

<Partitioning strategy if applicable.>

### Scaling

<Vertical/horizontal/read scaling strategy.>

## 17. Architectural Constraints

<List rules that engineers MUST respect.>

Examples:

- Domain-owned tables must not be modified directly by other domains.
- Audit records are append-only.
- Cross-tenant queries are prohibited.
- Database migrations must be version controlled.
- Redis must not be treated as the system of record.
- Operational transactions must remain within defined transaction boundaries.

## 18. Validation Criteria

<Criteria used to determine whether the implementation still conforms to this architecture.>
```

---

# 7. How Agents Should Validate the Architecture

This section is particularly important for your use case.

You don't only want an agent to **write** architecture documentation.

You want another agent to be able to answer:

> "Is this documentation actually true?"

The architecture document therefore needs to contain **testable claims**.

For example:

> `core` owns booking-related operational data.

This can be validated by inspecting:

- Schema definitions
    
- Application modules
    
- Repository packages
    
- Foreign keys
    
- Migration files
    
- Services
    

A stronger architecture statement is:

> The Booking module is the sole application owner responsible for mutations to `core.bookings`.

Now an agent can inspect the codebase and determine whether another module directly writes to the table.

---

# 8. Architecture Validation Matrix

I recommend adding this concept to the convention.

Every important architectural rule should be classified as one of:

|Type|Meaning|
|---|---|
|`STRUCTURAL`|Can be verified from database structure|
|`BEHAVIORAL`|Requires application/code inspection|
|`SECURITY`|Requires authorization/security inspection|
|`OPERATIONAL`|Requires deployment/infrastructure inspection|
|`DOCUMENTARY`|Exists as an architectural decision but may not be mechanically verifiable|

For example:

```text
Rule:
"auth owns authentication data."

Type:
STRUCTURAL + BEHAVIORAL

Verification:
- Inspect auth schema.
- Inspect authentication module.
- Identify write paths.
```

Another:

```text
Rule:
"Audit records are append-only."

Type:
STRUCTURAL + BEHAVIORAL

Verification:
- Check database privileges.
- Search application code for UPDATE/DELETE operations.
- Inspect repository methods.
```

Another:

```text
Rule:
"Production database is backed up daily."

Type:
OPERATIONAL

Verification:
- Inspect infrastructure configuration.
- Inspect cloud/database backup configuration.
```

This makes your documentation **auditable**.

---

# 9. Architecture Claims Should Be Verifiable

I recommend that AI agents follow this rule:

> **Every significant architectural statement should either be mechanically verifiable or explicitly identified as an architectural decision.**

For example:

### Weak

> The database is designed for high scalability.

There is no meaningful way to validate that.

### Better

> Read-heavy workloads may be served through read replicas while transactional writes remain directed to the primary database.

Now an agent can inspect infrastructure and application configuration.

---

# 10. Validation Status

When an AI agent reviews the architecture documentation, it should classify each important claim as:

```text
VERIFIED
PARTIALLY_VERIFIED
CONTRADICTED
UNVERIFIABLE
OUTDATED
```

Example:

|Architecture Claim|Status|Evidence|
|---|---|---|
|`auth` owns identity data|VERIFIED|Schema + auth module|
|Audit records are append-only|PARTIALLY_VERIFIED|Schema supports it, application writes need inspection|
|All tenant queries are isolated|UNVERIFIABLE|Requires complete query-path analysis|
|Redis is not authoritative|VERIFIED|Application architecture + persistence configuration|
|Bookings are immutable after acceptance|CONTRADICTED|Booking service performs updates|

This turns your architecture documentation into something that can be continuously checked by AI agents.

---

# 11. Architecture Documentation vs Table Documentation

You now have two different levels.

```text
DATABASE ARCHITECTURE
│
├── Why is the database organized this way?
├── Who owns each data boundary?
├── How do domains interact?
├── What are the consistency guarantees?
├── What are the security rules?
├── What are the migration/recovery strategies?
│
└── TABLE DOCUMENTATION
    │
    ├── What is this table?
    ├── What does one row mean?
    ├── What is its lifecycle?
    ├── What are its invariants?
    ├── What relationships does it have?
    └── How should engineers interact with it?
```

That separation is important.

The architecture document should **not** become a giant catalog of tables.

The table documents should **not** attempt to explain the entire database architecture.

---

# 12. Your Agent Skill Strategy

And yes — **absolutely use the previous table convention as an Agent Skill.**

I would structure your skills like this:

```text
skills/
└── database/
    │
    ├── architecture/
    │   └── SKILL.md
    │
    ├── schema/
    │   └── SKILL.md
    │
    └── table/
        └── SKILL.md
```

Then give each skill a very explicit responsibility.

### `database-architecture`

```text
Generate and validate database architecture documentation.

Input:
- Existing database
- Migrations
- Infrastructure
- Application modules
- Architecture documentation

Output:
- Architecture documentation
- Architectural claims
- Validation criteria
- Identified inconsistencies
```

### `database-schema`

```text
Document logical database schemas and ownership boundaries.

Input:
- Database schema
- Architecture documentation

Output:
- Schema documentation
- Schema responsibilities
- Ownership
- Allowed dependencies
- Boundary rules
```

### `database-table`

```text
Generate and validate table-level documentation.

Input:
- Table definition
- Migration
- Application code
- Architecture documentation

Output:
- Standard table documentation
- Table variant
- Row semantics
- Lifecycle
- Invariants
- Usage rules
```

---

# 13. Even Better: Make the Skills Hierarchical

For your use case, I would make the dependency explicit:

```text
database-architecture
        │
        ↓
database-schema
        │
        ↓
database-table
```

The table-level agent should understand:

> "I cannot determine whether this table violates an architectural boundary without consulting the architecture documentation."

Likewise, the architecture agent should understand:

> "I should not claim that a table is append-only merely because its name sounds like an audit table."

This gives you a **documentation hierarchy** rather than three disconnected prompt files.

---

# 14. The Most Important Design Decision

I would actually separate **generation** from **validation**.

Instead of one generic skill:

```text
database-table
```

you can eventually have:

```text
database/
├── architecture/
│   ├── generate.md
│   └── validate.md
│
├── schema/
│   ├── generate.md
│   └── validate.md
│
└── table/
    ├── generate.md
    └── validate.md
```

Or, if you want to keep the number of skills low:

```text
database-architecture
database-schema
database-table
```

with each skill supporting both:

```text
MODE: GENERATE
MODE: REVIEW
MODE: VALIDATE
```

I prefer the latter initially.

The key is that **the same convention defines both what good documentation looks like and how an agent determines whether existing documentation is correct**.

That is what will make this useful for your larger AI-assisted engineering workflow.