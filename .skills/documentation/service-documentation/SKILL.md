---
name: service-documentation
description: Generate documentation for service layer. Typically describe how a service behaves
---

# Service Documentation Skill

## Purpose

Generate and maintain human-readable documentation for a software service.

The documentation describes **what the service means, what it is responsible for, how it behaves, and how it interacts with the surrounding system**.

This documentation is intended for the project's human-readable architecture/knowledge base, especially an Obsidian-based documentation system.

It is NOT implementation documentation.

The primary goal is to make a service understandable to an engineer who has never worked on it before.

---

# Core Principle

Document the service as a **system capability and behavioral unit**, not as a collection of classes.

The documentation must answer:

1. What is this service?
2. What domain concepts does it operate on?
3. What can the service do?
4. What workflows does it execute?
5. What rules constrain those workflows?
6. How does state change over time?
7. What happens when something fails?
8. What does the service depend on?
9. What depends on the service?
10. Why does the service have its current design?

Prefer behavioral and architectural descriptions over implementation details.

---

# Documentation Boundary

This skill generates **architecture/domain knowledge**.

It must not become a duplicate of repository implementation documentation.

## Belongs in this documentation

- Service purpose
- Responsibilities
- Boundaries
- Domain concepts
- Business concepts
- Workflows
- Business rules
- State machines
- Lifecycle
- Preconditions
- Postconditions
- Invariants
- Failure behavior
- Dependencies
- Events
- Architectural relationships
- Design rationale
- Important tradeoffs

## Does NOT belong here

- Full class-by-class descriptions
- Method-by-method descriptions
- Exact package structures
- Exact source file listings
- Build commands
- Environment variables
- Framework configuration
- Dependency versions
- Low-level implementation details
- Boilerplate API reference
- Copying source code into documentation

When implementation details are relevant, reference the repository rather than reproducing them.

Example:

> The workflow is implemented by the provisioning application service.

Do not turn that into a catalog of every Java class involved.

---

# Service Documentation Structure

A service may use the following structure:

```text
<Service Name>/
├── Overview.md
├── Domain/
│   ├── <Concept>.md
│   ├── <Entity>.md
│   └── <State>.md
├── Workflows/
│   ├── <Workflow>.md
│   └── ...
├── Rules/
│   ├── Business Rules.md
│   ├── Validation Rules.md
│   └── Authorization Rules.md
├── Lifecycle.md
├── Failure Handling.md
├── Dependencies.md
└── Decisions/
    └── <Decision>.md
    
## Responsibilities

- Validate workspace provisioning requests
- Allocate workspace resources
- Create the workspace
- Initialize required defaults
- Emit provisioning completion events

## Does Not Own

- User authentication
- Subscription billing
- Organization identity
- Infrastructure-wide resource management