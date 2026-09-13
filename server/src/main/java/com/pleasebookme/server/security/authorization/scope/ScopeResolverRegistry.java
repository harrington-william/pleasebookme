package com.pleasebookme.server.security.authorization.scope;

public interface ScopeResolverRegistry {
    ResourceScope resolve(Object resource);
}
