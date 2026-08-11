package com.pleasebookme.server.service.auth.service.impl;

import com.pleasebookme.server.auth.enums.AccountStatus;
import com.pleasebookme.server.auth.role.entity.RoleEntity;
import com.pleasebookme.server.auth.role.exception.RoleNotFoundException;
import com.pleasebookme.server.auth.role.repository.RoleRepository;
import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import com.pleasebookme.server.auth.userrole.entity.UserRoleEntity;
import com.pleasebookme.server.auth.userrole.repository.UserRoleRepository;
import com.pleasebookme.server.global.enums.Locale;
import com.pleasebookme.server.global.enums.Theme;
import com.pleasebookme.server.global.enums.WeekStart;
import com.pleasebookme.server.organization.membership.entity.MembershipEntity;
import com.pleasebookme.server.organization.membership.repository.MembershipRepository;
import com.pleasebookme.server.organization.organizations.entity.OrganizationEntity;
import com.pleasebookme.server.organization.organizations.repository.OrganizationRepository;
import com.pleasebookme.server.organization.profile.entity.ProfileEntity;
import com.pleasebookme.server.organization.profile.repository.ProfileRepository;
import com.pleasebookme.server.service.auth.service.UserProvisioningService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProvisioningServiceImpl implements UserProvisioningService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final OrganizationRepository organizationRepository;
    private final MembershipRepository membershipRepository;
    private final ProfileRepository profileRepository;

    @Override
    public UserEntity provisionUser(
        String username,
        String name,
        String email,
        String phone,
        Locale locale,
        String timezone
    ) {
        Instant now = Instant.now();
        String resolvedTimezone = timezone != null ? timezone : "Australia/Sydney";

        UserEntity user = UserEntity.builder()
            .userUid(UUID.randomUUID())
            .username(username)
            .name(name)
            .email(email)
            .phone(phone)
            .locale(locale != null ? locale : Locale.en)
            .timezone(resolvedTimezone)
            .theme(Theme.DARK)
            .weekStart(WeekStart.MONDAY)
            .accountStatus(AccountStatus.ACTIVE)
            .createdAt(now)
            .updatedAt(now)
            .build();
        userRepository.save(user);

        RoleEntity userRole = roleRepository.findByName("USER")
            .orElseThrow(() -> new RoleNotFoundException("Role not found: USER"));

        UserRoleEntity userRoleEntity = UserRoleEntity.builder()
            .user(user)
            .role(userRole)
            .build();
        userRoleRepository.save(userRoleEntity);

        String organizationName = name + "'s Organization";
        String organizationSlug = name
            .toLowerCase()
            .replace(" ", "") +
            "-organization";

        if (organizationRepository.existsBySlug(organizationSlug)) {
            organizationSlug = user.getUserUid().toString();
        }

        OrganizationEntity organization = OrganizationEntity.builder()
            .name(organizationName)
            .slug(organizationSlug)
            .isPrivate(true)
            .timezone(user.getTimezone())
            .build();
        organizationRepository.save(organization);

        MembershipEntity membership = MembershipEntity.builder()
            .organization(organization)
            .user(user)
            .accepted(true)
            .build();
        membershipRepository.save(membership);

        ProfileEntity profile = ProfileEntity.builder()
            .user(user)
            .organization(organization)
            .username(username)
            .build();
        profileRepository.save(profile);

        return user;
    }
}
