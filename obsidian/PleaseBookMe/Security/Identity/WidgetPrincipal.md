# What It Is

`WidgetPrincipal` is one of the two implementations of the sealed **[[AuthenticatedPrincipal]]** interface — see that page for the shared contract, why the interface is sealed, and the field every implementation must carry (`actorType`/`subject`/`tenantUid`). This page covers only what's specific to the Widget actor.

It is deliberately minimal — a widget is a single-purpose actor with a fixed, narrow capability set bound to exactly one tenant, not an RBAC participant. It has no roles, no permissions, no organization/membership concept at all, unlike **[[UserPrincipal]]**.

Package: `com.pleasebookme.server.security.identity.principal`. Implements `AuthenticatedPrincipal, Serializable`.

```java
public record WidgetPrincipal(
    @NotNull
    AuthenticatedActorType actorType,

    UUID widgetUid,
    UUID tenantUid,
    BigInteger tenantId,

    WidgetStatus status

) implements AuthenticatedPrincipal, Serializable {
    @Override
    public UUID subject() {
        return widgetUid;
    }

    public boolean isActive() { ... }
}
```

---

# Fields

| Field | Type | Notes |
| --- | --- | --- |
| `actorType` | `AuthenticatedActorType` | Always `WIDGET`. |
| `widgetUid` | `UUID` | The widget's own identifier. Not named `subject` on the record — see below. |
| `tenantUid` | `UUID` | The owning tenant's UUID, satisfying the sealed contract's `tenantUid()` accessor. Always populated — a widget is never self-provisioned, it's manually created for a business that is already a paying tenant, so a `WidgetPrincipal` without a tenant cannot exist under the current onboarding model. |
| `tenantId` | `BigInteger` | The owning tenant's **surrogate primary key** — a second, separate reference to the same tenant, in the DB's native ID type. See below for why this exists alongside `tenantUid`. |
| `status` | `WidgetStatus` | `widget`-schema enum: `REGISTERING`, `ACTIVE`, `DISABLED`, `REVOKED`. Drives `isActive()`. |

---

# `subject()` Is Overridden, Not a Canonical Field

Note that the record's second component is named `widgetUid`, not `subject` — `WidgetPrincipal` overrides `subject()` explicitly to return `widgetUid`:

```java
@Override
public UUID subject() {
    return widgetUid;
}
```

This is a naming/domain-clarity choice: within `WidgetIdentityLoader` and everywhere else a widget is discussed, "widget UID" is the meaningful name; "subject" is only meaningful at the `AuthenticatedPrincipal` contract boundary. The override reconciles the two without forcing the interface's generic vocabulary onto the concrete type's own field names.

---

# Why Both `tenantUid` (UUID) and `tenantId` (BigInteger) Exist

This is the one place a principal carries the *same* reference twice, in two different ID types, and it is deliberate rather than redundant:

- `tenantUid` (`UUID`) exists **only** to satisfy `AuthenticatedPrincipal.tenantUid()` — the sealed contract standardizes on `UUID` because that's the external, JWT-safe identifier type used across every `*Uid` column in the codebase (per `CODING_CONVENTIONS.md`'s UID-naming rule).
- `tenantId` (`BigInteger`) is the actual surrogate PK (`tenant.tenants.id`), and it's what `WidgetTenantIsolationPolicy` and `ResourceScope` actually compare against:

```java
// WidgetTenantIsolationPolicy.evaluate
if (!scope.matchesTenant(widgetPrincipal.tenantId())) { ... }
```

`ResourceScope.tenantId()` is itself a `BigInteger` (mirroring `TenantEntity`'s PK type), because scope resolution walks JPA entity graphs (`ScopeResolver<BookingEntity>` → `Booking.getService().getOrganization()` → `TenantRepository.findByOrganizationOrganizationId(...)`) where everything is keyed by the DB-native `BigInteger` ID, not the externally-facing UUID. So `WidgetPrincipal` keeps both: the UUID for the identity-layer contract, the `BigInteger` for the authorization-layer scope comparison — converting between them on every check would be wasted work and an easy place to introduce a bug.

`WidgetIdentityLoader.map(WidgetEntity)` populates both directly off the same `TenantEntity` association:

```java
return new WidgetPrincipal(
    AuthenticatedActorType.WIDGET,
    widget.getWidgetUid(),
    widget.getTenant().getTenantUid(),
    widget.getTenant().getTenantId(),
    widget.getStatus()
);
```

---

# Helper Methods

- `isActive()` — `status == WidgetStatus.ACTIVE`. Backs `ActorStatusPolicy`'s veto — the same role `UserPrincipal.isActive()` plays for users, reached through the exhaustive sealed-type switch described in **[[AuthenticatedPrincipal]]**. Note `WidgetStatus` has four values (`REGISTERING`, `ACTIVE`, `DISABLED`, `REVOKED`); only `ACTIVE` passes.

---

# What's Deliberately Absent

No `roles`, no `permissions`, no `attributes` map. A widget's entire capability set today is the fixed allow-list hardcoded in `WidgetCapabilityPolicy` (`AVAILABILITY.READ`, `SERVICE.READ`, `SELECTEDSLOT.CREATE`/`DELETE`, `BOOKING.CREATE`, `ATTENDEE.CREATE`) — see below and **[[Security Architecture]]** section 8/12. A future per-widget, non-RBAC scope set (reusing the `auth.permissions` slug vocabulary) is anticipated but not yet built; when it lands, it is more likely to arrive as a new field on `WidgetPrincipal` than as a reuse of `UserPrincipal`'s `roles`/`permissions` shape, since a widget still won't be an RBAC participant.

`WidgetPrincipal` also never passes through `UserDetailsAdapter` or `PrincipalUserDetails` (contrast **[[UserPrincipal]]**) — it has no password/account-lock concepts for `UserDetails` to represent, so wrapping it in one would be modeling a lie.

---

# Where It's Consumed

**Produced by**
- `WidgetIdentityLoader.loadByPublicKey(publicKey, secretKey, origin)` — the widget bootstrap path: verifies the secret key, then runs the pre-authentication origin/status/expiry gate (see **[[Security Architecture]]**, section 4) before ever constructing a principal.
- `WidgetIdentityLoader.loadByUid(uid)` — the per-request reconstruction path `JwtAuthenticationFilter` calls on every subsequent request bearing a widget JWT.

Both map straight from `WidgetEntity` in one step — there is no aggregation and no mapper for Widget, unlike `UserPrincipal`'s `AuthenticationAggregation`/`UserPrincipalMapper` pair.

**Token & Session** (`security/token/`)
- `DefaultAuthenticationTokenFactory.createWidgetAuthentication` — the `WidgetPrincipal` branch of the sealed-type switch, storing the principal directly as the principal on a `PreAuthenticatedAuthenticationToken` with no `GrantedAuthority`s (`Collections.emptyList()`).

**Authorization** (`security/authorization/`)
- `WidgetCapabilityPolicy` — narrows to `WidgetPrincipal` via `instanceof`. Permits only if the requested `<RESOURCE>.<ACTION>` slug is in the fixed allow-list; denies everything else. Abstains for any other actor type.
- `WidgetTenantIsolationPolicy` — narrows to `WidgetPrincipal` via `instanceof`. Denies if the resource's tenant scope doesn't match `widgetPrincipal.tenantId()` (see "Why Both `tenantUid`..." above); otherwise abstains.

**Not Yet Implemented**

`WidgetPrincipal` currently carries no scopes or authorities of its own — `AuthenticationTokenFactory` grants an empty authority set, and `WidgetCapabilityPolicy`'s hardcoded allow-list is the only thing standing in for a real capability model. See **[[Security Architecture]]** section 12 for the anticipated evolution.
