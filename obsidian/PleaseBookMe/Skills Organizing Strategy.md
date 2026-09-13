Yes. Once you have established:

- `AGENTS.md` → universal agent policy
    
- `CLAUDE.md` → Claude orchestration
    
- `.agents/` → shared project knowledge
    

the next layer is **Skills**.

I would treat Skills as a fundamentally different object from documentation.

> **Documentation tells an agent what the system is.  
> Skills tell an agent how to perform a particular class of work.**

That distinction becomes extremely important once your codebase becomes large.

---

# 1. First: separate Knowledge, Rules, Skills, and Tasks

I recommend thinking about your AI infrastructure as four distinct layers:

```text
┌────────────────────────────────────────────┐
│                 AGENTS.md                  │
│              GLOBAL POLICIES               │
└──────────────────────┬─────────────────────┘
                       │
┌──────────────────────▼─────────────────────┐
│                .agents/                    │
│             PROJECT KNOWLEDGE              │
│                                            │
│ architecture / security / database / etc. │
└──────────────────────┬─────────────────────┘
                       │
┌──────────────────────▼─────────────────────┐
│                .skills/                    │
│             CAPABILITIES / SOPs            │
│                                            │
│ how to implement / review / migrate / etc.│
└──────────────────────┬─────────────────────┘
                       │
┌──────────────────────▼─────────────────────┐
│              .agents/tasks/                │
│             TASK INSTANCES                 │
└────────────────────────────────────────────┘
```

You don't necessarily have to call the directory `.skills`; use whatever convention your agent ecosystem requires. The important thing is the **semantic separation**.

---

# 2. What exactly is a Skill?

A Skill should answer:

> **"Given a certain type of task, what procedure should an agent follow?"**

For example:

```text
security-review
database-migration
spring-feature
react-component
api-endpoint
oauth-integration
jwt-authentication
integration-test
performance-analysis
bug-investigation
architecture-review
```

A skill isn't merely:

> "Here's information about JWT."

That's documentation.

Instead:

> "Here's how an agent should implement and validate JWT authentication in this repository."

That's a skill.

---

# 3. Example: JWT documentation vs JWT skill

Your `.agents/security/authentication.md` might say:

```text
The system uses:
- short-lived JWT access tokens
- refresh tokens
- asymmetric signing
- JwtTokenVerifier
- JwtClaimsMapper
- AuthenticatedPrincipal
...
```

That's **knowledge**.

A skill would say:

```text
# JWT Authentication Skill

## Trigger

Use when modifying:
- JWT authentication
- token verification
- claims
- principal construction
- authentication filters

## Procedure

1. Read authentication architecture.
2. Inspect JwtTokenVerifier.
3. Inspect JwtClaimsMapper.
4. Inspect principal construction.
5. Identify trust boundaries.
6. Implement change.
7. Add authentication tests.
8. Run security review.
9. Verify token failure behavior.

## Security Requirements

- Never trust client-provided identity fields.
- Reject malformed tokens.
- Reject expired tokens.
- ...
```

That's an **operational capability**.

---

# 4. Don't create one skill per technology

This is a common mistake.

You don't want:

```text
skills/
├── java/
├── spring/
├── spring-boot/
├── postgres/
├── react/
├── typescript/
├── docker/
├── jwt/
├── redis/
└── ...
```

That becomes a technology encyclopedia.

Instead, organize Skills around **engineering activities**.

For example:

```text
.skills/
├── development/
├── architecture/
├── database/
├── security/
├── testing/
├── operations/
└── maintenance/
```

Then:

```text
.skills/
├── development/
│   ├── feature-implementation/
│   ├── bug-fix/
│   ├── refactoring/
│   └── api-development/
│
├── architecture/
│   ├── architecture-review/
│   ├── adr-creation/
│   └── boundary-analysis/
│
├── database/
│   ├── schema-change/
│   ├── migration/
│   ├── query-optimization/
│   └── data-integrity-review/
│
├── security/
│   ├── authentication-review/
│   ├── authorization-review/
│   ├── threat-modeling/
│   ├── security-audit/
│   └── oauth-integration/
│
├── testing/
│   ├── unit-testing/
│   ├── integration-testing/
│   ├── e2e-testing/
│   └── regression-analysis/
│
└── maintenance/
    ├── dependency-upgrade/
    ├── documentation-update/
    └── technical-debt/
```

This is much more scalable.

---

# 5. Skills should be composable

This is probably the most important design principle.

Suppose the user says:

> "Add Google OAuth authentication."

Don't create one giant skill:

```text
google-oauth-feature
```

Instead compose:

```text
Feature Implementation
        +
OAuth Integration
        +
Authentication
        +
Database Migration
        +
Integration Testing
        +
Security Review
```

Conceptually:

```text
                 OAuth Feature
                      │
          ┌───────────┼───────────┐
          ▼           ▼           ▼
       OAuth        Feature      DB
       Skill         Skill      Skill
          │           │           │
          └───────────┼───────────┘
                      ▼
                  Test Skill
                      │
                      ▼
                Security Review
```

This prevents a combinatorial explosion.

Without composition:

```text
google-oauth-spring
google-oauth-fastapi
google-oauth-react
google-oauth-postgres
google-oauth-security
google-oauth-testing
...
```

You'll eventually have hundreds of redundant skills.

---

# 6. I recommend three categories of skills

In a serious system, I'd distinguish:

## A. Workflow Skills

These describe **how to perform an engineering activity**.

Examples:

```text
feature-development
bug-investigation
refactoring
code-review
architecture-review
incident-analysis
```

---

## B. Domain Skills

These describe **how to work within a specific domain**.

Examples:

```text
authentication
authorization
booking
payment
notification
audit
identity
tenant-management
```

These are especially useful for your project.

---

## C. Technology Skills

These describe **repository-specific usage of a technology**.

Examples:

```text
spring-boot
postgresql
redis
docker
react
kafka
```

But keep these thin.

They should not attempt to reproduce official documentation.

Instead:

```text
Spring Boot in this repository
```

rather than:

```text
Everything about Spring Boot.
```

---

# 7. Therefore your hierarchy can become

I'd use:

```text
.skills/
│
├── workflows/
│   ├── feature-development/
│   ├── bug-investigation/
│   ├── refactoring/
│   ├── code-review/
│   └── architecture-review/
│
├── domains/
│   ├── authentication/
│   ├── authorization/
│   ├── identity/
│   ├── booking/
│   ├── audit/
│   └── billing/
│
├── technologies/
│   ├── spring-boot/
│   ├── postgresql/
│   ├── react/
│   ├── docker/
│   └── redis/
│
└── operations/
    ├── database-migration/
    ├── deployment/
    ├── dependency-upgrade/
    └── incident-response/
```

This gives you three dimensions:

```text
What are we doing?
        ↓
workflow

What part of the business/system?
        ↓
domain

What technology is involved?
        ↓
technology
```

---

# 8. A Skill should have a predictable structure

Don't make Skills arbitrary Markdown documents.

Give them a contract.

For example:

```text
.skills/workflows/feature-development/
├── SKILL.md
├── checklist.md
├── templates/
│   └── task.md
└── examples/
```

The main `SKILL.md` could contain:

```text
# Feature Development

## Purpose

Define the standard procedure for implementing a new feature.

## When to Use

Use when:
- adding new functionality
- extending an existing domain capability
- introducing a new API capability

## Inputs

Required:
- feature requirement
- affected domain
- acceptance criteria

Optional:
- architecture proposal
- ADR
- existing issue

## Required Context

Read:
- relevant architecture
- domain documentation
- applicable ADRs
- coding conventions

## Procedure

1. Understand requirement.
2. Identify affected boundaries.
3. Inspect existing implementation.
4. Determine required changes.
5. Create implementation plan.
6. Implement.
7. Test.
8. Review.
9. Update documentation.

## Validation

...

## Failure Conditions

Stop if:
- requirements conflict with architecture;
- security implications are unclear;
- database semantics are ambiguous;
- required dependencies are unavailable.

## Outputs

- implementation
- tests
- documentation changes
- review artifacts
```

This makes Skills predictable to both humans and agents.

---

# 9. Skills should declare dependencies

This becomes extremely powerful.

For example:

```text
# OAuth Integration Skill

## Depends On

- authentication
- security-review
- integration-testing

## Required Knowledge

- .agents/security/authentication.md
- .agents/security/identity.md
- .agents/security/oauth/google.md
```

Now the orchestrator can construct a context graph.

```text
OAuth Skill
    │
    ├── Authentication Skill
    │
    ├── Security Review Skill
    │
    └── Integration Testing Skill
```

And:

```text
Required Knowledge
    │
    ├── authentication.md
    ├── identity.md
    └── google-oauth.md
```

This is where your system starts becoming genuinely sophisticated.

---

# 10. Skills should also define their "activation conditions"

You don't want Claude loading 50 skills every time.

Each skill should have something like:

```text
## Activation

Activate when the task involves:

- creating an OAuth provider;
- modifying OAuth callback behavior;
- modifying external identity mapping;
- changing OAuth scopes;
- changing OAuth state/PKCE handling.
```

And:

```text
## Do Not Activate

Do not activate for:

- ordinary login UI changes;
- unrelated JWT parsing;
- generic REST endpoints.
```

This gives the orchestrator a way to select Skills dynamically.

---

# 11. Think of Skills as executable SOPs

A good analogy is:

```text
Documentation
=
Reference Manual

Skill
=
Standard Operating Procedure

Task
=
Work Order

Agent
=
Engineer

Orchestrator
=
Engineering Manager
```

For example:

```text
Reference:
    "How authorization works"

Skill:
    "How to modify authorization safely"

Task:
    "Add resource-level authorization"

Agent:
    Codex

Manager:
    Claude
```

This separation is exceptionally useful.

---

# 12. Don't duplicate `.agents` documentation inside Skills

Suppose:

```text
.agents/security/authorization.md
```

contains:

```text
Authorization architecture
```

Then your skill should reference it:

```text
## Required Context

Read:
.agents/security/authorization.md
```

rather than copying:

```text
RBAC works like this...
ABAC works like this...
PBAC works like this...
```

Otherwise you'll eventually get:

```text
authorization.md says X

authorization-review/SKILL.md says Y

authorization-implementation/SKILL.md says Z
```

Now your AI has three competing realities.

Bad.

---

# 13. Skills can produce standardized artifacts

This is another major advantage.

For example:

### Architecture review skill

Input:

```text
code + architecture + task
```

Output:

```text
.agents/reviews/architecture/REV-042.md
```

with:

```text
# Architecture Review

## Scope

## Findings

### ARCH-001
Severity: High
...

## Architectural Risks

## Recommendations

## Decision
APPROVED / CHANGES_REQUIRED
```

Security review:

```text
.agents/reviews/security/SEC-042.md
```

Testing:

```text
.agents/reviews/testing/QA-042.md
```

Now your agents communicate through standardized artifacts.

---

# 14. This enables an actual orchestration graph

Eventually, Claude can reason like:

```text
Task
 │
 ├── feature-development
 │
 ├── authorization
 │       │
 │       └── security-review
 │
 ├── database-migration
 │
 └── integration-testing
```

Then the orchestration system resolves dependencies:

```text
authorization
      ↓
security-review

database-migration
      ↓
integration-testing
```

And creates the execution plan:

```text
1. Architecture analysis
2. Database design
3. Implementation
4. Migration
5. Unit tests
6. Integration tests
7. Security review
8. Architecture review
9. Final validation
```

That's much more scalable than manually telling Claude:

> "Use Codex, then ask Gemini to review it."

---

# 15. I'd also introduce Skill metadata

For a large system, every skill can have metadata.

For example:

```yaml
name: authorization-review
type: security
version: 1.2
risk: critical

activation:
  - authorization
  - access-control
  - privilege

requires:
  knowledge:
    - security/authorization
    - security/identity
  skills:
    - code-review

produces:
  - security-review

agents:
  preferred:
    - security-reviewer
```

This turns Skills into something closer to **declarative workflow components**.

Your orchestrator can eventually parse them.

---

# 16. But don't over-engineer immediately

I'd evolve this progressively.

### Stage 1

```text
.skills/
├── feature-development/
├── bug-fix/
├── code-review/
├── security-review/
└── database-migration/
```

Just Markdown.

### Stage 2

Add:

```text
activation
dependencies
inputs
outputs
```

### Stage 3

Add metadata:

```yaml
type:
risk:
requires:
produces:
```

### Stage 4

Build orchestration:

```text
Task
 ↓
Skill discovery
 ↓
Dependency resolution
 ↓
Agent assignment
 ↓
Execution
 ↓
Review
 ↓
Artifact generation
```

### Stage 5

Automate it through your agent infrastructure.

---

# 17. One subtle distinction: Skills vs Roles

Don't confuse these.

**Role:**

> "You are a Security Engineer."

**Skill:**

> "Perform a threat-modeling analysis."

Role describes **who the agent is acting as**.

Skill describes **what procedure it knows how to execute**.

Therefore:

```text
Security Agent
    │
    ├── threat-modeling
    ├── authorization-review
    ├── security-code-review
    └── vulnerability-analysis
```

Likewise:

```text
Implementation Agent
    │
    ├── feature-development
    ├── bug-fix
    ├── refactoring
    ├── api-development
    └── test-implementation
```

This distinction will become extremely useful when you have multiple models.

---

# 18. Your final AI infrastructure starts looking like this

```text
                         ┌───────────────┐
                         │     USER      │
                         └───────┬───────┘
                                 │
                                 ▼
                     ┌─────────────────────┐
                     │      CLAUDE         │
                     │ Engineering Lead    │
                     └──────────┬──────────┘
                                │
                ┌───────────────┼────────────────┐
                │               │                │
                ▼               ▼                ▼
           AGENTS.md        .agents/          .skills/
           POLICY           KNOWLEDGE         CAPABILITIES
                                │                │
                                │                │
                                └───────┬────────┘
                                        │
                                        ▼
                               Task Construction
                                        │
                       ┌────────────────┼────────────────┐
                       ▼                ▼                ▼
                  Architect          Codex           Security
                    Agent           Agent             Agent
                       │                │                │
                       └────────────────┼────────────────┘
                                        ▼
                                  Git / Artifacts
                                        │
                                        ▼
                                   Review Gates
                                        │
                                        ▼
                                     MERGE
```

And this gives you a very clean separation:

|Component|Question it answers|
|---|---|
|`AGENTS.md`|**What rules must agents obey?**|
|`.agents/`|**What does this project know?**|
|`.skills/`|**How should a class of work be performed?**|
|`CLAUDE.md`|**How should Claude orchestrate the work?**|
|Task artifact|**What exactly are we doing now?**|
|Agent|**Who performs the work?**|
|Git|**What actually changed?**|
|Review artifact|**Is the work acceptable?**|

That is the architecture I'd use for a large AI-assisted codebase.

**One additional recommendation:** don't necessarily call the directory `.skills` if you're using an ecosystem that already has a prescribed skill-discovery mechanism. Keep the **conceptual architecture** above, but conform the physical layout to the tooling's native convention. The important thing is that Skills remain **procedural, composable, discoverable, and separate from project knowledge**.