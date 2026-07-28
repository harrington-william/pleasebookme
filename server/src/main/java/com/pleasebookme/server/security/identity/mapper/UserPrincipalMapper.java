package com.pleasebookme.server.security.identity.mapper;

import com.pleasebookme.server.auth.permission.entity.PermissionEntity;
import com.pleasebookme.server.auth.role.entity.RoleEntity;
import com.pleasebookme.server.security.identity.aggregation.AuthenticationAggregation;
import com.pleasebookme.server.security.identity.enums.AuthenticatedActorType;
import com.pleasebookme.server.security.identity.principal.UserPrincipal;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;


@Component
public class UserPrincipalMapper implements PrincipalMapper<AuthenticationAggregation> {

    @Override
    public UserPrincipal map(AuthenticationAggregation aggregation) {

        return new UserPrincipal(
            AuthenticatedActorType.USER,

            aggregation.user().getUserUid(),
            aggregation.tenant().getTenantUid(),
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
