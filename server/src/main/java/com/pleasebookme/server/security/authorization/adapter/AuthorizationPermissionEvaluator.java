package com.pleasebookme.server.security.authorization.adapter;

import com.pleasebookme.server.security.authorization.context.AuthorizationContext;
import com.pleasebookme.server.security.authorization.decision.AuthorizationDecision;
import com.pleasebookme.server.security.authorization.scope.ResourceScope;
import com.pleasebookme.server.security.authorization.service.AuthorizationService;
import com.pleasebookme.server.security.identity.context.CurrentPrincipalProvider;
import com.pleasebookme.server.security.identity.principal.AuthenticatedPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthorizationPermissionEvaluator implements PermissionEvaluator {

    private final AuthorizationService authorizationService;
    private final CurrentPrincipalProvider currentPrincipalProvider;

    @Override
    public boolean hasPermission(
        Authentication authentication,
        Object targetDomainObject,
        Object permission
    ) {
        Optional<AuthenticatedPrincipal> principal = currentPrincipalProvider.find();
        if (principal.isEmpty()) {
            return false;
        }

        String[] slug = splitSlug(permission);
        if (slug == null) {
            return false;
        }

        return decide(
            principal.get(),
            slug[0],
            slug[1],
            targetDomainObject
        );
    }

    @Override
    public boolean hasPermission(
        Authentication authentication,
        Serializable targetId,
        String targetType,
        Object permission
    ) {
        Optional<AuthenticatedPrincipal> principal = currentPrincipalProvider.find();

        if (principal.isEmpty() || targetType == null || permission == null) {
            return false;
        }

        return decide(
            principal.get(),
            targetType,
            permission.toString(),
            targetId
        );
    }

    // Build authorization context and call appropriate authorization service
    private boolean decide(
        AuthenticatedPrincipal principal,
        String resourceType,
        String action,
        Object resource
    ) {
        AuthorizationContext context = new AuthorizationContext(
            principal,
            resourceType,
            action,
            resource,
            ResourceScope.unscoped(),
            null
        );

        AuthorizationDecision decision = authorizationService.authorize(context);

        return decision.granted();
    }

    private String[] splitSlug(Object permission) {
        if (!(permission instanceof String value) || value.isBlank()) {
            return null;
        }

        int separator = value.indexOf('.');
        if (separator <= 0 || separator == value.length() - 1) {
            return null;
        }

        return new String[] {
            value.substring(0, separator),
            value.substring(separator + 1)
        };
    }
}
