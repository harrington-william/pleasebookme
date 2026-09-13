# Task Contract

## 1. IDENTITY

Title: Widget Configuration — Backend (tenant provisioning, widget CRUD reshape,
       credentials, scoped list/stats, soft delete)
Domain: Server / `widget/widgets` + `widget/widgetorigin` (Spring Boot)
        Server / `service/auth` (`UserProvisioningService`) + new
        `service/tenant` (`CurrentTenantProvider`)
        Database / `widget`, `tenant` schemas (Flyway `V139`–`V141`)
Priority: High — TASK-0008 (the dashboard Widgets page) cannot start until this
          lands; it consumes the wire contract in §9.1 verbatim.
Risk: Medium — three data/seed migrations, one provisioning change on the
      registration path (affects `POST /api/v1/auth/register` **and** the Google
      one-shot callback), one existing endpoint removed (`GET /api/v1/widgets`
      unscoped list-all), request DTO reshaped. No entity relationships change.

Status: ACTIVE

---

## 2. INTENT

Make the `widget` bounded context usable by a real dashboard page. Today it has a
generic CRUD stack that **cannot actually be used**: every widget requires a
`tenant_id` and no tenant row has ever been provisioned (dev DB: 3 users, 3
organizations, **0 tenants**); `secret_key` is written verbatim although the
bootstrap verifier expects a BCrypt hash; `expires_at` had to be invented by the
client; the list endpoint is unscoped and unfiltered; the request DTO forces the
client to send a secret it can never read back.

After this task:

1. Every newly registered user owns a tenant on the `FREE` plan, and the three
   existing dev organizations are backfilled.
2. `WidgetServiceImpl` derives its tenant from the caller's organization
   context — no `tenantId` in any request.
3. Credentials are server-generated on demand, hashed at rest, and returned in
   plaintext exactly once.
4. The list is organization-scoped, paginated, filterable by `type`/`status`,
   and hydrates the widget's registered origin in one batched query.
5. `DELETE` is a soft delete (`status = REVOKED`); revoked widgets vanish from the
   list and are excluded from `total`.

### Decisions already made — do not reopen

These were settled with the task author on 2026-09-11/12. Implement them; do not
relitigate them in the closing report.

| # | Decision |
|---|---|
| D1 | Tenant provisioning is fixed **inside `UserProvisioningService`** (a new `FREE`-plan tenant per organization), not lazily on first widget create. |
| D2 | The server generates + BCrypt-hashes the key pair. Generation happens **only when the client explicitly asks** (`POST /api/v1/widgets/credentials`), never implicitly on `POST /api/v1/widgets`. The plaintext secret is returned once, from the generation call, and never again. |
| D3 | `widget_origins.origin` keeps its **embed allow-list** semantics — the thing `DefaultWidgetIdentityLoader.validateOrigin` checks the browser `Origin` header against. It is not a redirect URL. |
| D4 | `DELETE /api/v1/widgets/{id}` sets `status = REVOKED`. No row is removed. |
| D5 | `expires_at` is nullable (V136, already applied). Nothing in this task writes it; it stays `null`. |
| D6 | Authorization is **out of scope** (see §6). Ownership *derivation* (which tenant does this widget belong to) is in scope — that is data integrity, not authorization. |

---

## 3. DELIVERABLES

### Database

1. `V139__tenant_plans_seed_free_limits.sql`
2. `V140__tenant_seed_general_ecosystem.sql`
3. `V141__tenant_backfill_tenants_for_organizations.sql`

### Server

4. `UserProvisioningServiceImpl` writes a `tenant.tenants` row.
5. `service/tenant/service/CurrentTenantProvider` + `impl/CurrentTenantProviderImpl`.
6. `widget/widgets/**` reshaped: DTOs, credential generator, specification
   package, service, controller, one new exception.
7. `widget/widgetorigin/**`: repository finder fix + origin normalizer.
8. `EcosystemRepository.findByCode`, `TenantPlanRepository.findByCode`.
9. `GlobalExceptionHandler`: two new handler methods.

### Tests

10. Unit tests listed in §12.

### Documentation

11. `SERVER_AGENTS.md`, `SECURITY.md` known-gaps bullet, Obsidian widget/tenant
    docs, `.agents/issues/open/ISSUE-0002` — see §14 Phase F.

### Closing report

12. Modified-file list, executed validation commands with output, unresolved
    risks, and — critically — the **exact JSON of one real `POST /api/v1/widgets`
    response and one `GET /api/v1/widgets?organizationId=…` response** so
    TASK-0008 can type against reality rather than this document.

Review agents produce `.agents/reviews/TASK-0007-widget-page-backend-review.md`.

---

## 4. SCOPE

### In Scope

- Migrations `V139`–`V141` (Flyway copies only — see §6 Architectural on the
  `database/init/` mirror).
- `service/auth/service/impl/UserProvisioningServiceImpl.java`.
- New package `service/tenant/`.
- `tenant/ecosystem/repository/EcosystemRepository.java`,
  `tenant/plan/repository/TenantPlanRepository.java` (one finder each).
- Everything under `widget/widgets/` except `entity/` relationships.
- `widget/widgetorigin/repository/WidgetOriginRepository.java` and a new
  `widget/widgetorigin/support/OriginNormalizer.java`.
- `global/handler/GlobalExceptionHandler.java` (additive).
- `WidgetEntity` annotation fix only: `expires_at` is nullable now.
- Tests under `server/src/test/**` for the classes above.
- Documentation listed in §14 Phase F.

### Out of Scope — do not touch

- **Anything under `security/authorization/**`**, `@PreAuthorize`, permission
  checks, `WIDGET.*` permission seeds (already exist in V82). See §6.
- `security/identity/loader/widget/**`, `security/identity/verifier/**`,
  `POST /api/v1/auth/widget/bootstrap`. The bootstrap flow is a **consumer** of
  what this task writes (hashed secret, normalized origin); it is not modified.
  If you find it needs a change to accept what you write, that is Escalation #4.
- `WidgetOriginController` / `WidgetOriginService` generic CRUD — leave the
  endpoints as they are. Only the repository gains finders.
- Tenant quota enforcement (`tenants.max_widgets`). Data is written; nothing
  reads it yet.
- Schedule / availability / notification-preference provisioning
  (`ISSUE-0002`'s other gaps). This task closes the **tenant** gap only.
- `UserPrincipal.tenantUid` and `DefaultUserIdentityLoader`. Provisioning a
  tenant does not change the principal; that is `ISSUE-0001`'s follow-up.
- `auth.refresh_tokens` cleanup on revoke.
- Client code. TASK-0008 owns `client/**`.
- `database/init/**` mirror (numbering has diverged — see §6).

---

## 5. BOUNDARIES

- The **only** writer of `tenant.tenants` at registration time is
  `UserProvisioningServiceImpl`. Do not add a second provisioning path, a lazy
  get-or-create in `CurrentTenantProvider`, or a tenant write inside
  `WidgetServiceImpl`. A user whose organization has no tenant gets
  `TenantNotFoundException` (404) from the widget endpoints — that is the
  correct signal for a pre-V141 account, not something to paper over.
- `CurrentTenantProvider` sits **on top of** `CurrentOrganizationProvider`. It
  must not re-implement membership resolution; it calls
  `currentOrganizationProvider.requireCurrent()` and maps organization → tenant.
- `WidgetServiceImpl` is the only writer of `widget.widgets`. It may write
  `widget.widget_origins` rows for its own widget (the nested-origin upsert,
  §9.4) — same local deviation TASK-0002 authorised for
  `BusinessServiceImpl` + booking policies, for the same reason: one
  transaction, no second bean, no second endpoint.
- Secrets never leave the server except from
  `POST /api/v1/widgets/credentials`. `WidgetResponse` must not gain a
  `secretKey` field. Nothing logs a secret.
- Do not introduce Redis into the widget domain. The stateless generator
  (§9.3) is deliberately stateless; the Redis-staged alternative is recorded in
  §16 as future hardening, not built here.
- Do not touch `WidgetEntity`'s associations or add columns. The schema this
  task needs is already applied (V136–V138).

---

## 6. CONSTRAINTS

### Security is deferred by explicit decision — read before "hardening" anything

The task author has a dedicated plan to build authorization policies across all
domains in one pass. This task is not part of it. Concretely:

- Do **not** add `@PreAuthorize`, do not reference `security/authorization/**`.
- `organizationId` on `GET /widgets` and `GET /widgets/stats` arrives as a query
  parameter. Follow `ResourceServiceImpl`'s shape exactly: resolve the caller's
  current organization, and on mismatch return an **empty page / all-zero
  stats**, never an error. That guard is the precedent's; keep it because the
  create path needs `CurrentOrganizationProvider` anyway and the list should
  agree with it. It is not an authorization mechanism and must not grow into
  one.
- `GET /widgets/{id}`, `PUT`, `DELETE` do **not** check that the widget belongs
  to the caller's tenant. Known, accepted, and listed in §16. If you spot
  something exploitable, write it in the closing report and move on.

### Architectural

- `CODING_CONVENTIONS.md` governs every controller/service/DTO/repository
  method. It is not `@`-included from `CLAUDE.md` — open it.
- Naming: interface + `Impl` (`CurrentTenantProviderImpl`,
  `WidgetServiceImpl`). The sibling `DefaultCurrentOrganizationProvider` is the
  older `Default*` style; TASK-0006 already ruled the skill wins. Do not rename
  the sibling.
- `Specification.unrestricted()` for an absent filter, never `null` —
  Spring Data JPA 4.x asserts non-null inside `Specification.and`. The
  `ResourceSpecificationsTest` exists precisely because this bit once.
- Two request records (`WidgetCreateRequest`, `WidgetUpdateRequest`) instead of
  one `WidgetRequest` — because `credentials` is mandatory on create and
  optional on update, and validation groups are not a pattern this codebase
  uses. Document the reason in one Javadoc line on each record.
- One new typed exception only, with a real throw site:
  `WidgetRevokedException` (409). Plus `InvalidWidgetOriginException` (400) in
  `widgetorigin/exception/`. Do not add exceptions that cannot be thrown.
- Flyway: `V136`–`V138` are **written but may not be applied yet** — on
  2026-09-12 the dev database still reported `MAX(version) = 135`. They apply
  on the next server boot. New files are `V139`, `V140`, `V141`. Before
  writing anything, run
  `SELECT MAX(version::numeric) FROM flyway_schema_history WHERE success`
  (§14 Step 1): `135` means boot once to apply V136–V138; `138` means already
  applied; anything else is Escalation #2. Zero comments in migration files
  (`.skills/technologies/flyway/SKILL.md`).
- `database/init/` is **not** updated. Its numbering diverged long ago
  (`V70__widgets.sql`, `V205__indexes_widget.sql`) and nothing from V126 on was
  mirrored. The Flyway tree is the only source of truth for this task.
- No new abstractions beyond those named in §9. If you believe one more is
  needed, stop and escalate (#8).

### Compatibility

- `POST /api/v1/auth/register` and the Google one-shot callback must keep
  working. Provisioning failure must roll the whole registration back — verify
  every caller of `provisionUser` is inside a transaction (§14 Step 5).
- `WidgetOriginController` endpoints unchanged.
- `POST /api/v1/auth/widget/bootstrap` must succeed against a widget created by
  the new `POST /api/v1/widgets` (§12 Validation #14). This is the acceptance
  test for D2 and D3 together.

---

## 7. DEPENDENCIES

### Already done — do not rebuild

| Component | Status |
|---|---|
| `V136__drop_widgets_expires_not_null.sql` | Written (applies on next boot). `expires_at` nullable. |
| `V137__indexes_widgets_tenant_status.sql` | Written. `idx_widgets_tenant_status (tenant_id, status)`; `idx_widgets_tenant_id` dropped. Serves every count and filter in §9. |
| `V138__unique_tenants_organization.sql` | Written. `uq_tenants_organization UNIQUE (organization_id)`; `idx_tenants_organization_id` dropped. Backs `TenantRepository.findByOrganizationOrganizationId` returning `Optional`. |
| `TenantRepository.findByOrganizationOrganizationId`, `existsBySlug` | Exist. |
| `CurrentOrganizationProvider.requireCurrent()` | Exists (`service/organization/`). Throws `NoOrganizationMembershipException` / `AmbiguousOrganizationContextException`, both already handled. |
| `PasswordEncoder` bean | `CryptoConfig` → `BCryptPasswordEncoder(11)`. The same bean `DefaultWidgetVerifier` uses. Inject it; do not create another. |
| `PkceGenerator` | `security/oauth/google/pkce/`. The `SecureRandom` + url-safe-Base64-without-padding precedent to copy for the credential generator. |
| `ResourceSpecifications`, `ResourceSort`, `ResourceFilter`, `ResourcePageResponse`, `ResourceStatsResponse` | The list/filter/pagination/stats precedent. Copy the shape. |
| `BusinessServiceImpl.createService` / `upsertBookingPolicy` | The `@Transactional` nested-child upsert precedent for origins. |
| `TenantNotFoundException`, `EcosystemNotFoundException`, `TenantPlanNotFoundException`, `WidgetNotFoundException`, `DuplicateWidgetException` + handlers | Exist. |
| Seeds `V100` (`BARBERSHOP` ecosystem), `V102` (`FREE` plan with only `max_services`/`max_resources`) | Exist. V139/V140 complete them. |

### Baseline

Verify before touching anything (§14 Step 1):

```text
./gradlew compileJava
./gradlew test
```

Both must be green on a clean tree. If not, that is Escalation #1 — do not fix
unrelated tests.

### Required infrastructure

- Postgres + Redis via the docker-compose integration (`SERVER_AGENTS.md` →
  Infrastructure). Redis is needed only because the app will not boot without
  it.
- A signed-in user with exactly one accepted membership (the dev accounts
  qualify) for the `curl` validations.

---

## 8. INPUT CONTEXT

### Skills to invoke (mandatory)

| Skill | When |
|---|---|
| `.skills/technologies/flyway/SKILL.md` | Before writing V139–V141. Version sequencing and the no-comments rule. |
| `.skills/technologies/postgres/tables/SKILL.md` | Index/constraint formatting if any DDL is needed (V139–V141 are DML only). |
| `.skills/technologies/spring-boot/services/SKILL.md`, `.../controller-declaration/SKILL.md`, `.../repositories/SKILL.md`, `.../method-declaration/SKILL.md` | Before writing any service/controller/repository. |
| `.skills/technologies/spring-boot/entity-declaration/SKILL.md` | For the one-line `WidgetEntity` change. |
| `.skills/workflows/quality-code-comments/SKILL.md` | Comments say **why**. Every deliberate decision in §9 that a future reader would "fix" needs one; nothing else does. |
| `.skills/workflows/performance-avoid-quadratic/SKILL.md` | Before §9.5 (batched origin hydration). |
| `.skills/domains/api/api-tester/SKILL.md` | For §12 — how this repository verifies contracts, and the secret-exclusion checklist. |
| `.skills/documentation/api-documentation/SKILL.md` | Phase F — the Obsidian API pages. |
| `.skills/workflows/code-review/SKILL.md` | Reviewers. |

### The patterns to mirror (read in full)

| File | Why |
|---|---|
| `server/.../resource/resources/service/impl/ResourceServiceImpl.java` | Org-context create, guarded list, stats via `countBy*`. The shape for `WidgetServiceImpl`. |
| `server/.../resource/resources/specification/*.java` | `Specification.unrestricted()`, sort whitelist, page-size cap. |
| `server/.../resource/resources/dto/*.java` | `ResourceFilter`, `ResourcePageResponse`, `ResourceStatsResponse` — copy the records, rename. |
| `server/.../resource/resources/controller/ResourceController.java` | `@PageableDefault`, `@RequestParam(required = false)` filters, `/stats`. |
| `server/.../core/service/service/impl/BusinessServiceImpl.java` | `@Transactional` create with a nested child, `upsertBookingPolicy`, and `ServiceCreateResult` (entity + child returned together so the controller can build one response). |
| `server/.../core/service/dto/ServiceCreateResult.java` | The two-field result record. `WidgetDetail` is its twin. |
| `server/.../security/oauth/google/pkce/PkceGenerator.java` | `SecureRandom`, `Base64.getUrlEncoder().withoutPadding()`. |
| `server/.../service/organization/service/impl/DefaultCurrentOrganizationProvider.java` | What `CurrentTenantProviderImpl` wraps. |
| `server/.../service/auth/service/impl/UserProvisioningServiceImpl.java` | The file you extend. Note the org-slug collision fallback; replicate it for the tenant slug. |
| `server/.../security/identity/loader/widget/DefaultWidgetIdentityLoader.java` | The **reader** of what you write. `validateOrigin` uses `existsByWidgetWidgetIdAndOrigin(widgetId, origin)` with the raw `Origin` header — this is why §9.4 normalizes to `scheme://host[:port]`. |
| `server/.../security/identity/verifier/DefaultWidgetVerifier.java` | `passwordEncoder.matches(secret, widget.getSecretKey())` — this is why the stored value must be a BCrypt hash. |
| `server/src/test/.../resource/resources/ResourceSpecificationsTest.java`, `ResourceSortTest.java` | Test shape for the specification package. |
| `server/src/test/.../core/service/BusinessServiceImplTest.java` | Mockito service-test shape. |

### Domain facts

| Source | Fact |
|---|---|
| `V47__widgets.sql` + `V98` + `V136` | `widgets`: `tenant_id NOT NULL`, `name VARCHAR(255) NOT NULL`, `status DEFAULT 'REGISTERING'`, `type DEFAULT 'EMBEDDED'`, `origin_validation DEFAULT false`, `public_key TEXT NOT NULL UNIQUE`, `secret_key TEXT NOT NULL`, `issued_at DEFAULT now()`, `expires_at` nullable, `last_used_at` nullable. No `service_id`. |
| `V48__widget_origins.sql` | `(widget_id, origin) UNIQUE`; `origin VARCHAR(255) NOT NULL`; `created_by` nullable → `auth.users`; `created_at` only. |
| `WidgetStatus` | `REGISTERING, ACTIVE, DISABLED, REVOKED`. This task never writes `REGISTERING`. |
| `WidgetType` | `INLINE, POPUP, FULL_PAGE, EMBEDDED`. Server accepts all four; the "INLINE only" restriction is a **client** rule (TASK-0008). |
| `V24__tenant_tenants.sql` | `tenants`: `organization_id`, `owner_user_id`, `ecosystem_id`, `plan_id` all `NOT NULL`; `name`, `slug UNIQUE`; `status tenant.tenant_status NOT NULL` (no default); `region tenant.region NOT NULL` (no default); `default_timezone DEFAULT 'Australia/Sydney'`; `default_locale DEFAULT 'en'`; `max_users`, `max_services`, `max_widgets` `NOT NULL` (no default). |
| `V101` + `V102` | `plans.max_*` all nullable; `FREE` seeded with `max_services = 10`, `max_resources = 10`, the other three **NULL**. V139 fixes this. |
| `V100` | Only ecosystem is `BARBERSHOP`. V140 adds `GENERAL`. |
| `TenantStatus` / `TenantRegion` | `ACTIVE, SUSPENDED, TRIAL, PENDING, ARCHIVED` / `AU, UK, US, SG, VN`. |
| `WidgetOriginRepository.findByWidgetWidgetId` | Returns `Optional` on a 1:N table — throws `IncorrectResultSizeDataAccessException` on a second origin. No caller today. Replace, don't keep. |
| `WidgetEntity.issuedAt` | `@CreationTimestamp`, no `updatable = false`. Rotation must be able to reset it — §14 Step 9 verifies the UPDATE actually carries `issued_at`. |
| BCrypt | 72-byte input limit. `pbm_sk_` + 43 chars = 50 bytes. Safe. |
| `AGENTS.md` → Platform Stage | `billing`/`analytics`/`webhook` excluded. Nothing here touches them. |
| `.agents/issues/open/ISSUE-0002` | The five seed/schema blockers for tenant provisioning. §9.2 resolves 1–5. |

---

## 9. FUNCTIONAL REQUIREMENTS

### 9.1 Wire contract — TASK-0008 types against this verbatim

```text
POST   /api/v1/widgets/credentials              200  WidgetCredentialResponse
POST   /api/v1/widgets                          201  WidgetResponse
GET    /api/v1/widgets?organizationId=&type=&status=&page=&size=&sort=
                                                200  WidgetPageResponse
GET    /api/v1/widgets/stats?organizationId=    200  WidgetStatsResponse
GET    /api/v1/widgets/{widgetId}               200  WidgetResponse
PUT    /api/v1/widgets/{widgetId}               200  WidgetResponse
DELETE /api/v1/widgets/{widgetId}               204  (soft delete → REVOKED)
```

`GET /api/v1/widgets` **without** `organizationId` is removed (it was the
unscoped list-all). A missing `organizationId` is a 400 from Spring's required
`@RequestParam`, same as `ResourceController`.

```java
// widget/widgets/dto/WidgetCredentialResponse.java
record WidgetCredentialResponse(String publicKey, String secretKey)
// The ONLY response in the domain that carries a secret. Javadoc says so.

// widget/widgets/dto/WidgetCredentialRequest.java
record WidgetCredentialRequest(
    @NotBlank @Pattern(regexp = "^pbm_pk_[A-Za-z0-9_-]{22}$") String publicKey,
    @NotBlank @Pattern(regexp = "^pbm_sk_[A-Za-z0-9_-]{43}$") String secretKey
)
// The @Pattern is a deliberate, documented exception to CODING_CONVENTIONS'
// "no format validators beyond the column type": these values are ones the
// server itself minted via /credentials, so rejecting anything else is not
// inventing a constraint — it is refusing a credential the platform did not
// issue. One Javadoc sentence on the record says this.

// widget/widgets/dto/WidgetCreateRequest.java
record WidgetCreateRequest(
    @NotBlank @Size(max = 255) String name,
    WidgetType type,                      // optional; entity default EMBEDDED
    @Size(max = 255) String origin,       // optional; null/blank = no origin
    @NotNull @Valid WidgetCredentialRequest credentials
)

// widget/widgets/dto/WidgetUpdateRequest.java
record WidgetUpdateRequest(
    @NotBlank @Size(max = 255) String name,
    WidgetType type,
    WidgetStatus status,                  // optional; only ACTIVE / DISABLED accepted
    @Size(max = 255) String origin,
    @Valid WidgetCredentialRequest credentials   // optional; present = rotate
)

// widget/widgets/dto/WidgetResponse.java  (existing record, ONE field added)
record WidgetResponse(
    BigInteger widgetId, UUID widgetUid, BigInteger tenantId, String name,
    WidgetStatus status, WidgetType type, Boolean originValidation,
    String publicKey,
    String origin,                        // NEW — normalized, null when none
    Instant issuedAt, Instant expiresAt, Instant lastUsedAt,
    Instant createdAt, Instant updatedAt
)
// static from(WidgetDetail detail)

// widget/widgets/dto/WidgetDetail.java   (service → controller carrier, mirrors ServiceCreateResult)
record WidgetDetail(WidgetEntity widget, WidgetOriginEntity origin)   // origin nullable

// widget/widgets/dto/WidgetFilter.java
record WidgetFilter(WidgetType type, WidgetStatus status) { static none() }

// widget/widgets/dto/WidgetPageResponse.java
record WidgetPageResponse(List<WidgetResponse> content, int page, int size,
                          long totalElements, int totalPages)
// static from(Page<WidgetDetail>)

// widget/widgets/dto/WidgetStatsResponse.java
record WidgetStatsResponse(long total, long active, long disabled, long revoked)
// total = count(status <> REVOKED). `revoked` is reported for honesty; the
// client's two tiles read `active` and `disabled`.
```

Removed from the request surface, deliberately: `tenantId` (derived),
`publicKey`/`secretKey` as top-level fields (moved under `credentials`),
`originValidation` (derived: `origin != null`), `expiresAt`, `lastUsedAt`
(server-owned). Keep `WidgetRequest.java` **deleted**, not empty.

### 9.2 Tenant provisioning

`UserProvisioningServiceImpl.provisionUser` writes a sixth row, after the
profile:

| Column | Value | Why |
|---|---|---|
| `organization` | the organization just created | 1:1 per V138 |
| `ownerUser` | the user just created | |
| `ecosystem` | `ecosystemRepository.findByCode("GENERAL")` → `EcosystemNotFoundException` if absent | V140 seeds it. `BARBERSHOP` is one vertical; assigning every signup to it would be a lie. |
| `plan` | `tenantPlanRepository.findByCode("FREE")` → `TenantPlanNotFoundException` if absent | D1 |
| `name` | `organization.getName()` | |
| `slug` | `organization.getSlug()`; if `tenantRepository.existsBySlug(...)`, fall back to `user.getUserUid().toString()` | Same collision rule the org slug already uses. |
| `status` | `TenantStatus.ACTIVE` | "The free plan is a real plan, not a trial" (ISSUE-0002 #4). |
| `region` | `resolveRegion(user.getTimezone())` — private helper: `Australia/*` → `AU`, `Europe/London` → `UK`, `America/*` → `US`, `Asia/Singapore` → `SG`, everything else → `VN` | VN is the launch market; the helper keeps the row honest for the four other regions the enum names. One comment explaining the fallback. |
| `defaultTimezone` | `user.getTimezone()` | |
| `defaultLocale` | `user.getLocale()` | |
| `maxUsers`, `maxServices`, `maxWidgets` | copied from the plan | Denormalized by design (`Table Tenants.md`). If any is `null`, throw `IllegalStateException("Plan FREE is missing quota limits; apply V139")` — a deploy-time misconfiguration must fail loudly, not write a wrong quota. |

Constants `DEFAULT_PLAN_CODE = "FREE"` and `DEFAULT_ECOSYSTEM_CODE = "GENERAL"`
on the impl. Inject `TenantRepository`, `EcosystemRepository`,
`TenantPlanRepository`.

**Migrations that make it possible:**

```sql
-- V139__tenant_plans_seed_free_limits.sql
UPDATE tenant.plans SET max_users = 1, max_widgets = 3, max_api_keys = 1 WHERE code = 'FREE';
```

(`max_users = 1`, `max_widgets = 3`, `max_api_keys = 1` are the task author's
provisional free-tier numbers. Nothing enforces them yet; they are data. If the
author changes them before you start, use the author's.)

```sql
-- V140__tenant_seed_general_ecosystem.sql
INSERT INTO tenant.ecosystems(code, name, description, icon, status)
VALUES ('GENERAL', 'General', 'Service businesses that do not fit a listed vertical', '🏷️', 'ACTIVE');
```

```sql
-- V141__tenant_backfill_tenants_for_organizations.sql
INSERT INTO tenant.tenants (organization_id, owner_user_id, ecosystem_id, name, slug, status, plan_id, region, default_timezone, default_locale, max_users, max_services, max_widgets)
SELECT o.id, m.user_id, e.id, o.name, o.slug, 'ACTIVE'::tenant.tenant_status, p.id, 'VN'::tenant.region, o.timezone, u.locale, p.max_users, p.max_services, p.max_widgets
FROM organization.organizations o
JOIN LATERAL (SELECT user_id FROM organization.memberships WHERE organization_id = o.id AND accepted ORDER BY created_at, id LIMIT 1) m ON true
JOIN auth.users u ON u.id = m.user_id
JOIN tenant.plans p ON p.code = 'FREE'
JOIN tenant.ecosystems e ON e.code = 'GENERAL'
WHERE NOT EXISTS (SELECT 1 FROM tenant.tenants t WHERE t.organization_id = o.id)
AND NOT EXISTS (SELECT 1 FROM tenant.tenants t WHERE t.slug = o.slug);
```

V141 is a no-op on an empty database and idempotent on re-run. It exists so the
three dev accounts can create widgets without re-registering. Owner = earliest
accepted member, which today is always the sole member. Verify the exact column
list against `\d tenant.tenants` before committing — `uid`, `created_at`,
`updated_at` have defaults and are omitted on purpose.

### 9.3 Credentials

`widget/widgets/credential/WidgetCredentialGenerator.java` (`@Component`):

- `WidgetCredentialPair generate()` → `record WidgetCredentialPair(String publicKey, String secretKey)`.
- `publicKey = "pbm_pk_" + base64url(16 random bytes)` → 22 chars.
- `secretKey = "pbm_sk_" + base64url(32 random bytes)` → 43 chars.
- One `SecureRandom` instance per component, url-safe encoder without padding —
  copy `PkceGenerator`.
- The prefixes are what let a leaked key be recognised in logs and grep'd out
  of a repository; the fixed lengths are what `@Pattern` in §9.1 enforces. One
  comment says this.

`WidgetService.generateCredentials()` returns the pair. The controller maps it
to `WidgetCredentialResponse`. **Nothing is persisted.** The endpoint is a pure
function; calling it a hundred times writes nothing.

At `create` and at rotation the service stores
`passwordEncoder.encode(request.credentials().secretKey())` in `secretKey` and
the public key verbatim. It checks `existsByPublicKey` first
(`DuplicateWidgetException` → 409) — on rotation only when the public key
actually changes.

### 9.4 Origin

`widget/widgetorigin/support/OriginNormalizer.java` (`final`, static
`normalize(String raw)`):

- `null` / blank → `null`.
- Trim. If no `://`, prepend `https://`.
- Parse with `java.net.URI`. Require scheme `http` or `https` and a non-empty
  host. Reject userinfo. Output `scheme://host` lowercase, plus `:port` only
  when a port is present and is not the scheme default (80/443).
- Path, query and fragment are **dropped silently** — a user pasting
  `https://barbershop.com/book` meant the site, and the browser `Origin` header
  never carries a path.
- Anything unparseable → `InvalidWidgetOriginException(message)` → 400.

Why this exists, in one comment: `DefaultWidgetIdentityLoader.validateOrigin`
compares the raw `Origin` request header against `widget_origins.origin` with
`equals`. Browsers send `scheme://host[:port]`, lowercase, no trailing slash. A
row that stores anything else can never match, and origin validation would
silently reject every legitimate embed.

**Upsert rule** (private `upsertOrigin(WidgetEntity, String normalized)` on
`WidgetServiceImpl`, inside the same `@Transactional` as the parent write):

| Request `origin` | Existing rows | Action |
|---|---|---|
| `null` | any | delete all rows for the widget; `originValidation = false` |
| `X` | none | insert `{widget, origin = X, verified = false, createdBy = context.user()}`; `originValidation = true` |
| `X` | one row `= X` | nothing |
| `X` | rows ≠ X | delete rows ≠ X; insert X if missing; `originValidation = true` |

`createdBy` is stamped from `OrganizationContext.user()`, never from the
request. `verified` stays `false` — nothing verifies origins yet.

### 9.5 List, stats, sort

`WidgetRepository` gains `extends JpaSpecificationExecutor<WidgetEntity>`,
`long countByTenantTenantIdAndStatus(BigInteger, WidgetStatus)`,
`long countByTenantTenantIdAndStatusNot(BigInteger, WidgetStatus)`.

`WidgetOriginRepository`: replace the `Optional` finder with
`List<WidgetOriginEntity> findAllByWidgetWidgetIdIn(Collection<BigInteger>)`.

`widget/widgets/specification/WidgetSpecifications`: `hasTenant(tenantId)`,
`isNotRevoked()`, `hasType(WidgetType)`, `hasStatus(WidgetStatus)`. The last two
return `Specification.unrestricted()` for `null`. No text search — no UI
consumes one, and the project rule is to add a filter only when a page uses it.

`widget/widgets/specification/WidgetSort.sanitize(Pageable)`: whitelist
`name, status, type, createdAt, updatedAt`; default `createdAt DESC`; cap 100.

`getWidgetsByOrganizationId(organizationId, filter, pageable)`:

1. `currentOrganizationProvider.requireCurrent()`; mismatch → `Page.empty(safePageable)`.
2. `currentTenantProvider.requireByOrganizationId(organizationId)` → tenant.
3. `Specification.allOf(hasTenant, isNotRevoked, hasType, hasStatus)`.
4. `Page<WidgetEntity> page = widgetRepository.findAll(spec, safePageable)`.
5. **One** `findAllByWidgetWidgetIdIn(ids)`; group into
   `Map<BigInteger, WidgetOriginEntity>` keeping the row with the lowest
   `widgetOriginId` per widget (deterministic when legacy data has several).
6. `page.map(widget -> new WidgetDetail(widget, originsById.get(widget.getWidgetId())))`.

`isNotRevoked` composes **before** `hasStatus`, so `?status=REVOKED` yields an
empty page rather than resurrecting soft-deleted rows. Comment it.

`getWidgetStatsByOrganizationId`: same two guards, then
`total = countByTenantTenantIdAndStatusNot(tenantId, REVOKED)`,
`active`, `disabled`, `revoked` via `countByTenantTenantIdAndStatus`. Four
queries, all served by `idx_widgets_tenant_status`.

### 9.6 Create / read / update / delete

`createWidget(WidgetCreateRequest)` — `@Transactional`:

1. `context = currentOrganizationProvider.requireCurrent()`;
   `tenant = currentTenantProvider.requireCurrent()`.
2. `existsByPublicKey` → `DuplicateWidgetException`.
3. `normalized = OriginNormalizer.normalize(request.origin())`.
4. Build: `.tenant(tenant).name(...).publicKey(...).secretKey(encode(...))`
   `.status(WidgetStatus.ACTIVE)` — explicit, because the entity default is
   `REGISTERING`, which meant "credentials not yet issued", a state this flow
   never passes through. `.originValidation(normalized != null)`. `type` only
   when supplied. `expiresAt`/`lastUsedAt` untouched (null).
5. `save`, then `upsertOrigin`, return `WidgetDetail`.

`getWidgetById(id)` → `WidgetDetail` (single origin lookup via
`findAllByWidgetWidgetIdIn(List.of(id))`, same lowest-id rule). Returns REVOKED
widgets too — a direct read is not the list.

`updateWidget(id, WidgetUpdateRequest)` — `@Transactional`:

1. Load; if `status == REVOKED` → `WidgetRevokedException` (409). A revoked
   widget is dead; `PUT` must not resurrect it.
2. `name` full-replace; `type` guarded (`if != null`).
3. `status`: apply only when it is `ACTIVE` or `DISABLED`. Any other value
   (`REGISTERING`, `REVOKED`, `null`) leaves the stored status untouched —
   `REVOKED` is reachable only through `DELETE`, and `REGISTERING` is not a
   state this flow ever writes. One comment says so. Do not add an exception
   for it; silently ignoring an unreachable state is the honest behaviour
   here, and TASK-0008 never sends one.
4. `credentials` present → `existsByPublicKey` if changed → set both, encode
   the secret, `setIssuedAt(Instant.now())`.
5. `upsertOrigin`; `originValidation = normalized != null`.
6. `save`, return `WidgetDetail`.

`deleteWidget(id)`:

1. Load. If already `REVOKED`, return — idempotent 204, no write.
2. `setStatus(REVOKED)`, `save`. Origin rows are kept (they are part of the
   audit trail of what was authorised).

### 9.7 `CurrentTenantProvider`

```java
// service/tenant/service/CurrentTenantProvider.java
TenantEntity requireCurrent();                          // via CurrentOrganizationProvider.requireCurrent()
TenantEntity requireByOrganizationId(BigInteger organizationId);
```

Both resolve through `tenantRepository.findByOrganizationOrganizationId` and
throw `TenantNotFoundException("Organization <id> has no tenant; it predates
tenant provisioning (V141)")`. `@Transactional(readOnly = true)`. That message
is the one a pre-backfill dev account will see — make it say what to do.

### 9.8 Controller

`WidgetController` (`/api/v1/widgets`), in this order so the literal paths win
over `/{widgetId}`:

```java
@PostMapping("/credentials")                                   generateCredentials
@PostMapping  @ResponseStatus(CREATED)                         createWidget(@Valid WidgetCreateRequest)
@GetMapping("/stats")                                          getWidgetStats(@RequestParam organizationId)
@GetMapping                                                    getWidgets(@RequestParam organizationId,
                                                                 @RequestParam(required=false) WidgetType type,
                                                                 @RequestParam(required=false) WidgetStatus status,
                                                                 @PageableDefault(size = 20) Pageable)
@GetMapping("/{widgetId}")                                     getWidget
@PutMapping("/{widgetId}")                                     updateWidget(@Valid WidgetUpdateRequest)
@DeleteMapping("/{widgetId}") @ResponseStatus(NO_CONTENT)      deleteWidget
```

Controllers map DTO ↔ service only. `WidgetResponse.from(WidgetDetail)` is the
one mapping call.

### 9.9 Exceptions

- `widget/widgets/exception/WidgetRevokedException` → 409 `CONFLICT`.
- `widget/widgetorigin/exception/InvalidWidgetOriginException` → 400 `BAD_REQUEST`.
- Both wired in `GlobalExceptionHandler` by copying an adjacent handler method.
  `ApiErrorResponse` shape unchanged.

---

## 10. NON-FUNCTIONAL REQUIREMENTS

### Must not exist when this task is done

- Any response DTO other than `WidgetCredentialResponse` carrying a secret, a
  hash, or a `secretKey` field.
- Any log line containing a generated secret (grep the diff for
  `secretKey` inside `log.`/`System.out`).
- A `tenantId`, `expiresAt`, `lastUsedAt`, `originValidation`, top-level
  `publicKey`/`secretKey` field on either request record.
- A `null`-returning specification factory.
- A per-row origin query inside a loop or a `Page.map` (N+1).
- `GET /api/v1/widgets` answering without `organizationId`.
- A `Redis*` dependency in `widget/**`.
- `@PreAuthorize`, any `security/authorization/**` import in `widget/**` or
  `service/tenant/**`.
- A migration with a comment, or numbered ≤ 138.
- A second class named `WidgetRequest`.

### Quality

- Every new class compiles under the existing `./gradlew compileJava` with no
  new warnings introduced by this task.
- Comment density matches the surrounding code; see the skill.
- `WidgetCredentialGenerator` is the only place that knows the key format
  besides the two `@Pattern`s. Do not scatter the prefixes.

---

## 11. ACCEPTANCE CRITERIA

### Provisioning

1. Given a fresh `POST /api/v1/auth/register`, when it returns 201, then
   `tenant.tenants` has exactly one row for the new organization with
   `plan = FREE`, `ecosystem = GENERAL`, `status = ACTIVE`, non-null
   `max_users/max_services/max_widgets`, `default_timezone` equal to the
   user's, and `slug` equal to the organization slug.
2. Given the `FREE` plan has a `NULL` `max_widgets` (simulate in a unit test),
   when `provisionUser` runs, then it throws `IllegalStateException` and no
   tenant is saved.
3. Given V141 has been applied to the dev database, then every organization
   with an accepted member has a tenant, and re-applying the statement inserts
   nothing.

### Credentials

4. `POST /api/v1/widgets/credentials` returns 200 with `publicKey` matching
   `^pbm_pk_[A-Za-z0-9_-]{22}$` and `secretKey` matching
   `^pbm_sk_[A-Za-z0-9_-]{43}$`, and writes no row.
5. Given a pair from #4, `POST /api/v1/widgets` returns 201, the stored
   `secret_key` starts with `$2a$11$` (BCrypt, strength 11), `expires_at IS
   NULL`, `status = 'ACTIVE'`, and the response body has no `secretKey` key.
6. Given the same pair posted twice, the second `POST` returns 409.
7. `POST /api/v1/widgets` with `credentials.publicKey = "abc"` returns 400
   (bean validation), not 500.
8. Given a widget created in #5, `POST /api/v1/auth/widget/bootstrap` with
   that plaintext secret and a matching `Origin` returns 200. **This is the
   integration proof for D2 + D3.**

### Origin

9. `origin: "Barbershop.com/book?x=1"` is stored as `https://barbershop.com`,
   `origin_validation = true`, and echoed normalized in the response.
10. `origin: "http://localhost:3000"` is stored as `http://localhost:3000`.
11. `origin: "ftp://x"` and `origin: "https://"` return 400.
12. `origin: null` on create → no `widget_origins` row, `origin_validation =
    false`, response `origin: null`. On update of a widget that had one → the
    row is deleted and the flag drops to `false`.

### List / stats

13. `GET /widgets?organizationId=<mine>` returns only my tenant's non-revoked
    widgets, each with its `origin` populated, in one widgets query plus one
    origins query (verify with `spring.jpa.show-sql` or the Hibernate log).
14. `?status=DISABLED` returns only disabled; `?status=REVOKED` returns an
    empty page; `?type=INLINE` filters; `?sort=bogus,asc` is ignored and the
    default `createdAt,desc` applies; `?size=500` is capped at 100.
15. `GET /widgets?organizationId=<not mine>` → empty page, 200.
16. `GET /widgets/stats?organizationId=<mine>` → counts agree with the table;
    `total` excludes revoked; `revoked` counts them.

### Update / delete

17. `PUT` without `credentials` leaves `public_key`, `secret_key`, `issued_at`
    unchanged; with `credentials` replaces all three and `issued_at` moves.
18. `PUT` with `status: "DISABLED"` then `"ACTIVE"` round-trips; `status:
    "REVOKED"` in a `PUT` body is ignored.
19. `DELETE` → 204, `status = REVOKED`, row still present, origin rows still
    present; second `DELETE` → 204 with no write; subsequent `PUT` → 409;
    subsequent `GET /{id}` → 200 showing `REVOKED`; the widget is absent from
    the list.

### Structure

20. `WidgetRequest.java` does not exist. `WidgetCreateRequest`,
    `WidgetUpdateRequest`, `WidgetCredentialRequest`, `WidgetCredentialResponse`,
    `WidgetDetail`, `WidgetFilter`, `WidgetPageResponse`, `WidgetStatsResponse`
    exist under `widget/widgets/dto/`.
21. `CurrentTenantProvider` has no repository other than `TenantRepository` and
    no dependency other than `CurrentOrganizationProvider`.
22. `./gradlew test` is green, including the new tests in §12.

---

## 12. VALIDATION

### Automated (required)

| Test class | Covers |
|---|---|
| `service/auth/service/UserProvisioningServiceImplTest` (new, Mockito) | Tenant row fields (#1); null plan limit throws (#2); slug collision falls back to user UID; ecosystem/plan missing throws the typed exception. |
| `service/tenant/service/impl/CurrentTenantProviderImplTest` (new) | Maps org → tenant; missing tenant → `TenantNotFoundException`. |
| `widget/widgets/credential/WidgetCredentialGeneratorTest` (new) | Format regexes; 1,000 generations produce 1,000 distinct public keys; `BCryptPasswordEncoder.matches(secret, encode(secret))` is true. |
| `widget/widgetorigin/support/OriginNormalizerTest` (new) | Every row of #9–#12 plus: uppercase host, trailing slash, `:443` dropped, `:8443` kept, userinfo rejected, blank → null. |
| `widget/widgets/WidgetSpecificationsTest` (new) | Copy `ResourceSpecificationsTest`: `allOf` composes with every optional filter null; factories never return null. |
| `widget/widgets/WidgetSortTest` (new) | Copy `ResourceSortTest`. |
| `widget/widgets/WidgetServiceImplTest` (new, Mockito) | create hashes the secret and sets ACTIVE; duplicate public key → 409 exception before any save; update without credentials does not touch them; update on REVOKED throws; delete sets REVOKED and is idempotent; list issues exactly one `findAllByWidgetWidgetIdIn`. |
| `ServerApplicationTests` | Context still loads (catches a constructor cycle between `CurrentTenantProvider` and anything in `security/`). |

### Manual — run against the live server, record the commands and output

| # | Scenario | How | Expected |
|---|---|---|---|
| M1 | Flyway applies V139–V141 | boot the server; `SELECT MAX(version::numeric) FROM flyway_schema_history WHERE success` | `141` |
| M2 | Backfill | `SELECT o.id, t.id, t.slug, t.plan_id FROM organization.organizations o LEFT JOIN tenant.tenants t ON t.organization_id = o.id` | no NULL `t.id` |
| M3 | Register | `curl -X POST /api/v1/auth/register …` | 201; new tenant row (#1) |
| M4 | Generate | `curl -X POST /api/v1/widgets/credentials -H 'Authorization: Bearer …'` | 200, pattern (#4) |
| M5 | Create | `curl -X POST /api/v1/widgets` with `{name, type:"INLINE", origin:"Barbershop.com/book", credentials:{…}}` | 201 (#5, #9); paste the JSON into the closing report |
| M6 | Bootstrap | `curl -X POST /api/v1/auth/widget/bootstrap -H 'Origin: https://barbershop.com' -d '{publicKey, secretKey, origin}'` | 200 (#8) |
| M7 | Bootstrap, wrong origin | same with `Origin: https://evil.com` | 401 `WidgetOriginMismatchException` |
| M8 | List | `curl '/api/v1/widgets?organizationId=<mine>'` | 200 (#13); paste JSON |
| M9 | Stats | `curl '/api/v1/widgets/stats?organizationId=<mine>'` | #16 |
| M10 | Update, rotate | `PUT` with new credentials | #17; `issued_at` changed in DB |
| M11 | Delete | `DELETE` twice, then `PUT`, then list | #19 |
| M12 | Other org | list/stats with a foreign `organizationId` | empty / zeros (#15) |

### Static / build

```text
./gradlew compileJava
./gradlew test
```

Record both. "Tests passed" without the command output is a review finding.

---

## 13. ESCALATION

Stop and report to the orchestrator when:

1. The baseline `./gradlew test` is not green before you change anything.
2. `SELECT MAX(version)` is neither `135` (pre-boot) nor `138` (post-boot) —
   someone else has moved the high-water mark; renumber only after confirming
   with the author.
3. The `FREE` plan or `BARBERSHOP` ecosystem row is missing or has been edited
   by hand in the dev DB in a way V139/V141 would not handle.
4. `POST /api/v1/auth/widget/bootstrap` (M6) fails against a widget created by
   the new `POST /api/v1/widgets` for a reason inside `security/identity/**` —
   that layer is out of scope and must not be edited here.
5. Any caller of `provisionUser` turns out **not** to be inside a transaction
   (§14 Step 5). Do not add `@Transactional` to `provisionUser` itself without
   asking — it would change rollback semantics for both callers.
6. Hibernate refuses to write `issued_at` on update because of
   `@CreationTimestamp` (§14 Step 9) — report; the fallback is described there
   but is an entity change.
7. A dev account has **two** accepted memberships, so
   `CurrentOrganizationProvider.requireCurrent()` throws `Ambiguous…` and the
   manual validations cannot run.
8. You believe a new abstraction beyond those named in §9 is required.
9. A test that was green at baseline fails and the fix would be to edit the
   test.
10. You find an exploitable gap (cross-tenant read via `GET /{id}`, etc.). Note
    it, do not fix it — §6.

Do not silently resolve architectural or security ambiguity.

---

## 14. IMPLEMENTATION PLAN

### Phase A — ground truth (Step 1)

1. Clean tree. Run the baseline (§7). Query the DB: `MAX(version)` — if
   `135`, boot the server once so V136–V138 apply, then re-query and expect
   `138`; `SELECT * FROM tenant.plans`; `SELECT * FROM tenant.ecosystems`;
   `SELECT count(*) FROM tenant.tenants` (expect 0); `\d tenant.tenants` and
   `\d widget.widgets` to confirm `expires_at` is nullable and the two new
   indexes exist. Paste all of it into the closing report's "Baseline"
   section.

### Phase B — migrations (Steps 2–3)

2. Write `V139`, `V140`, `V141` under
   `server/src/main/resources/db/migration/tenant/` exactly as §9.2, adjusting
   V141's column list to `\d tenant.tenants`. No comments.
3. Boot the server once (`./gradlew bootRun` or `ServerApplicationTests`) to
   apply them. Run M1 and M2. Stop the server.

### Phase C — provisioning (Steps 4–6)

4. Add `Optional<EcosystemEntity> findByCode(String)` and
   `Optional<TenantPlanEntity> findByCode(String)`.
5. Grep every caller of `provisionUser` (`AuthServiceImpl.register`,
   `DefaultGoogleAccountResolver` → `DefaultGoogleOnboardingService.finalizeOnboarding`
   and `DefaultGoogleSignInService`) and confirm each is inside a
   `@Transactional` method. Record which annotation covers which caller. If
   one is not — Escalation #5.
6. Extend `UserProvisioningServiceImpl` per §9.2. Write
   `UserProvisioningServiceImplTest`. Run M3 against the live server and
   inspect the tenant row.

### Phase D — tenant context (Step 7)

7. Create `service/tenant/service/CurrentTenantProvider` and
   `service/tenant/service/impl/CurrentTenantProviderImpl` per §9.7. Test it.
   Run `ServerApplicationTests` — this is where a bean cycle would surface.

### Phase E — widget domain (Steps 8–15)

8. **DTOs** (§9.1). Delete `WidgetRequest.java`. Add the eight records. Fix
   `WidgetResponse` (add `origin`, `from(WidgetDetail)`).
9. **Entity**: `WidgetEntity.expiresAt` → drop `nullable = false`. Leave
   `@CreationTimestamp` on `issuedAt` for now; Step 12 verifies rotation
   writes it. If the UPDATE statement omits `issued_at`, the fallback is to
   remove `@CreationTimestamp` from `issuedAt` and set it explicitly in
   `createWidget` too — report it as Escalation #6 before doing so, since it
   is an entity change.
10. **Credentials**: `WidgetCredentialGenerator` + `WidgetCredentialPair` under
    `widget/widgets/credential/`. Test.
11. **Origin**: `OriginNormalizer` + `InvalidWidgetOriginException`. Fix
    `WidgetOriginRepository` (remove the `Optional` finder, add
    `findAllByWidgetWidgetIdIn`). Test the normalizer.
12. **Specification**: `WidgetSpecifications`, `WidgetSort`. Tests. Extend
    `WidgetRepository` (`JpaSpecificationExecutor`, two `countBy*`).
13. **Service**: rewrite `WidgetService` / `WidgetServiceImpl` per §9.3–9.7.
    Inject `WidgetRepository`, `WidgetOriginRepository`,
    `CurrentOrganizationProvider`, `CurrentTenantProvider`,
    `WidgetCredentialGenerator`, `PasswordEncoder`. `@Transactional` on
    `createWidget`, `updateWidget`, `deleteWidget`;
    `@Transactional(readOnly = true)` on the reads. Write
    `WidgetServiceImplTest`.
14. **Exceptions + handler**: `WidgetRevokedException`,
    `InvalidWidgetOriginException`, two handler methods.
15. **Controller**: rewrite per §9.8. Compile. Boot. Run M4–M12 in order;
    paste the M5 and M8 JSON into the report.

▶ **Run `./gradlew test` here**, before documentation.

### Phase F — documentation (Step 16)

16. Update, in this order:
    - `SERVER_AGENTS.md`: Migrations → "Current applied schema version:
      v141"; replace the `TenantEntity`/`WidgetEntity` CRUD bullets' stale
      parts with: tenant provisioning now written (what, defaults, region
      helper), `CurrentTenantProvider` (where, what it wraps), the widget
      reshape (two request records + why, credentials flow, origin
      normalization + why, soft delete, `isNotRevoked` ordering, batched
      origins). Keep it to the non-obvious; the code is the "what".
    - `SECURITY.md` → "Known gaps" first bullet: tenant is now provisioned;
      schedule/availability/notification prefs still are not. Also §10
      "Not Yet Implemented": the bootstrap endpoint exists (the Obsidian
      widget doc already flags this) — one sentence.
    - `.agents/issues/open/ISSUE-0002-workspace-provisioning-incomplete.md`:
      mark the tenant bullet and blockers 1–5 resolved with the migration
      numbers; leave the rest open.
    - Obsidian `API/Widget/Widgets/*.md` via the api-documentation skill:
      rewrite the six pages + summary for the new contract, add
      "Generate widget credentials" and "Get widget stats", state that
      `GET /widgets` requires `organizationId`.
    - Obsidian `Database/Schemas/Widget/Table Widgets.md` (nullable
      `expires_at`, new index, soft-delete semantics, hashed `secret_key`),
      `Table Widget Origins.md` (normalized form), `Tenant/Table Tenants.md`
      (provisioned at registration, unique org), `Table Plans.md` (FREE limits
      complete), `Table Ecosystems.md` (`GENERAL`).

### Phase G — report (Step 17)

17. Closing report per §3 item 12. Include the exact M5/M8 JSON — TASK-0008's
    `features/widgets/types/widget.ts` is written from it.

---

## 15. REVIEW STRATEGY

Reviewers follow `.skills/workflows/code-review/SKILL.md` and write
`.agents/reviews/TASK-0007-widget-page-backend-review.md` with role, scope,
findings (severity + evidence), recommendation.

### 15.1 What to review, by role

- **Architecture**: `service/tenant/` placement and dependency direction
  (`widget` → `service/tenant` → `service/organization` → `security/identity`,
  never the reverse); no second provisioning path; the origin upsert stays
  inside `WidgetServiceImpl` per the TASK-0002 precedent; two request records
  justified.
- **Security** (mandatory — credentials and the registration path changed):
  hash-at-rest, plaintext-once, no secret in any other response or log,
  `@Pattern` on credentials, origin normalization matches what the identity
  loader compares, `createdBy` from context not request, provisioning rollback
  on failure, and the §6 deferrals listed — not "fixed".
- **Database**: V139–V141 numbering, no comments, V141 idempotency and its
  `LATERAL` owner choice, no schema drift from the entity.
- **Performance**: exactly one origin query per list page; four count queries
  on `idx_widgets_tenant_status`; no `findAll()` anywhere in the widget list
  path.
- **Testing**: every §12 class exists and asserts the behaviour named, not just
  "does not throw".

### 15.2 Core components to read

`UserProvisioningServiceImpl`, `CurrentTenantProviderImpl`,
`WidgetServiceImpl`, `WidgetCredentialGenerator`, `OriginNormalizer`,
`WidgetSpecifications`, `WidgetController`, the three migrations, the two new
handler methods.

### 15.3 How to review — ordered checklist

1. Diff `WidgetResponse` / `WidgetCredentialResponse` against the
   secret-exclusion rule.
2. Grep `widget/**` and `service/tenant/**` for `security.authorization`,
   `PreAuthorize`, `Redis`. Expect nothing.
3. Read `createWidget` top to bottom: guard order (duplicate check before any
   build), `encode` present, `ACTIVE` explicit, `upsertOrigin` inside the
   transaction.
4. Read `updateWidget`: revoked guard first; credentials optional; `issued_at`
   moves on rotation and only then.
5. Read `getWidgetsByOrganizationId`: two guards, `isNotRevoked` before
   `hasStatus`, one batched origin query, deterministic lowest-id pick.
6. Read `OriginNormalizer` against `DefaultWidgetIdentityLoader.validateOrigin`.
7. Read `provisionUser`: null-limit throw, slug fallback, region helper,
   ecosystem/plan lookups by constant.
8. Confirm the closing report contains the M5/M8 JSON and the baseline output.

### 15.4 Findings that are *not* findings

- "No authorization on `GET/PUT/DELETE /{id}`" — §6, deferred by decision.
- "`@Pattern` violates CODING_CONVENTIONS" — §9.1 documents the exception.
- "Two request records instead of one" — §6 Architectural.
- "`database/init/` not updated" — §6.
- "`REGISTERING` is never written" — §9.6, intentional.
- "`UserPrincipal.tenantUid` still null" — out of scope (§4).

---

## 16. NOT IN THIS TASK — recorded so it is not rediscovered

- **Tenant-scoped `GET/PUT/DELETE /{id}`** — lands with the authorization pass.
- **Redis-staged credential generation** (generate → stage hash under a short
  TTL → `POST /widgets` sends only the public key). Would stop the secret
  transiting client → server a second time. The stateless version was chosen
  for now because the transit is a same-origin HTTPS POST body, not a URL.
- **Quota enforcement** on `tenants.max_widgets`.
- **Origin verification** (`widget_origins.verified` stays `false`).
- **Refresh-token cleanup on revoke** (`auth.refresh_tokens.owner = WIDGET`).
- **`UserPrincipal.tenantUid` non-null** and the loader change — `ISSUE-0001`.
- **Schedule / availability / notification-preference provisioning** —
  `ISSUE-0002`, still open.
- **Region from a signup picker** instead of the timezone heuristic.
