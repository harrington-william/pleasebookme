---
title: Task Generator
description: Guidelines for generating large tasks with strong structure
---

# Task Generating Skill

## Where to put it?

- Automatically put it in @/.agents/tasks/ active or completed based on the status of the task

## Task Structure

1. Identity
2. Intent
3. Deliverables
4. Scope
5. Boundaries
6. Constraints
7. Dependencies
8. Input Context
9. Functional Requirements
10. Non-Functional Requirements
11. Acceptance Criteria
12. Validation
13. Escalation
14. Implementation Plan
15. Review Strategy

## What to write in fields

### Identity

Title: [Title]
Domain: [Domain]
Priority: [Priority]
Risk: [Risk]
Status: [Status] <- This should be ACTIVE or COMPLETED

### Intent

Explicitly describe the intent of the task. This should be a concise description of what the task is trying to achieve.

### Scope

Explicitly define the scope and out of scope of the task as 2 separated sections

### Boundaries

- Define the boundaries of the task
- Define the excluded boundaries. For example:

```text
Do not introduce a second authorization mechanism

Do not modify the auth.users table
```

### Constraints

- Define task constraints

Try to define architectural and security constaints

### Dependencies

- List out dependencies of the task, typically including:

1. Required components
2. Related policies
3. Required infrastructure
4. Required services

### Input context

- Guide the specilist how to get the correct context before making changes
- This should include links to relevant documentation such as architecture, domains, decisions, source, drafts

### Functional Requirements

- Define what functional feature must exist after the implementation of the task

List them in an ordered list separated by categories

### Non-Functional Requirements

- Define what functional feature that must not be produced after the implementation

List them in an ordered list separated by categories

### Acceptance Criteria

- Define the acceptance criteria for the task, which mean the conditions that demonstrate the feature has completed successfully

List them in an ordered list separated by categories

### Validation

- Define the validation scenarios for the task with how to test it

### Escalation

- Explitictly define the exit threadshold for the task, tell the specialist when to stop the process and report to the reviewer
- Tell the specilist to not silently resolve architectural or security ambiguity

### Implementation Plan

- Flexibly generate a step-by-step implementation plan for the task from inferences. This should be the longest section and very detailed

*There is no fixed universal template for this section, it should be generated based on the task context*

### Review Strategy

- Explicitly generate the review strategy for reviewers or review agents to follow, this should contain:

1. What to review
2. How to review
3. Core components to review (e.g. AuthorizationPolicyRegistry, AuthService, etc.)

## Templates & Examples

See templates and examples in @/.agents/tasks/templates and @/.agents/tasks/examples for reference