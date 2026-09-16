# Metadata

Title:
Domain:
Priority:
Risk:
Status: ACTIVE | COMPLETED

# Structure

- Identity
- Intent
- Deliverables
- Scope
- Out Of Scope
- Boundaries
- Constraints
- Dependencies
- Input Context
- Functional Requirements
- Non-functional Requirements
- Acceptance Criteria
- Validation
- Escalation
- Implementation Plan
- Review Strategy

---

## Template

```text
TASK CONTRACT

## 1. IDENTITY

Title: Resource-Level Authorization
Domain: Authorization
Priority: High
Risk: High
Status: Active

---

## 2. INTENT

Implement resource-level authorization so that access to a resource
is determined by the authenticated actor's relationship to the
resource and its owning organization.

The implementation must preserve tenant isolation and integrate
with the existing authorization architecture without replacing
the current RBAC model.

---

## 3. DELIVERABLES

The implementation agent must produce:

1. Production implementation.
2. Unit tests.
3. Integration tests.
4. Required configuration/wiring changes.
5. Documentation updates when required.
6. Summary of modified files.
7. Validation results.
8. Known limitations or unresolved risks.

Review agents must produce their corresponding review artifacts.

## 4. SCOPE

### In Scope

- ResourcePolicy
- MembershipRepository integration required by the policy
- Resource authorization decision logic
- Authorization-related unit tests
- Authorization-related integration tests
- Required wiring/configuration for the existing authorization engine
- Relevant security documentation updates if behavior or architecture changes

### Out of Scope

- Authentication
- JWT generation or verification
- Identity provider integration
- OAuth implementation
- Redesign of the authorization engine
- Redesign of the RBAC model
- General API redesign
- Unrelated resource functionality

---

## 5. BOUNDARIES

The implementation must operate within the existing:

- authentication boundary.
- authorization engine.
- domain/module boundaries.
- RBAC model.
- tenant isolation model.

Do not introduce a second authorization mechanism.

Do not bypass the existing AuthorizationService or equivalent
authorization boundary.

Do not access another module's persistence layer directly when
an established application/domain interface exists.

---

## 6. CONSTRAINTS

### Architectural

- Preserve existing module boundaries.
- No cross-module repository access.
- Reuse existing authorization abstractions where applicable.
- Do not introduce a new authorization framework.
- Existing RBAC semantics must remain unchanged.

### Security

- Tenant isolation is mandatory.
- An actor must never access resources belonging to an unrelated organization.
- Authorization must be based on authenticated identity and server-side data.
- Client-provided organization/resource ownership claims must not be trusted.
- Authorization failures must fail closed.
- Unauthenticated requests must not reach protected resource operations.

### Compatibility

- Existing authentication behavior must remain unchanged.
- Existing authorization policies must remain functional.
- Existing API contracts should remain unchanged unless explicitly required.

---

## 7. DEPENDENCIES

### Required Components

- AuthorizationService
- AuthorizationPolicy
- AuthenticatedActor / principal context
- Organization membership model
- Resource ownership model
- MembershipRepository or approved membership access abstraction

### Related Policies

- Existing organization-level authorization policies
- Existing resource access policies

### Required Infrastructure

- Existing authorization configuration/registry
- Existing security context integration

---

## 8. INPUT CONTEXT

The agent must inspect the following before implementation:

### Architecture

- `obsidian/PleaseBookMe/Security/Architecture/Security Architecture.md`
- `obsidian/PleaseBookMe/Security/Context/Authorization Context.md`
- `docs/resource/RESOURCE_SCHEMA.md`

### Domain

- organization
- resource

### Decisions

- `.agents/decisions/ADR-004.md`

### Source

Inspect the existing implementations of:

(Those can be the path)

- AuthorizationService
- AuthorizationPolicy
- authorization policy registry
- AuthenticatedActor
- organization membership access
- resource ownership/access model

The listed context is the minimum required context.
The agent may retrieve additional context when required.

---

## 9. FUNCTIONAL REQUIREMENTS

### 1. Organization Owner Access

An authenticated organization owner must be authorized to access
resources belonging to their organization.

### 2. Assigned Staff Access

An authenticated staff actor must be authorized to access a resource
when the actor has an explicit valid assignment to that resource
according to the existing domain model.

### 3. Cross-Tenant Isolation

An actor belonging to organization A must not be authorized to access
resources belonging to organization B unless an explicitly documented
cross-organization authorization rule already exists.

### 4. Unauthenticated Access

Unauthenticated actors must not be authorized to access protected
resources.

### 5. Authorization Failure

Authorization must fail closed when:

- the actor cannot be resolved;
- the resource does not exist;
- organization ownership cannot be established;
- membership cannot be established;
- required authorization data is unavailable.

---

## 10. NON-FUNCTIONAL REQUIREMENTS

### Security

The implementation must not introduce:

- IDOR.
- privilege escalation.
- cross-tenant access.
- authorization bypass.
- trust in client-controlled ownership information.

### Architecture

The implementation must conform to the existing
authorization architecture and module dependency direction.

### Maintainability

Authorization logic should remain explicit, testable, and isolated
from transport/framework concerns.

---

## 11. ACCEPTANCE CRITERIA

### 1. Owner Access

Given an authenticated organization owner,
when the owner accesses a resource belonging to their organization,
the authorization decision is ALLOW.

### 2. Assigned Staff Access

Given an authenticated staff actor,
when the actor accesses a resource to which they are assigned,
the authorization decision is ALLOW.

### 3. Unassigned Staff

Given an authenticated staff actor,
when the actor accesses a resource to which they are not assigned,
the authorization decision is DENY unless another documented
authorization rule grants access.

### 4. Cross-Organization Access

Given an authenticated actor from organization A,
when the actor attempts to access a resource belonging to
organization B,
the authorization decision is DENY.

### 5. Unauthenticated Access

Given no authenticated actor,
when protected resource access is attempted,
the request is rejected.

### 6. Missing Authorization Data

Given incomplete or unresolved ownership/membership information,
the authorization decision is DENY.

### 7. Existing Authorization

Existing authorization policies and RBAC behavior must continue
to pass without regression.

---

## 12. VALIDATION

### Automated Tests

Required:

- policy unit tests.
- authorization service tests where affected.
- integration tests for protected resource access.
- regression tests for existing authorization behavior.

### Required Security Scenarios

Test at minimum:

- authenticated owner → own organization resource.
- authenticated staff → assigned resource.
- authenticated staff → unassigned resource.
- organization A → organization B resource.
- unauthenticated → protected resource.
- missing resource.
- missing membership.
- invalid/unknown actor.
- tampered client-side ownership information.

### Static / Build Validation

Run the repository's standard:

- formatter.
- linter.
- compiler/build.
- test suite.
- static analysis.

Record the commands executed and their results.

---

## 13. ESCALATION

The agent must stop and escalate to the orchestrator when:

- requirements conflict with existing architecture.
- required authorization semantics are ambiguous.
- existing documentation conflicts with source behavior.
- tenant ownership cannot be reliably established.
- implementation requires changing a protected architectural boundary.
- the RBAC model must be changed.
- a security-critical assumption cannot be verified.
- acceptance criteria cannot be satisfied without expanding scope.

Do not silently resolve architectural or security ambiguity.

---

## 14. IMPLEMENTATION PLAN

*This should be very detail*

The implementation should:

1. Inspect the existing authorization flow.
2. Identify the correct authorization policy extension point.
3. Determine the minimum membership/ownership data required.
4. Implement resource-level authorization within the existing
   authorization abstraction.
5. Reuse existing principal/actor resolution.
6. Avoid coupling the policy directly to transport/framework APIs.
7. Add focused unit tests.
8. Add integration tests covering the complete authorization path.
9. Run the required security review.

Do not create new abstractions unless existing abstractions cannot
express the required behavior.

If a new abstraction is necessary, document the reason and architectural
impact before introducing it.

---

## 15. REVIEWS

This task requires:

### Architecture Review

Required because authorization behavior is being extended.

Review:

- module boundaries.
- policy placement.
- dependency direction.
- compatibility with the existing authorization engine.

### Security Review

Mandatory.

Review:

- tenant isolation.
- privilege escalation.
- IDOR.
- fail-closed behavior.
- actor identity resolution.
- ownership validation.
- authorization bypass opportunities.

```