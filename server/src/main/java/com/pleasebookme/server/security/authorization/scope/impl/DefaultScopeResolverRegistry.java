package com.pleasebookme.server.security.authorization.scope.impl;

import com.pleasebookme.server.security.authorization.scope.ResourceScope;
import com.pleasebookme.server.security.authorization.scope.ScopeResolver;
import com.pleasebookme.server.security.authorization.scope.ScopeResolverRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DefaultScopeResolverRegistry implements ScopeResolverRegistry {

    private final List<ScopeResolver<?>> resolvers;

    @PostConstruct
    void validateNoDuplicateResourceTypes() {
        Set<Class<?>> seen = new HashSet<>();

        for (ScopeResolver<?> resolver : resolvers) {
            if (!seen.add(resolver.resourceType())) {
                throw new IllegalStateException(
                    "Duplicate ScopeResolver registered for resource type " + resolver.resourceType().getName()
                );
            }
        }
    }

    @Override
    public ResourceScope resolve(Object resource) {
        if (resource == null) {
            return ResourceScope.unscoped();
        }

        for (ScopeResolver<?> resolver : resolvers) {
            if (resolver.resourceType().isInstance(resource)) {
                return resolveTyped(resolver, resource);
            }
        }

        return ResourceScope.unscoped();
    }

    @SuppressWarnings("unchecked")
    private <T> ResourceScope resolveTyped(ScopeResolver<T> resolver, Object resource) {
        return resolver.resolve((T) resource);
    }
}
