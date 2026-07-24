package com.pleasebookme.server.security.identity.loader;

import com.pleasebookme.server.auth.permission.entity.PermissionEntity;
import com.pleasebookme.server.auth.role.entity.RoleEntity;
import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.user.exception.UserNotFoundException;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import com.pleasebookme.server.organization.membership.entity.MembershipEntity;
import com.pleasebookme.server.organization.membership.exception.MembershipNotFoundException;
import com.pleasebookme.server.organization.membership.repository.MembershipRepository;
import com.pleasebookme.server.organization.organizations.entity.OrganizationEntity;
import com.pleasebookme.server.organization.profile.entity.ProfileEntity;
import com.pleasebookme.server.organization.profile.exception.ProfileNotFoundException;
import com.pleasebookme.server.organization.profile.repository.ProfileRepository;
import com.pleasebookme.server.security.identity.aggregation.AuthenticationAggregation;
import com.pleasebookme.server.tenant.tenants.entity.TenantEntity;
import com.pleasebookme.server.tenant.tenants.exception.TenantNotFoundException;
import com.pleasebookme.server.tenant.tenants.repository.TenantRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DefaultIdentityLoader implements IdentityLoader {
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    private final ProfileRepository profileRepository;
    private final TenantRepository tenantRepository;

    @Override
    public AuthenticationAggregation loadByUsername(String username) {
        UserEntity user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException(
                "User not found with username: " + username
            ));

        return build(user);
    }

    @Override
    public AuthenticationAggregation loadByEmail(String email) {
        UserEntity user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException(
                "User not found with username: " + email
            ));

        return build(user);
    }

    @Override
    public AuthenticationAggregation loadByUid(UUID uid) {
        UserEntity user = userRepository.findByUserUid(uid)
            .orElseThrow(() -> new UserNotFoundException(
                "User not found with username: " + uid
            ));

        return build(user);
    }

    private AuthenticationAggregation build(UserEntity user) {
        MembershipEntity membership = membershipRepository
            .findByUserUserId(user.getUserId())
            .orElseThrow(() -> new MembershipNotFoundException(
                "Membership not found for user: " + user.getUserId()
            ));

        ProfileEntity profile = profileRepository
            .findByUserUserId(user.getUserId())
            .orElseThrow(() -> new ProfileNotFoundException(
                "Profile not found for user: " + user.getUserId()
            ));

        OrganizationEntity organization = membership.getOrganization();

        TenantEntity tenant = tenantRepository
            .findByOrganizationOrganizationId(organization.getOrganizationId())
            .orElseThrow(() -> new TenantNotFoundException(
                "Tenant not found for user: " + user.getUserId()
            ));

        // Handle roles
        Set<RoleEntity> roles = Set.copyOf(user.getRoles());

        // Handle permissions
        Set<PermissionEntity> permissions = roles
            .stream()
            .flatMap(
                role -> role.getPermissions().stream()
            )
            .collect(Collectors.toUnmodifiableSet());

        return new AuthenticationAggregation(
            user,
            membership,
            organization,
            tenant,
            profile,
            roles,
            permissions
        );
    }
}
