---
title: table-creator
description: Table-level convention
---


# Table-level Convention

## PRIMARY KEY

- Every ID PRIMARY KEY columns are BIG SERIAL auto increment. Some special tables like core.bookings has uid as a dedicated unique identity becase they may used by external tools.

## Foreign Keys

Format:

```sql
CONSTRAINT fk_role_permission_role
        FOREIGN KEY(role_id)
        REFERENCES auth.roles(id)
        ON DELETE CASCADE,

CONSTRAINT fk_role_permission_permission
    FOREIGN KEY(permission_id)
    REFERENCES auth.permissions(id)
    ON DELETE CASCADE
```

Prohibited Formats:

```sql
/* Inline */
user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE
```

## Indexes

Format:

```sql
CREATE INDEX idx_schedules_user_id
ON core.schedules(user_id);
```