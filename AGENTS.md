# Agents

## Repository

This repository contains the PleaseBookMe modular monolith.

## Stack

- **Java 21**, **Spring Boot 4.1**, **Gradle Kotlin DSL**
- **Jackson 3** from `tools.jackson.databind.*` / `tools.jackson.core.*`
- **Hibernate ORM** from `spring-boot-starter-data-jpa`
- **RestClient** from `spring-boot-starter-webclient`

## Mission

1. Agents operate as software engineering contributors.
2. They must preserve established architecture, security boundaries, data integrity, and documented engineering decisions.

## Source of Truth

When determining project behavior, prefer sources in this order:

1. Current source code
2. Documentation (Obsidian and docs)
3. ADRs
4. Task-specific instructions
5. Agent assumptions

Never invent architecture when authoritative documentation exists.

You should primarily read and write documentation in: `@./obsidian/PleaseBookMe`

## Context Discovery

Before modifying code:

1. Identify the affected domain/module.
2. Read the relevant architecture documentation.
3. Read applicable security documentation.
4. Read relevant database/API documentation.
5. Read applicable ADRs.
6. Inspect the existing implementation.
7. Determine the minimum additional context required.

Do not load the entire .agents directory by default.
Prefer targeted context retrieval.

## Engineering Rules

- Do not modify architecture merely to simplify implementation.
- Do not introduce abstractions without an architectural reason.
- Do not bypass module boundaries.
- Do not weaken security controls to make tests pass.
- Do not modify unrelated code.
- Do not silently change public APIs.
- Do not create duplicate implementations of existing infrastructure.
- Prefer existing project patterns over introducing new patterns.

## Workflows

This is the universal workflows apply accross agents.

### Task Workflow

For non-trivial and large tasks:

1. Understand the task.
2. Discover relevant context.
3. Inspect the current implementation.
4. Produce an implementation plan.
5. Identify architectural/security implications.
6. Implement the smallest coherent change.
7. Run relevant tests.
8. Review the resulting diff.
9. Report unresolved risks.

Do not begin implementation when requirements are materially ambiguous.

### Agent Artifacts

Agents may create artifacts under:

.agents/tasks/          <- Active & Completed tasks
.agents/reviews/        <- AcCode & PR reviews
.agents/decisions/      <- ADRs

Task artifacts must identify:
- objective
- scope
- constraints
- context
- acceptance criteria
- validation requirements

Review artifacts must identify:
- reviewer role
- scope reviewed
- findings
- severity
- evidence
- recommendation

## Prohibited Behavior

Agents must not:

- Rewrite unrelated modules.
- Delete documentation to avoid conflicts.
- Override an ADR without recording a new decision.
- Bypass authorization checks.
- Modify security-sensitive infrastructure without review.
- Expose secrets or credentials.
- Silently change database semantics.
- Mark a task complete without validation.
- Claim tests passed when they were not executed.

## Platform Stage

The current phase is PLATFORM V1.0.0

In PLATFORM V1.0.0 version, some enterprise premium schemas are intentionally excluded

Excluded schemas
- billing
- analytics
- webhook

These are premium feature, not prerequisites of the system. Building these just increase complexity. We just need to focus on other schemas.

Same to tables, some tables such as API Keys are created but have no usage because they just not fit the current architecture. Later, when have enough supporting components, they will live

Same principle applies to permissions, not just tables. `auth.permissions` has a `SESSION` domain (`SESSION.READ`, `SESSION.REVOKE`) seeded in `V79__auth_seed_permission.sql` even though `auth/session/` has no entity, repository, service, or controller yet — it's an empty scaffold package. This is intentional: the permission slugs are reserved ahead of time so RBAC checks/role assignments referencing `SESSION.*` don't need a follow-up migration once the session domain is actually implemented. Don't remove these permission rows as "dead"/unused — they're forward-looking, same as the API Keys table.

## Permission seed coverage (V79-V88) — flags to resolve

`V79__auth_seed_permission.sql` through `V88__audit_seed_permission.sql` seed a plain CRUD permission set (`<RESOURCE>.CREATE/READ/UPDATE/DELETE`, `<RESOURCE>.CREATE/READ` only for the four `audit.*` tables per their append-only business policy) for every domain that has an actual repository/service/controller stack. Deliberately left out, and worth a decision before the next pass:

- **Only plain CRUD verbs were seeded, per explicit instruction** — no domain-specific actions like `BOOKING.REJECT`, `BOOKING.CONFIRM`, `NOTIFICATION.RESEND`, `TENANT.SUSPEND`, etc. were added even though several of these domains (bookings, notifications, tenants) clearly have business actions beyond CRUD once the corresponding service methods exist. These will need their own seed migration(s) later, additive to V79-V88, not a rewrite of them.
- **Resource-code naming**: each seeded `resource` column uses the entity's class name minus "Entity", uppercased, with spaces stripped (e.g. `ResourceAttributesEntity` → `RESOURCEATTRIBUTE`, matching the singular `ResourceAttribute*` DTO/controller naming already used in code rather than the plural entity class name). If a future entity is renamed, its permission `resource`/`slug` values won't auto-follow — update them by hand in a new migration, these seed files are not regenerated automatically.
