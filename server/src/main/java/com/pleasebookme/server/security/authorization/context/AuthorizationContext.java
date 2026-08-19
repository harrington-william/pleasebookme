package com.pleasebookme.server.security.authorization.context;

import com.pleasebookme.server.security.authorization.membership.MembershipSnapshot;
import com.pleasebookme.server.security.authorization.scope.ResourceScope;
import com.pleasebookme.server.security.identity.principal.AuthenticatedPrincipal;

import java.util.Map;
import java.util.Optional;

public record AuthorizationContext(
    AuthenticatedPrincipal principal,
    String resourceType,
    String action,
    Object resource,
    ResourceScope scope,
    MembershipSnapshot membership,
    Map<String, Object> attributes
) {
    public AuthorizationContext {
        if (principal == null) {
            throw new IllegalArgumentException("Principal is required");
        }

        if (resourceType == null || resourceType.isBlank()) {
            throw new IllegalArgumentException("Resource type is required");
        }

        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("Action is required");
        }

        scope = scope == null ? ResourceScope.unscoped() : scope;
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    public static AuthorizationContext forCreate(
        AuthenticatedPrincipal principal,
        String resourceType,
        ResourceScope scope,
        MembershipSnapshot membership
    ) {
        return new AuthorizationContext(
            principal,
            resourceType,
            "CREATE",
            null,
            scope,
            membership,
            null
        );
    }

    public static AuthorizationContext forResource(
        AuthenticatedPrincipal principal,
        String resourceType,
        String action,
        Object resource,
        ResourceScope scope,
        MembershipSnapshot membership
    ) {
        return new AuthorizationContext(
            principal,
            resourceType,
            action,
            resource,
            scope,
            membership,
            null
        );
    }

    public String permissionSlug() {
        return resourceType + "." + action;
    }

    public boolean hasMembership() {
        return membership != null;
    }

    public Optional<Object> attribute(String key) {
        return Optional.ofNullable(attributes.get(key));
    }

    public <T> Optional<T> resourceAs(Class<T> type) {
        return type.isInstance(resource) ? Optional.of(type.cast(resource)) : Optional.empty();
    }
}
