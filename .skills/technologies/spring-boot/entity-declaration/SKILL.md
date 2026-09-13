---
name: entity-writter
description: Entity convention
---

# Entity Declaration Convention

## Entity name

Entities must be named with the word "Entity" at the end

For example: UserEntity, RoleEntity, BookingEntity

## Annotation order

Place annotation in this order

```java
@Entity
@Table(
    schema = "",
    name = ""
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
```

## Column order

- Columns order MUST match exactly to that table in Flyway migration definition.
- Always place createdAt and updatedAt at the end of the entity. If these 2 columns are not placed at the end in SQL script, shift them to the end in the entity.

## Columns Convention

- The ID field in an entity must be named by starting with the entity name.
- ID columns must be started with the entity name, such as userId, userUid. Apply it for every ID and UID column.

ID Format:

```java
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private BigInteger userId;
}
```

```java
@UuidGenerator
    @Column(name = "uid", nullable = false, unique = true, updatable = false)
    private UUID userUid;
```

Metadata Format:

```java
@JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata")
    private JsonNode metadata;
```

Timestampt Format:

```java
@CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
```