*This file is used to store Spring Boot server context for AIs*

*Note everything important about the server in here*

# Stack

- Java 21, Spring Boot 4.1, Gradle Kotlin DSL (`server/build.gradle.kts`)
- Jackson 3 — import from `tools.jackson.databind.*`, NOT `com.fasterxml.jackson.*`
- Hibernate ORM via `spring-boot-starter-data-jpa`, Jakarta Persistence (`jakarta.persistence.*`)
- Flyway (`flyway-database-postgresql`) owns the schema. Hibernate `ddl-auto` is never used to generate/alter schema — entities are written to match existing `V*__*.sql` migrations exactly, never the other way around.
- No extra ORM libraries (e.g. no hypersistence-utils) — native Postgres enum/JSON mapping is done with plain Hibernate 6.2+ annotations (see below).

# Migrations

- Authored with comments in `database/init/<domain>/`, then mirrored comment-free into `server/src/main/resources/db/migration/<domain>/` on request.
- SQL conventions: `BIGSERIAL` PKs, named FK/index constraints, single-line FK/index formatting, zero comments in the Flyway copy.

# Package structure

Domain-driven, one folder per subdomain under its bounded context, each with `entity/`, `repository/`, `service/`, `controller/`, `dto/` (created as needed, not all populated upfront):

```
auth/
  user/, role/, permission/, account/, apikey/, attribute/,
  password/, refreshtoken/, rolepermission/, session/, userrole/
  enums/          <- auth-schema-specific enums (e.g. AccountStatus)
global/
  enums/          <- cross-schema/public-schema enums (Locale, Theme, WeekStart, Currency)
  exception/, handler/, response/, utils/
security/
  authorization/, config/, identity/, oauth/, permission/, policy/, token/
```

Other top-level bounded contexts mirror the schemas: `analytics`, `audit`, `billing`, `core`, `customer`, `feature` (appointment/auth/notification/slot/workspace), `integration`, `notification`, `organization`, `resource`, `tenant`, `webhook`, `widget`.

# Entity conventions

Established across `UserEntity`, `RoleEntity`, `PermissionEntity`, `AccountEntity`, `ApiKeyEntity`, `RefreshTokenEntity`:

- **PK**: `BigInteger` field named `<domain>Id` (e.g. `userId`, `roleId`, `permissionId`), mapped to DB column `id`, via `@GeneratedValue(strategy = GenerationType.IDENTITY)` — matches `BIGSERIAL`.
- **Timestamps**: `Instant createdAt` / `Instant updatedAt` via `@CreationTimestamp` / `@UpdateTimestamp` (`org.hibernate.annotations`) — matches `TIMESTAMPTZ ... DEFAULT now()`. `createdAt` is `updatable = false`. `Instant` (not `OffsetDateTime`) is the standard choice, matching the platform's canonical-UTC temporal architecture goal.
- **UUID columns** (e.g. `users.uid`): `@UuidGenerator` (`org.hibernate.annotations.UuidGenerator`), `nullable = false, unique = true, updatable = false`. Hibernate generates the UUID client-side; the DB's `gen_random_uuid()` default is not relied on.
- **Postgres native enum columns** (e.g. `public.locale`, `public.theme`, `public.week_start`, `auth.account_status`): map to a Java enum with `@Enumerated(EnumType.STRING)` + `@JdbcTypeCode(SqlTypes.NAMED_ENUM)`. No extra dependency needed — this is native Hibernate 6.2+ support.
- **JSONB columns**: map to `tools.jackson.databind.JsonNode` via `@JdbcTypeCode(SqlTypes.JSON)`.
- **Enum placement**: enums scoped to one Postgres schema and reused only within that schema's tables live in `<schema>/enums/` (e.g. `auth/enums/AccountStatus.java`). Enums under the `public` Postgres schema, shared across domains, live in `global/enums/` (`Locale`, `Theme`, `WeekStart`, `Currency`).
- **Lombok**: `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder` on every entity. `RoleEntity`/`PermissionEntity`/`AccountEntity` also carry `@Getter`/`@Setter`; `UserEntity` currently does not — flagged here as an observed inconsistency, not a deliberate rule, in case it should be reconciled later.
- Columns with a SQL `DEFAULT` that Hibernate doesn't auto-populate (e.g. plain non-generated NOT NULL columns like `locale`, `theme`, `week_start`, `account_status`, `timezone`) should mirror the DB default as a `@Builder.Default` literal, so entities built via the Lombok builder without setting them don't send an explicit `NULL` that would shadow the DB default and violate `NOT NULL`.
- **Not every table has `created_at`/`updated_at`** — e.g. `auth.accounts` (OAuth provider linkage) has neither; don't add timestamp fields unless the migration actually declares those columns.
- **FK to a schema/entity that doesn't exist yet**: map the raw column as a plain `BigInteger` field (not a relationship) instead of inventing/blocking on an entity. Examples: `ApiKeyEntity.tenantId` — `tenant_id` FKs to `tenant.tenants(id)`, but no `TenantEntity` exists yet (see `AGENTS.md`: api_keys have no usage until the tenant module is built); `RefreshTokenEntity.widgetId` — same treatment, `widget_id` FKs to `widget.widgets(id)` (added via `V63__auth_add_refresh_token_widget_fk.sql`, `ON DELETE SET NULL`) but no `WidgetEntity` exists yet. `owner_user_id` / `user_id` on those same tables *do* use a real `@ManyToOne` since `UserEntity` already exists.
- **`@Builder.Default` should mirror an actual DB `DEFAULT`, not invent business logic** — `AccountEntity.type` was given `@Builder.Default private String type = "oauth"` even though `auth.accounts.type` has no SQL default; this is a deliberate application-level choice made outside this convention, not a migration-mirroring default. Worth knowing this field can diverge from the "mirror the DB default" rule above.
- **`NOT NULL` without a DB default is still a plain required column, not a generated one** — e.g. `refresh_tokens.expires_at` is `TIMESTAMPTZ NOT NULL` with no `DEFAULT`, so `RefreshTokenEntity.expiresAt` is just `@Column(nullable = false)`, not `@CreationTimestamp`/`@UpdateTimestamp` or a `@Builder.Default`. The app is expected to always supply it explicitly.
- **A table may have `created_at` with no `updated_at`** — `auth.refresh_tokens` only has `created_at` (`@CreationTimestamp`, no `@UpdateTimestamp`); tokens are immutable once issued (revoked via `revoked_at`, not row updates). Don't assume the pair always comes together — check the migration.
- **Renamed columns**: `refresh_tokens.client` was renamed to `device_name` in `V76__rename_refresh_tokens_client.sql`. When a later migration renames/alters a column, map the entity to the *current* column name, not the one in the original `CREATE TABLE`.
- **Skeleton files can have copy-paste bugs** — `RefreshTokenEntity`'s stub had `@Table(name = "api_keys")` (leftover from `ApiKeyEntity`). Verify `@Table(schema, name)` against the actual migration filename/table before trusting a pre-existing stub.

# Entity relationships

- **Many-to-one (owning FK side)**: `@ManyToOne(fetch = FetchType.LAZY)` + `@JoinColumn(name = "<fk_column>", nullable = ...)`, matching the migration's `NOT NULL`/nullability on the FK column. Example: `AccountEntity.user` → `auth.accounts.user_id`. DB-level `ON DELETE CASCADE` is not mirrored as a JPA cascade — cascade behavior lives in the DB, not the entity.
- **Many-to-many**: `@Builder.Default Set<...> = new HashSet<>()` + `@ManyToMany(fetch = FetchType.EAGER)` + `@JoinTable(joinColumns = ..., inverseJoinColumns = ...)`. Examples: `UserEntity.roles` (via `auth.user_roles`), `RoleEntity.permissions` (via `auth.role_permissions`).
  - **Inconsistency to watch**: `UserEntity`'s `@JoinTable` sets `schema = "auth"` explicitly; `RoleEntity`'s does not (relies on the default schema even though `role_permissions` lives in `auth`). Reconcile if it causes a resolution issue.
