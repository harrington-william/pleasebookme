---
title: service-writter
description: Service convention
---

# Service Convention

- Use interface x implementation strategy
- Name the implementation method by starting with the exact interface's name + "Impl" at the end

## Structure

```text
└── 📁user
    └── service
        UserService.java              <- Define methods
            └── impl
                UserServiceImpl.java  <- Implement methods
```

## Standard Method Set

Create basic CRUD endpoints for every service, including:

### Create a resource

### Get a resource

### Update a resource

### Delete a resource