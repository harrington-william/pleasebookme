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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class IdentityLoader {
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    private final ProfileRepository profileRepository;
    private final TenantRepository tenantRepository;

    // Temporarily load by username
    // Then add load by email, load by phone methods

    /*
    Strategy:
    Build a private load() method, then implement loadByEmail, loadByPhone method
     */
    public AuthenticationAggregation load(String username) {
        UserEntity user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException(
                "User not found with username: " + username
            ));

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
            .flatMap(role -> role.getPermissions().stream())
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
