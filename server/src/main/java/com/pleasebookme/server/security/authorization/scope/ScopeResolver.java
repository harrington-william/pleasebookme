package com.pleasebookme.server.security.authorization.scope;

public interface ScopeResolver<T> {
    Class<T> resourceType();

    ResourceScope resolve(T resource);
}
