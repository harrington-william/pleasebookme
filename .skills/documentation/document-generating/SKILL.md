---
name: document-generating
description: Generate maintainable and human-readable documentation
--- 

# Documentation Generating Skill

## Purpose

Generate and maintain human-readable engineering documentation for a
long-lived software project.

Documentation is a human-facing interface to the system. It must explain
the system clearly to engineers who were not present when it was designed
or implemented.

The goal is not to maximize information density.

The goal is to maximize human comprehension while preserving technically
important information.

---

# 1. Core Principles

## 1.1 Optimize for Human Reading

Write for engineers reading the document to understand, operate, modify,
or review the system.

Prefer:

* short sections.
* explicit headings.
* concise paragraphs.
* structured lists.
* tables where comparison is useful.
* diagrams for relationships and flows.
* examples for non-obvious behavior.

Avoid:

* excessively dense paragraphs.
* narrating the implementation line-by-line.
* repeating information.
* unnecessary prose around trivial code.
* unexplained jargon.
* implementation detail without architectural relevance.

---

## 1.2 Explain Concepts Before Implementation

Documentation must normally follow this direction:

Concept
→ Responsibility
→ Structure
→ Behavior
→ Implementation
→ Integration

Do not begin with source-code mechanics unless the document is explicitly
an implementation reference.

---

## 1.3 One Section, One Question

Every major section should answer one clear question.

Good:

* What is it?
* Why does it exist?
* What does it contain?
* What invariants does it enforce?
* How does it behave?
* Where is it used?
* What are its limitations?

Avoid sections that answer several unrelated questions simultaneously.

---

## 1.4 Prefer Structure Over Prose

When information has a natural structure, represent that structure explicitly.

Use:

* tables for fields and comparisons.
* lists for rules.
* numbered steps for procedures.
* code blocks for code.
* diagrams for flows.
* callouts for important warnings or invariants.

Do not encode structured information inside a long paragraph.

---

## 1.5 Avoid Redundant Explanation

Do not explain the same fact in multiple sections.

A concept should have one authoritative explanation.

Other sections should link to or briefly reference it.

---

# 2. Documentation Levels

Determine the abstraction level before writing.

## Architecture Documentation

Explain:

* purpose.
* responsibilities.
* boundaries.
* dependencies.
* data flow.
* invariants.
* major decisions.

Do not describe every class or method.

## Domain Documentation

Explain:

* domain concepts.
* entities.
* relationships.
* business rules.
* lifecycle.
* important invariants.

Do not reproduce implementation details unless they affect behavior.

## Component Documentation

Explain:

* responsibility.
* public interface.
* important fields.
* important behavior.
* invariants.
* integration points.
* failure behavior.

## Reference Documentation

Explain:

* API.
* methods.
* parameters.
* return values.
* configuration.
* exact operational behavior.

Reference documentation may be more implementation-oriented.

---

# 3. Standard Document Structure

Choose the smallest structure that completely explains the subject.

For a component/class document, prefer:

# Name

## Purpose

What this component represents and why it exists.

## Responsibility

What this component owns.

## Structure

Important fields, collaborators, or relationships.

## Invariants

Rules that must always hold.

## Behavior

Important operations and their semantics.

## Lifecycle / Flow

How the component is created, used, and completed.

## Integration

Where it participates in the larger system.

## Current Limitations

Known gaps, constraints, or incomplete integration.

## Examples

Use only when they improve understanding.

## Related Documentation

Links to relevant concepts.

Do not force every section into every document.

---

# 4. Writing Rules

## Paragraph Length

Prefer paragraphs of 1–4 sentences.

If a paragraph contains several independent ideas, split it.

If information can be expressed as a list, table, or diagram, prefer that
structure.

## Sentence Length

Prefer direct sentences.

Avoid chaining many clauses with em dashes, semicolons, or parentheses.

Bad:

"AuthorizationContext is the complete authorization question, assembled once
by whoever wants an answer and then passed unchanged through the entire engine..."

Prefer:

"AuthorizationContext represents one complete authorization request.

The context is created once and passed unchanged through the authorization
engine.

All policies evaluate the same context."

---

# 5. Explain Importance, Not Obvious Mechanics

Explain implementation details only when they communicate an architectural,
security, behavioral, or maintenance property.

For example:

Useful:

"attributes is defensively copied so policies cannot observe mutation during
authorization evaluation."

Not useful:

"attributes uses Map.copyOf(attributes)."

The code already shows the second fact.

---

# 6. Use Tables for Structured Data

For fields, prefer:

| Field        | Meaning                               | Required | Notes                      |
| ------------ | ------------------------------------- | -------: | -------------------------- |
| principal    | Authenticated actor requesting access |      Yes | Must not be null           |
| resourceType | Type of protected resource            |      Yes | Used for policy selection  |
| action       | Requested operation                   |      Yes | Example: `READ`            |
| resource     | Target resource                       |       No | Null for create operations |
| scope        | Organization/tenant scope             |      Yes | Defaults to unscoped       |
| membership   | Organization-specific membership      |       No | May be absent              |
| attributes   | Additional policy inputs              |      Yes | Empty when unused          |

Do not place field semantics into large prose paragraphs when a table
communicates them more efficiently.

---

# 7. Use Explicit Invariants

Architecturally important rules must be visually prominent.

Example:

## Invariants

* `principal` is always present.
* `resourceType` is non-blank.
* `action` is non-blank.
* `scope` is never null after construction.
* `attributes` is immutable.
* `membership` may be absent.

Invariants should never be buried inside explanatory prose.

---

# 8. Distinguish Current, Intended, and Possible Behavior

Never mix them implicitly.

Use explicit labels:

## Current Behavior

What the system does today.

## Known Limitation

What is incomplete or problematic today.

## Planned Direction

An explicitly intended future change.

## Alternatives

Possible solutions that have not been selected.

Never present a possible future design as if it were already implemented.

---

# 9. Code Examples

Code should demonstrate behavior, not reproduce the entire source file.

Prefer focused examples:

```java
AuthorizationContext.forResource(
    principal,
    "BOOKING",
    "READ",
    booking,
    scope,
    membership
);
```

Then explain what the example demonstrates.

Do not paste large implementations unless the implementation itself is
the subject of the document.

---

# 10. Explain Flows as Flows

When describing behavior across components, prefer:

```text
Caller
  ↓
AuthorizationPermissionEvaluator
  ↓
AuthorizationContext
  ↓
AuthorizationService
  ↓
AuthorizationPolicy
  ↓
AuthorizationDecision
```

Then explain each important transition.

Do not describe a multi-component flow as one large paragraph.

---

# 11. Links and Cross-References

Use links to avoid duplication.

Prefer:

> `AuthorizationContext` provides the input consumed by [[Authorization Engine]].

Do not copy the entire explanation of the authorization engine into the
context document.

Cross-references should help the reader navigate the conceptual model.

---

# 12. Readability Gate

Before finalizing documentation, inspect it as a human reader.

Ask:

1. Can I understand what this thing is within 30 seconds?
2. Can I identify its responsibility immediately?
3. Can I scan the important rules without reading every paragraph?
4. Can I understand the lifecycle or flow?
5. Can I distinguish current behavior from future plans?
6. Can I find important limitations quickly?
7. Does each section answer one clear question?
8. Is any paragraph doing the job of a table, list, or diagram?
9. Is any information repeated unnecessarily?
10. Would an engineer unfamiliar with the implementation understand it?

If several answers are "no", restructure the document before finalizing it.

---

# 13. Technical Accuracy Gate

Before finalizing:

* Verify names against source code.
* Verify relationships against the actual architecture.
* Verify examples.
* Verify current call sites.
* Verify current behavior.
* Do not invent undocumented behavior.
* Do not present assumptions as facts.
* Clearly label inferred or future behavior.

When source code and documentation disagree, do not silently choose one.
Record the discrepancy or escalate it.

---

# 14. Documentation Maintenance

Documentation must describe the system at the appropriate level of
abstraction, not merely describe the current source code line-by-line.

When implementation changes:

1. Determine whether the change affects documented behavior.
2. Update the smallest relevant document.
3. Preserve the existing conceptual structure.
4. Avoid rewriting unrelated sections.
5. Remove obsolete information.
6. Add new information only when it helps a future maintainer.

Do not regenerate an entire document merely because one implementation
detail changed.

---

# 15. Agent Workflow

When generating or updating documentation:

### Step 1 — Determine the Subject

Identify whether the document describes:

* architecture.
* domain.
* component.
* API.
* workflow.
* configuration.
* operational procedure.

### Step 2 — Determine the Reader

Assume the reader needs to understand the system without reading the
entire implementation.

### Step 3 — Gather Evidence

Inspect:

* relevant source code.
* architecture documentation.
* domain documentation.
* ADRs.
* related components.
* existing cross-references.

Do not rely on source code alone when architectural documentation exists.

### Step 4 — Build the Mental Model

Before writing, determine:

* what it is.
* why it exists.
* what it owns.
* what it depends on.
* how it behaves.
* what invariants matter.
* where it fits in the system.
* what limitations exist.

### Step 5 — Design the Information Structure

Choose headings before writing detailed prose.

Prefer conceptual organization over source-code order.

### Step 6 — Write for Scanning

Convert dense explanations into:

* headings.
* tables.
* lists.
* diagrams.
* examples.
* short paragraphs.

### Step 7 — Verify

Check technical accuracy against the implementation and authoritative
project documentation.

### Step 8 — Perform the Readability Gate

Read the document as a new engineer.

Rewrite any section that requires unnecessary effort to understand.

---

# 16. Anti-Patterns

Never produce documentation that:

* mirrors source-code order mechanically.
* explains every getter/setter.
* turns trivial methods into long prose sections.
* duplicates architecture documentation.
* mixes current and future behavior.
* uses paragraphs where tables or lists are clearer.
* contains implementation commentary with no architectural value.
* repeats the same fact in multiple places.
* describes hypothetical behavior as implemented behavior.
* maximizes completeness at the expense of readability.

---

# 17. Definition of Done

Documentation is complete when:

* the purpose is immediately understandable.
* the major concepts are clearly separated.
* important invariants are explicit.
* behavior is easy to scan.
* flows are represented as flows.
* implementation details appear only when relevant.
* current limitations are visible.
* links connect related concepts.
* no important statement is unsupported.
* no unnecessary repetition remains.
* a new engineer can use the document without reading the source first.
