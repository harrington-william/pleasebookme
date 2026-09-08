---
name: method-declarator
description: Method convention
---

# Method Declaration Convention

## 1 parameter

For methods only have 1 parameter, use this format:

```java
public UserResponse login(Parameter parameter) {
    return;
}
```

## 2 or more parameters

For methods have more than 1 parameter, use this format:

```java
public UserResponse login(
    Parameter1 parameter1
    Parameter2 parameter2
) {
    return;
}
```