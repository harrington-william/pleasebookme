package com.pleasebookme.server.security.authorization.membership.impl;

import com.pleasebookme.server.auth.permission.entity.PermissionEntity;
import com.pleasebookme.server.auth.role.entity.RoleEntity;
import com.pleasebookme.server.organization.membership.entity.MembershipEntity;
import com.pleasebookme.server.organization.membership.repository.MembershipRepository;
import com.pleasebookme.server.organization.membershiprole.entity.MembershipRoleEntity;
import com.pleasebookme.server.organization.membershiprole.repository.MembershipRoleRepository;
import com.pleasebookme.server.security.authorization.membership.MembershipResolver;
import com.pleasebookme.server.security.authorization.membership.MembershipSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DefaultMembershipResolver implements MembershipResolver {

    private final MembershipRepository membershipRepository;
    private final MembershipRoleRepository membershipRoleRepository;

    @Override
    public Optional<MembershipSnapshot> resolve(UUID userUid, BigInteger organizationId) {
        if (userUid == null || organizationId == null) {
            return Optional.empty();
        }

        return membershipRepository
            .findByUserUserUidAndOrganizationOrganizationIdAndAccepted(userUid, organizationId, true)
            .map(this::toSnapshot);
    }

    private MembershipSnapshot toSnapshot(MembershipEntity membership) {
        List<MembershipRoleEntity> membershipRoles = membershipRoleRepository
            .findByMembershipMembershipId(membership.getMembershipId());

        Set<RoleEntity> roles = membershipRoles.stream()
            .map(MembershipRoleEntity::getRole)
            .collect(Collectors.toUnmodifiableSet());

        Set<String> roleNames = roles.stream()
            .map(RoleEntity::getName)
            .collect(Collectors.toUnmodifiableSet());

        Set<String> permissionSlugs = roles.stream()
            .flatMap(role -> role.getPermissions().stream())
            .map(PermissionEntity::getSlug)
            .collect(Collectors.toUnmodifiableSet());

        return new MembershipSnapshot(
            membership.getMembershipId(),
            membership.getOrganization().getOrganizationId(),
            roleNames,
            permissionSlugs
        );
    }
}
