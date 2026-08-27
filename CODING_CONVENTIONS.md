# Coding Convention

### Field selection

- `Request` carries only what the client may supply: exclude the PK, UID, and any `@CreationTimestamp`/`@UpdateTimestamp` column
- `Response` carries the PK, UID (if present), every plain scalar column, every FK flattened to its raw ID (see below), and both timestamps
- Both DTOs exclude relationship collections (e.g. a `@ManyToMany` set) and free-form `metadata` JSON columns until a concrete need for them shows up — don't expose everything preemptively
- A foreign key always appears in both DTOs as its raw scalar ID (`userId: BigInteger`), never as the related entity or a nested object
- **`Response` never echoes back a stored credential/secret/token**: drop columns like a raw/plaintext password, an OAuth `accessToken`/`refreshToken`/`idToken`, a bearer refresh-token `secret`, or a hashed secret (`secretHash`). These stay on the entity and on `Request` (needed to persist them) but never round-trip out through the API. A derived *public* identifier (e.g. an API key's `publicKey`) is fine to keep — the rule is about live credentials, not every string field

### Validation

- `spring-boot-starter-validation` is a project dependency; use `jakarta.validation.constraints.*` on `Request` fields, and put `@Valid` on every `@RequestBody Request` parameter in the controller — the annotations are inert without it
- Map each `Request` field's annotations directly off its migration column, not off assumed business rules:
  - `NOT NULL`, no SQL `DEFAULT`, `VARCHAR(n)` → `@NotBlank` + `@Size(max = n)`
  - `NOT NULL`, no SQL `DEFAULT`, `TEXT` (unbounded) → `@NotBlank` only, no `@Size`
  - `NOT NULL`, no SQL `DEFAULT`, non-`String` type (FK ID, `Instant`, enum, etc.) → `@NotNull`
  - Nullable column → no presence annotation; still add `@Size(max = n)` if it's a bounded `VARCHAR(n)` (only validated when a value is supplied)
  - `NOT NULL` **with** a SQL `DEFAULT` (or an entity-level `@Builder.Default` mirroring one) → no presence annotation at all; treat the field as optional in the request, since omitting it means "use the default" (see the Service section below)
  - Never add a format validator (`@Email`, `@Pattern`, etc.) beyond what the column type itself enforces — don't invent constraints the migration doesn't declare

## Repository

- One repository per domain: `<Name>Repository extends JpaRepository<<Name>Entity, <IdType>>`, annotated `@Repository`
- `<IdType>` is `BigInteger` for a surrogate-PK entity, or the `@EmbeddedId` class for a pure composite-key join table (e.g. `JpaRepository<UserRoleEntity, UserRoleId>`) — a composite-key repository needs no custom finder methods, construct the ID class inline (`new UserRoleId(userId, roleId)`) and call `findById`/`existsById`/`delete` directly
- Add `existsBy<Column>` for every unique constraint the table declares (composite uniqueness → `existsBy<ColA>And<ColB>`) — this backs the duplicate-check in `create` below
- Add `findBy<Column>` only when a lookup by that column is actually needed by a service method, not preemptively

## Exception Handling

- Every domain gets exactly two typed exceptions in its own `<domain>/exception/` package: `<Name>NotFoundException` and `Duplicate<Name>Exception`, both plain `RuntimeException` subclasses with a single `String message` constructor — no shared base class
- Wire both into the single shared `global/handler/GlobalExceptionHandler.java` (`@ControllerAdvice`): a `<Name>NotFoundException` handler → `HttpStatus.NOT_FOUND`, a `Duplicate<Name>Exception` handler → `HttpStatus.CONFLICT`, both returning the shared `global/response/ApiErrorResponse` shape (`status`, `message`, `data`, `client`, `timestamp`, `path`) — copy the existing handler methods rather than inventing a new response shape
- `global/exception/ResourceNotFoundException` is a generic fallback only for a domain that hasn't been given its own typed exception yet — new domains get their own pair from the start

## Service

- Always use interface x implementation strategy
- Name the implementation method by starting with the exact interface's name and add "Impl" at the end
- `@Service` + `@RequiredArgsConstructor` on the impl, injecting the repository (and any other repository needed to resolve a foreign key) via constructor

For example:

```
└── 📁user
    └── service
        UserService.java (Interface) <- Define methods
            └── impl
                UserServiceImpl.java (Class) <- Implement methods
```

### Standard method set

- `create<Name>(Request)`, `get<Name>ById(id)`, `getAll<Name>s()`, `update<Name>(id, Request)`, `delete<Name>(id)` — for a composite-key join table, `id` becomes two parameters (`get<Name>ById(idA, idB)`) instead of one
- **Omit a verb the entity's actual shape doesn't support, rather than implementing it as a no-op.** A pure join table with nothing but its composite key and an immutable `@CreationTimestamp` has no mutable field to `update` — don't add one. A credentials table doesn't need a bulk `getAll` — don't add one just for CRUD-completeness symmetry with other domains. Decide per-entity, don't apply all five verbs mechanically

### `create`

1. Duplicate-check via the repository's `existsBy*`/`existsById` **before** building the entity, throwing `Duplicate<Name>Exception` on a hit. This matters even for a surrogate-PK entity (protects a unique column) and is mandatory for anything with a shared/derived PK, where `JpaRepository.save()` on a non-null `@Id` does a silent `merge` (UPDATE) instead of failing
2. Resolve every foreign key from the request's raw ID via that target's own repository, throwing the target's own `<Target>NotFoundException` on a miss (e.g. `userRepository.findById(request.userId()).orElseThrow(() -> new UserNotFoundException(...))`)
   - **If no repository exists yet for the FK's target domain** (only the entity has been implemented so far), build a bare reference instance instead — `TargetEntity.builder().targetId(request.targetId()).build()` — and set that on the association. Hibernate only needs the target's `@Id` populated to write the FK column. This skips existence validation, so an invalid ID surfaces as a raw DB-level FK-violation 500 instead of a clean 404 — replace it with a real `findById().orElseThrow()` as soon as that domain's repository exists
3. Build the entity via its Lombok builder, setting **associations** (`.user(user)`), never the raw FK field directly
4. For a field with an entity-level `@Builder.Default`, only call the builder setter when the request supplied a non-null value (`if (request.x() != null) builder.x(request.x());`). Never pass a ternary that can resolve to `null` (`request.x() != null ? request.x() : null` is just `request.x()`) — an explicit `null` still overwrites the default
5. `save()` and return

### `getById` / `getAll` / `delete`

- `getById`: `repository.findById(id).orElseThrow(() -> new <Name>NotFoundException(...))`
- `getAll`: plain `repository.findAll()` passthrough (when included — see above)
- `delete`: `repository.delete(get<Name>ById(id))` — reuse the not-found-checked getter, don't call `deleteById` directly

### `update`

- Fetch the existing entity via `get<Name>ById(id)`, then mutate it with setters — full-replace semantics, the caller is expected to send the complete resource
- Re-resolve any FK association exactly like `create` does (repository lookup, or reference-entity if no repository exists yet)
- A `@Builder.Default`-backed optional field gets the same `if (request.x() != null) entity.setX(request.x());` guard as `create` — don't unconditionally overwrite it with `null`
- `save()` and return