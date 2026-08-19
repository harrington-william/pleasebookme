package com.pleasebookme.server.security.authorization.membership.impl;

import com.pleasebookme.server.auth.permission.entity.PermissionEntity;
import com.pleasebookme.server.auth.role.entity.RoleEntity;
import com.pleasebookme.server.organization.membership.entity.MembershipEntity;
import com.pleasebookme.server.organization.membership.repository.MembershipRepository;
import com.pleasebookme.server.organization.membershiprole.entity.MembershipRoleEntity;
import com.pleasebookme.server.organization.membershiprole.repository.MembershipRoleRepository;
import com.pleasebookme.server.organization.organizations.entity.OrganizationEntity;
import com.pleasebookme.server.security.authorization.membership.MembershipSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultMembershipResolverTest {

    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private MembershipRoleRepository membershipRoleRepository;

    private DefaultMembershipResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new DefaultMembershipResolver(membershipRepository, membershipRoleRepository);
    }

    @Test
    void resolve_returnsEmpty_whenUserUidIsNull() {
        assertThat(resolver.resolve(null, BigInteger.ONE)).isEmpty();
        verifyNoInteractions(membershipRepository);
    }

    @Test
    void resolve_returnsEmpty_whenOrganizationIdIsNull() {
        assertThat(resolver.resolve(UUID.randomUUID(), null)).isEmpty();
        verifyNoInteractions(membershipRepository);
    }

    @Test
    void resolve_returnsEmpty_whenNoAcceptedMembershipExistsForThatOrganization() {
        UUID userUid = UUID.randomUUID();
        BigInteger organizationId = BigInteger.valueOf(7);

        when(membershipRepository.findByUserUserUidAndOrganizationOrganizationIdAndAccepted(
            userUid, organizationId, true
        )).thenReturn(Optional.empty());

        assertThat(resolver.resolve(userUid, organizationId)).isEmpty();
    }

    @Test
    void resolve_flattensRolesAndPermissions_fromTheMembershipsRoleAssignments() {
        UUID userUid = UUID.randomUUID();
        BigInteger organizationId = BigInteger.valueOf(7);

        OrganizationEntity organization = OrganizationEntity.builder()
            .organizationId(organizationId)
            .build();

        MembershipEntity membership = MembershipEntity.builder()
            .membershipId(BigInteger.valueOf(99))
            .organization(organization)
            .build();

        PermissionEntity readPermission = PermissionEntity.builder().slug("BOOKING.READ").build();
        PermissionEntity updatePermission = PermissionEntity.builder().slug("BOOKING.UPDATE").build();

        RoleEntity staffRole = RoleEntity.builder()
            .name("STAFF")
            .permissions(Set.of(readPermission, updatePermission))
            .build();

        MembershipRoleEntity membershipRole = MembershipRoleEntity.builder()
            .membership(membership)
            .role(staffRole)
            .build();

        when(membershipRepository.findByUserUserUidAndOrganizationOrganizationIdAndAccepted(
            userUid, organizationId, true
        )).thenReturn(Optional.of(membership));

        when(membershipRoleRepository.findByMembershipMembershipId(membership.getMembershipId()))
            .thenReturn(List.of(membershipRole));

        Optional<MembershipSnapshot> snapshot = resolver.resolve(userUid, organizationId);

        assertThat(snapshot).isPresent();
        assertThat(snapshot.get().membershipId()).isEqualTo(BigInteger.valueOf(99));
        assertThat(snapshot.get().organizationId()).isEqualTo(organizationId);
        assertThat(snapshot.get().roles()).containsExactly("STAFF");
        assertThat(snapshot.get().permissions()).containsExactlyInAnyOrder("BOOKING.READ", "BOOKING.UPDATE");
        assertThat(snapshot.get().hasRole("STAFF")).isTrue();
        assertThat(snapshot.get().hasPermission("BOOKING.READ")).isTrue();
        assertThat(snapshot.get().hasPermission("BOOKING.DELETE")).isFalse();
    }

    @Test
    void resolve_dedupesAPermissionGrantedByMoreThanOneRoleOnTheSameMembership() {
        UUID userUid = UUID.randomUUID();
        BigInteger organizationId = BigInteger.valueOf(7);

        OrganizationEntity organization = OrganizationEntity.builder()
            .organizationId(organizationId)
            .build();

        MembershipEntity membership = MembershipEntity.builder()
            .membershipId(BigInteger.valueOf(99))
            .organization(organization)
            .build();

        PermissionEntity shared = PermissionEntity.builder().slug("BOOKING.READ").build();

        RoleEntity staffRole = RoleEntity.builder().name("STAFF").permissions(Set.of(shared)).build();
        RoleEntity managerRole = RoleEntity.builder()
            .name("ORGANIZATION_MANAGER")
            .permissions(Set.of(shared))
            .build();

        MembershipRoleEntity staffAssignment = MembershipRoleEntity.builder()
            .membership(membership)
            .role(staffRole)
            .build();

        MembershipRoleEntity managerAssignment = MembershipRoleEntity.builder()
            .membership(membership)
            .role(managerRole)
            .build();

        when(membershipRepository.findByUserUserUidAndOrganizationOrganizationIdAndAccepted(
            userUid, organizationId, true
        )).thenReturn(Optional.of(membership));

        when(membershipRoleRepository.findByMembershipMembershipId(membership.getMembershipId()))
            .thenReturn(List.of(staffAssignment, managerAssignment));

        Optional<MembershipSnapshot> snapshot = resolver.resolve(userUid, organizationId);

        assertThat(snapshot).isPresent();
        assertThat(snapshot.get().roles()).containsExactlyInAnyOrder("STAFF", "ORGANIZATION_MANAGER");
        assertThat(snapshot.get().permissions()).containsExactly("BOOKING.READ");
    }
}
