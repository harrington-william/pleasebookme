For a large-scale project such as PleaseBookMe, I would not have Claude, Codex, Gemini, etc. all independently work on the same codebase. That creates conflicting assumptions, duplicated work, and context drift.

Instead, give each model a **well-defined role**, establish a **shared source of truth**, and orchestrate them through explicit artifacts and gates.

# 1. The architecture I recommend

Think of the system as:

```text
                         ┌─────────────────────┐
                         │  Engineering Lead   │
                         │  Claude Code / You  │
                         └──────────┬──────────┘
                                    │
	                         Task decomposition
                                    │
              ┌─────────────────────┼─────────────────────┐
              ▼                     ▼                     ▼
       ┌─────────────┐       ┌─────────────┐       ┌─────────────┐
       │ Architecture│       │   Coding    │       │   Research  │
       │    Agent    │       │    Agent    │       │    Agent    │
       └─────────────┘       └─────────────┘       └─────────────┘
              │                     │                     │
              └─────────────────────┼─────────────────────┘
                                    ▼
                           ┌─────────────────┐
                           │ Review / Audit  │
                           │     Agents      │
                           └────────┬────────┘
                                    │
                           Findings / objections
                                    │
                                    ▼
                           ┌─────────────────┐
                           │ Engineering Lead│
                           │  Final decision │
                           └────────┬────────┘
                                    │
                              merge / accept
                                    ▼
                              ┌───────────┐
                              │   Git     │
                              └───────────┘
```

The important concept is:

> **One agent owns orchestration. Other agents perform specialized functions.**

Do not make every model an equal participant.

---

# 2. Give models different jobs

A very effective setup is something like this:

|Role|Model/Agent|Responsibility|
|---|---|---|
|**Engineering Lead**|Claude Code|Understand task, inspect repo, decompose work, coordinate|
|**Implementation Engineer**|Codex|Write production code|
|**Architecture Agent**|Claude / Gemini|Challenge architectural decisions|
|**Code Reviewer**|Codex / Claude|Review implementation|
|**Security Reviewer**|Security-focused model|Threat modeling, auth, injection, isolation|
|**Test Engineer**|Codex|Tests, edge cases, regression|
|**Research Agent**|Gemini / web-enabled model|Documentation, APIs, standards, libraries|
|**Performance Agent**|specialized model|Profiling hypotheses, scalability review|
|**Release Agent**|Claude Code|Integration, migrations, CI/CD, release checks|

Notice something important:

**Coding is only one role.**

For serious software engineering, the difficult part is often:

```text
Requirements
    ↓
Architecture
    ↓
Design
    ↓
Implementation
    ↓
Verification
    ↓
Security
    ↓
Integration
    ↓
Release
```

AI should participate in each stage independently.

---

# 3. Claude Code as the "Engineering Manager"

Your idea of using Claude Code to manage context is actually very strong.

I'd make Claude Code the **primary repository-aware orchestrator**.

Its job isn't necessarily to write every line of code.

Instead:

```text
Claude Code
    │
    ├── Understand repository
    ├── Read architecture
    ├── Understand current task
    ├── Identify affected modules
    ├── Create implementation plan
    ├── Delegate implementation
    ├── Inspect resulting changes
    ├── Request review
    ├── Resolve conflicts
    └── Integrate
```

For example:

> "Implement organization-level resource authorization."

Claude Code should first inspect:

```text
docs/
architecture/
auth/
organization/
resource/
booking/
audit/
tests/
```

Then determine:

```text
Affected modules:
    authorization
    organization
    resource
    audit

Potential database changes:
    resource_assignments

Potential policy changes:
    organization membership
    resource ownership
    staff assignment

Potential tests:
    cross-organization access
    staff access
    owner access
    unauthorized access
```

Only after that should it delegate implementation.

---

# 4. Codex becomes the implementation specialist

Then Claude could create an implementation task for Codex:

```text
Task:
Implement OrganizationResourceAccessPolicy.

Context:
- Authorization architecture:
  docs/architecture/authorization.md
- Resource model:
  resource/src/main/java/...
- Membership model:
  auth/src/main/java/...
- Existing policies:
  authorization/src/main/java/...

Constraints:
- Do not modify public API.
- Follow existing package conventions.
- Do not introduce new authorization abstraction.
- Maintain tenant isolation.
- Add unit + integration tests.
```

Codex then works on its own branch:

```text
feature/resource-authorization
```

This is much better than dumping the entire project context into Codex and saying:

> "Implement authorization."

---

# 5. The crucial concept: shared engineering context

This is where your context-engineering skills become extremely valuable.

You need a **model-independent context layer**.

Don't let your architecture exist only inside Claude's context window.

Create something like:

```text
.ai/
├── architecture/
│   ├── system.md
│   ├── modules.md
│   ├── authorization.md
│   ├── persistence.md
│   └── events.md
│
├── conventions/
│   ├── java.md
│   ├── testing.md
│   ├── api.md
│   ├── database.md
│   └── security.md
│
├── decisions/
│   ├── ADR-001-modular-monolith.md
│   ├── ADR-002-rbac.md
│   ├── ADR-003-event-driven.md
│   └── ADR-004-tenant-isolation.md
│
├── tasks/
│   ├── active/
│   └── completed/
│
├── reviews/
│   ├── architecture/
│   ├── security/
│   └── code/
│
└── agents/
    ├── claude.md
    ├── codex.md
    └── reviewers.md
```

This becomes your **Engineering Knowledge Base**.

The models are disposable.

The knowledge isn't.

---

# 6. Think in terms of artifacts, not conversations

This is probably the biggest improvement you can make.

Instead of:

```text
Claude → Codex → Claude → Gemini
```

think:

```text
Requirement
    ↓
Design Document
    ↓
Implementation Plan
    ↓
Code
    ↓
Test Report
    ↓
Security Review
    ↓
Architecture Review
    ↓
Integration Decision
```

Each agent consumes and produces artifacts.

For example:

```text
task.md
   ↓
design.md
   ↓
implementation
   ↓
review.md
   ↓
security-review.md
   ↓
decision.md
```

This makes the system **auditable**.

---

# 7. Use Git as the coordination mechanism

Git becomes the shared state machine.

For example:

```text
main
 │
 └── feature/authorization-resource-policy
        │
        ├── Codex implementation
        │
        ├── Security review
        │
        ├── Architecture review
        │
        └── Tests
```

You can even have specialized branches:

```text
feature/booking-rescheduling

├── implementation
├── review/security
├── review/architecture
└── review/testing
```

But normally I'd keep reviewers read-only against the implementation branch rather than having them modify the same code.

---

# 8. Never let multiple agents freely modify the same code

This is a common mistake.

Bad:

```text
Claude ─────┐
Codex ──────┼──→ same working directory
Gemini ─────┤
Reviewer ───┘
```

You eventually get:

```text
context conflict
+
file conflict
+
architectural conflict
+
dependency conflict
```

Instead:

```text
                  Git repository
                       │
            ┌──────────┴──────────┐
            │                     │
       Implementation         Reviewers
            │                     │
          Codex             read-only
            │                     │
            └──────────┬──────────┘
                       ▼
                     Lead
                       │
                    merge
```

The **implementation agent owns the write operation**.

Review agents generally produce findings.

---

# 9. Use review gates

This is where you can make the system enterprise-grade.

For example:

```text
                    TASK
                     │
                     ▼
              Architecture Plan
                     │
              ┌──────┴──────┐
              │             │
           Approved?       Reject
              │             │
             YES            └────→ redesign
              │
              ▼
          Implementation
              │
              ▼
           Unit Tests
              │
              ▼
        Integration Tests
              │
       ┌──────┴──────┐
       │             │
   Security       Architecture
    Review          Review
       │             │
       └──────┬──────┘
              ▼
          Lead Review
              │
        ┌─────┴─────┐
        │           │
      Reject      Approve
        │           │
        ▼           ▼
     Fix loop      Merge
```

This creates an **agentic CI/CD pipeline**.

---

# 10. Different reviewers should have different attack surfaces

Don't ask five models:

> "Review this code."

You'll get five variations of the same generic feedback.

Instead, define explicit review contracts.

### Security reviewer

Ask:

```text
Review exclusively for:

- authentication bypass
- authorization bypass
- tenant isolation
- IDOR
- privilege escalation
- injection
- insecure deserialization
- SSRF
- sensitive data exposure
- race conditions affecting security
- auditability

Do not comment on formatting or naming.
```

### Architecture reviewer

Ask:

```text
Review exclusively for:

- module boundaries
- dependency direction
- coupling
- cohesion
- domain leakage
- transaction boundaries
- persistence abstraction
- event boundaries
- scalability
- architectural consistency
```

### Database reviewer

Ask:

```text
Review exclusively for:

- normalization
- constraints
- indexes
- transaction semantics
- locking
- isolation
- query performance
- data integrity
```

### Testing reviewer

Ask:

```text
Review exclusively for:

- missing test cases
- boundary conditions
- concurrency
- integration coverage
- regression risk
- authorization matrix
```

This is **specialized ensemble reasoning**.

---

# 11. Don't let reviewers directly decide

This is another important architectural principle.

Suppose:

```text
Security Agent:
CRITICAL — authorization bypass.

Architecture Agent:
APPROVED.

Performance Agent:
APPROVED.
```

Who decides?

Not the agents.

Your orchestration layer / lead agent should evaluate:

```text
Finding
↓
Evidence
↓
Severity
↓
Validity
↓
Conflict with architecture
↓
Decision
```

For example:

```text
SECURITY-001

Severity: CRITICAL
Reviewer: Security Agent
Claim:
Resource ID can be accessed cross-tenant.

Evidence:
ResourceController.java:47

Status:
CONFIRMED

Action:
BLOCK MERGE
```

That is much more useful than:

> "Security agent thinks there might be an issue."

---

# 12. Introduce a formal "task contract"

Every AI task should have a contract.

Something like:

```text
TASK
ID: AUTH-042
Title: Resource authorization

OBJECTIVE
Implement resource-level authorization.

SCOPE
- ResourcePolicy
- MembershipRepository
- authorization tests

OUT OF SCOPE
- authentication
- JWT implementation
- API redesign

CONSTRAINTS
- tenant isolation mandatory
- no cross-module repository access
- existing RBAC model remains unchanged

INPUT CONTEXT
- authorization.md
- organization.md
- resource.md
- ADR-004

ACCEPTANCE CRITERIA
- organization owner can access resources
- assigned staff can access assigned resources
- unrelated organization cannot access resources
- unauthenticated request rejected

VALIDATION
- unit tests
- integration tests
- security review
```

Now Claude, Codex, Gemini, etc. don't need to reconstruct the task.

---

# 13. You can build an actual AI Orchestrator

Eventually, I would go beyond manually switching between Claude Code and Codex.

Build an orchestration service.

For example:

```text
                  ┌─────────────────┐
                  │ AI Orchestrator │
                  └────────┬────────┘
                           │
             ┌─────────────┼──────────────┐
             │             │              │
             ▼             ▼              ▼
          Claude         Codex          Gemini
          Manager       Coder          Research
             │             │              │
             └─────────────┼──────────────┘
                           │
                           ▼
                     Artifact Store
                           │
            ┌──────────────┼──────────────┐
            ▼              ▼              ▼
          Git           Notion         Database
```

The orchestrator could expose operations such as:

```text
create_task()
assign_agent()
provide_context()
run_agent()
collect_result()
run_review()
evaluate_findings()
request_revision()
approve()
merge()
```

Conceptually:

```python
task = orchestrator.create_task(
    "Implement resource authorization"
)

plan = claude.plan(task)

implementation = codex.implement(
    task=task,
    plan=plan
)

security = security_agent.review(
    task=task,
    diff=implementation.diff
)

architecture = architecture_agent.review(
    task=task,
    diff=implementation.diff
)

decision = claude.evaluate(
    implementation,
    security,
    architecture
)

if decision.approved:
    git.merge()
else:
    codex.revise(decision.findings)
```

That is essentially a **multi-agent software engineering system**.

---

# 14. MCP becomes extremely interesting here

Since you're already using tools such as GitHub, Notion, Figma, etc., you can treat them as **capabilities** rather than sources of intelligence.

For example:

```text
Claude
  │
  ├── GitHub MCP
  ├── Notion MCP
  ├── Database MCP
  ├── Filesystem
  └── CI/CD
```

Claude understands the project and orchestrates.

Codex gets:

```text
Git repository
task contract
relevant files
architecture artifacts
acceptance criteria
```

Security agent gets:

```text
Git diff
architecture
threat model
security policy
```

Research agent gets:

```text
technical question
official documentation
existing decisions
```

The **tools are shared infrastructure**.

The **agents are specialized cognition**.

---

# 15. Context should be hierarchical

Do not give every agent the entire project.

Use:

```text
L0 — Global
    System architecture
    Engineering principles
    Coding standards

L1 — Domain
    Authorization
    Booking
    Customer
    Billing

L2 — Module
    Authorization module
    Resource module

L3 — Task
    Resource authorization policy

L4 — Files
    Exact files relevant to implementation
```

A coding agent might receive:

```text
L0
+
L1 Authorization
+
L2 Resource
+
L3 Task
+
L4 relevant files
```

It shouldn't need:

```text
Billing
Notification
Analytics
Customer CRM
Widget distribution
```

unless they are actually relevant.

This dramatically reduces context pollution.

---

# 16. The most powerful pattern: recursive delegation

You can eventually have:

```text
Engineering Lead
        │
        ├── Architecture Lead
        │       ├── Domain architect
        │       └── Data architect
        │
        ├── Implementation Lead
        │       ├── Backend engineer
        │       ├── Frontend engineer
        │       └── Test engineer
        │
        ├── Security Lead
        │       ├── AppSec
        │       ├── IAM
        │       └── Threat modeling
        │
        └── QA Lead
                ├── Integration
                ├── E2E
                └── Regression
```

This resembles a real engineering organization.

But **don't start here**.

It becomes unnecessarily complex very quickly.

---

# 17. What I would build for your workflow

Given your background and the way you're already approaching PleaseBookMe, I'd start with **5 agents**.

### Agent 1 — Lead / Orchestrator

**Claude Code**

Responsibilities:

- repository understanding
    
- task decomposition
    
- context selection
    
- architecture decisions
    
- delegation
    
- integration
    
- final review
    

### Agent 2 — Principal Engineer

**Claude or another strong reasoning model**

Responsibilities:

- architecture review
    
- design alternatives
    
- ADR generation
    
- identify coupling
    
- challenge assumptions
    

### Agent 3 — Implementation Engineer

**Codex**

Responsibilities:

- implementation
    
- refactoring
    
- tests
    
- migrations
    
- debugging
    

### Agent 4 — Security Engineer

**Security-oriented model**

Responsibilities:

- threat modeling
    
- authorization review
    
- tenant isolation
    
- AppSec
    
- secrets
    
- dependency risks
    
- attack paths
    

### Agent 5 — QA / Verification Engineer

**Another model or Codex**

Responsibilities:

- test strategy
    
- edge cases
    
- integration tests
    
- regression analysis
    
- acceptance criteria verification
    

Then:

```text
                 Claude Code
                Lead Engineer
                     │
                     ▼
             ┌───────────────┐
             │ Task Contract │
             └───────┬───────┘
                     │
          ┌──────────┼───────────┐
          ▼          ▼           ▼
      Architect     Codex      Research
          │          │           │
          └──────────┼───────────┘
                     ▼
                  Git Diff
                     │
             ┌───────┴───────┐
             ▼               ▼
          Security          QA
           Review          Review
             │               │
             └───────┬───────┘
                     ▼
                Claude Lead
                     │
               ┌─────┴─────┐
               ▼           ▼
             Reject      Approve
               │           │
               ▼           ▼
             Codex        Merge
```

That is a very strong starting architecture.

---

# 18. The deeper principle

The goal isn't:

> **"How can I make multiple AIs collaborate?"**

The better question is:

> **"How can I construct a software engineering process where different AI agents produce independently verifiable artifacts?"**

That's a fundamentally different problem.

Once you have:

```text
Task Contracts
       +
Shared Knowledge
       +
Specialized Agents
       +
Git
       +
Review Gates
       +
Automated Tests
       +
Human/Lead Arbitration
```

the underlying models become interchangeable.

You could replace:

```text
Claude → another model
Codex → another coding model
Gemini → another research model
```

without redesigning your engineering system.

That's the architecture I would aim for if you're serious about eventually building an **AI-native software engineering workflow** rather than simply using several chatbots.

And for a project like **PleaseBookMe**, this could become especially interesting because your modular-monolith architecture, authorization engine, audit system, event-driven components, and extensive domain boundaries naturally provide excellent boundaries for agent delegation.

A useful next step would be to design the **actual orchestration protocol**—task schemas, agent contracts, shared `.ai/`repository structure, Git branching model, review gates, and how Claude Code would invoke Codex and the other agents.