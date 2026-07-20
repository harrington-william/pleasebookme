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
