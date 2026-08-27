---
title: Repository Writter
description: Repository convention
---

file: Repository Convention

- One repository per domain with @Repsitory annotation

```java
@Repository
<Name>Repsitory extends JpaRepository<<Name>Entity, <IdType>>
```

- `<IdType>` is `BigInteger` or the `@EmbeddedId` class for compsite key

Declare needed default methods:
- findById / existsById
- findBy<Column> / existsBy<Column> when a lookup by that column is actually needed by services