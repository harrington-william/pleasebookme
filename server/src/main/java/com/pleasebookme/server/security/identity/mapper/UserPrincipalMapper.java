package com.pleasebookme.server.security.identity.mapper;

import com.pleasebookme.server.auth.permission.entity.PermissionEntity;
import com.pleasebookme.server.auth.role.entity.RoleEntity;
import com.pleasebookme.server.security.identity.aggregation.AuthenticationAggregation;
import com.pleasebookme.server.security.identity.enums.AuthenticatedActorType;
import com.pleasebookme.server.security.identity.principal.AuthenticatedPrincipal;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;


@Component
public class UserPrincipalMapper implements PrincipalMapper<AuthenticationAggregation> {

    @Override
    public AuthenticatedPrincipal map(AuthenticationAggregation aggregation) {

        return new AuthenticatedPrincipal(
            AuthenticatedActorType.USER,

            aggregation.user().getUserId(),
            aggregation.tenant().getTenantId(),
            aggregation.organization().getOrganizationId(),
            aggregation.membership().getMembershipId(),
            aggregation.profile().getProfileId(),

            aggregation.user().getUsername(),
            aggregation.user().getEmail(),

            aggregation.user().getName(),
            aggregation.user().getLocale(),
            aggregation.user().getTimezone(),

            aggregation.user().getAccountStatus(),

            aggregation.roles()
                .stream()
                .map(RoleEntity::getName)
                .collect(Collectors.toUnmodifiableSet()),

            aggregation.permissions()
                .stream()
                .map(PermissionEntity::getSlug)
                .collect(Collectors.toUnmodifiableSet()),

            Map.of()
        );
    }
}
