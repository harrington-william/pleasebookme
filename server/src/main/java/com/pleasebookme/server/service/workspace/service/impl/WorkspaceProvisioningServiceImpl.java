package com.pleasebookme.server.service.workspace.service.impl;

import com.pleasebookme.server.auth.enums.AccountStatus;
import com.pleasebookme.server.auth.role.entity.RoleEntity;
import com.pleasebookme.server.auth.role.exception.RoleNotFoundException;
import com.pleasebookme.server.auth.role.repository.RoleRepository;
import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import com.pleasebookme.server.auth.userrole.entity.UserRoleEntity;
import com.pleasebookme.server.auth.userrole.repository.UserRoleRepository;
import com.pleasebookme.server.core.availability.entity.AvailabilityEntity;
import com.pleasebookme.server.core.availability.repository.AvailabilityRepository;
import com.pleasebookme.server.core.bookingpolicy.entity.BookingPolicyEntity;
import com.pleasebookme.server.core.bookingpolicy.repository.BookingPolicyRepository;
import com.pleasebookme.server.core.schedule.entity.ScheduleEntity;
import com.pleasebookme.server.core.schedule.repository.ScheduleRepository;
import com.pleasebookme.server.core.service.entity.ServiceEntity;
import com.pleasebookme.server.core.service.repository.ServiceRepository;
import com.pleasebookme.server.global.enums.Locale;
import com.pleasebookme.server.global.enums.Theme;
import com.pleasebookme.server.global.enums.WeekStart;
import com.pleasebookme.server.organization.membership.entity.MembershipEntity;
import com.pleasebookme.server.organization.membership.repository.MembershipRepository;
import com.pleasebookme.server.organization.membershiprole.entity.MembershipRoleEntity;
import com.pleasebookme.server.organization.membershiprole.repository.MembershipRoleRepository;
import com.pleasebookme.server.organization.organizations.entity.OrganizationEntity;
import com.pleasebookme.server.organization.organizations.repository.OrganizationRepository;
import com.pleasebookme.server.organization.profile.entity.ProfileEntity;
import com.pleasebookme.server.organization.profile.repository.ProfileRepository;
import com.pleasebookme.server.service.workspace.dto.WorkspaceProvisionRequest;
import com.pleasebookme.server.service.workspace.service.WorkspaceProvisioningService;
import com.pleasebookme.server.tenant.ecosystem.entity.EcosystemEntity;
import com.pleasebookme.server.tenant.ecosystem.exception.EcosystemNotFoundException;
import com.pleasebookme.server.tenant.ecosystem.repository.EcosystemRepository;
import com.pleasebookme.server.tenant.enums.TenantRegion;
import com.pleasebookme.server.tenant.enums.TenantStatus;
import com.pleasebookme.server.tenant.plan.entity.TenantPlanEntity;
import com.pleasebookme.server.tenant.plan.exception.TenantPlanNotFoundException;
import com.pleasebookme.server.tenant.plan.repository.TenantPlanRepository;
import com.pleasebookme.server.tenant.tenants.entity.TenantEntity;
import com.pleasebookme.server.tenant.tenants.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkspaceProvisioningServiceImpl implements WorkspaceProvisioningService {
    private static final String DEFAULT_TIMEZONE = "Australia/Sydney";
    private static final String PLATFORM_ROLE = "USER";
    private static final String ORGANIZATION_OWNER_ROLE = "ORGANIZATION_OWNER";
    private static final String DEFAULT_PLAN_CODE = "FREE";
    private static final String DEFAULT_ECOSYSTEM_CODE = "GENERAL";

    private static final String DEFAULT_SCHEDULE_TITLE = "Working Hours";
    private static final Integer[] WEEKDAYS = {1, 2, 3, 4, 5};
    private static final LocalTime WORK_DAY_START = LocalTime.of(9, 0);
    private static final LocalTime WORK_DAY_END = LocalTime.of(17, 0);

    private static final String DEFAULT_SERVICE_TITLE = "Consultant Meeting";
    private static final String DEFAULT_SERVICE_SLUG = "consultant-meeting";
    private static final int DEFAULT_DURATION_MINUTES = 30;
    private static final int DEFAULT_MINIMUM_NOTICE_MINUTES = 120;
    private static final int DEFAULT_MAXIMUM_ADVANCE_BOOKING_MINUTES = 30 * 24 * 60;
    private static final String DEFAULT_BOOKING_WINDOW_TYPE = "ROLLING";
    private static final int DEFAULT_CAPACITY = 1;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final OrganizationRepository organizationRepository;
    private final MembershipRepository membershipRepository;
    private final MembershipRoleRepository membershipRoleRepository;
    private final ProfileRepository profileRepository;
    private final TenantRepository tenantRepository;
    private final EcosystemRepository ecosystemRepository;
    private final TenantPlanRepository tenantPlanRepository;
    private final ScheduleRepository scheduleRepository;
    private final AvailabilityRepository availabilityRepository;
    private final ServiceRepository serviceRepository;
    private final BookingPolicyRepository bookingPolicyRepository;

    @Override
    @Transactional
    public UserEntity provision(WorkspaceProvisionRequest request) {
        UserEntity user = createUser(
            request.username(),
            request.name(),
            request.email(),
            request.phone(),
            request.locale(),
            request.timezone()
        );
        assignPlatformRole(user);

        OrganizationEntity organization = createOrganization(user, request.name());
        MembershipEntity membership = createMembership(user, organization);
        assignOrganizationOwnerRole(membership);
        ProfileEntity profile = createProfile(user, organization, request.username());
        createTenant(user, organization);

        ScheduleEntity schedule = createSchedule(user);
        createAvailability(user, schedule);
        ServiceEntity service = createDefaultService(user, profile, organization, schedule);
        createDefaultBookingPolicy(service);

        return user;
    }

    private UserEntity createUser(
        String username,
        String name,
        String email,
        String phone,
        Locale locale,
        String timezone
    ) {
        Instant now = Instant.now();

        UserEntity user = UserEntity.builder()
            .userUid(UUID.randomUUID())
            .username(username)
            .name(name)
            .email(email)
            .phone(phone)
            .locale(locale != null ? locale : Locale.en)
            .timezone(timezone != null ? timezone : DEFAULT_TIMEZONE)
            .theme(Theme.DARK)
            .weekStart(WeekStart.MONDAY)
            .accountStatus(AccountStatus.ACTIVE)
            .createdAt(now)
            .updatedAt(now)
            .build();
        userRepository.save(user);
        return user;
    }

    private void assignPlatformRole(UserEntity user) {
        UserRoleEntity userRole = UserRoleEntity.builder()
            .user(user)
            .role(requireRole(PLATFORM_ROLE))
            .build();
        userRoleRepository.save(userRole);
    }

    private OrganizationEntity createOrganization(UserEntity user, String name) {
        String organizationSlug = name
            .toLowerCase()
            .replace(" ", "") +
            "-organization";

        if (organizationRepository.existsBySlug(organizationSlug)) {
            organizationSlug = user.getUserUid().toString();
        }

        OrganizationEntity organization = OrganizationEntity.builder()
            .name(name + "'s Organization")
            .slug(organizationSlug)
            .isPrivate(true)
            .timezone(user.getTimezone())
            .build();
        organizationRepository.save(organization);
        return organization;
    }

    private MembershipEntity createMembership(UserEntity user, OrganizationEntity organization) {
        MembershipEntity membership = MembershipEntity.builder()
            .organization(organization)
            .user(user)
            .accepted(true)
            .build();
        membershipRepository.save(membership);
        return membership;
    }

    private void assignOrganizationOwnerRole(MembershipEntity membership) {
        MembershipRoleEntity membershipRole = MembershipRoleEntity.builder()
            .membership(membership)
            .role(requireRole(ORGANIZATION_OWNER_ROLE))
            .build();
        membershipRoleRepository.save(membershipRole);
    }

    private ProfileEntity createProfile(UserEntity user, OrganizationEntity organization, String username) {
        ProfileEntity profile = ProfileEntity.builder()
            .user(user)
            .organization(organization)
            .username(username)
            .build();
        profileRepository.save(profile);
        return profile;
    }

    private void createTenant(UserEntity user, OrganizationEntity organization) {
        EcosystemEntity ecosystem = ecosystemRepository.findByCode(DEFAULT_ECOSYSTEM_CODE)
            .orElseThrow(() -> new EcosystemNotFoundException(
                "Ecosystem not found: " + DEFAULT_ECOSYSTEM_CODE
            ));
        TenantPlanEntity plan = tenantPlanRepository.findByCode(DEFAULT_PLAN_CODE)
            .orElseThrow(() -> new TenantPlanNotFoundException(
                "Tenant plan not found: " + DEFAULT_PLAN_CODE
            ));

        if (plan.getMaxUsers() == null || plan.getMaxServices() == null || plan.getMaxWidgets() == null) {
            throw new IllegalStateException("Plan FREE is missing quota limits; apply V139");
        }

        String tenantSlug = organization.getSlug();
        if (tenantRepository.existsBySlug(tenantSlug)) {
            tenantSlug = user.getUserUid().toString();
        }

        TenantEntity tenant = TenantEntity.builder()
            .organization(organization)
            .ownerUser(user)
            .ecosystem(ecosystem)
            .name(organization.getName())
            .slug(tenantSlug)
            .status(TenantStatus.ACTIVE)
            .plan(plan)
            .region(resolveRegion(user.getTimezone()))
            .defaultTimezone(user.getTimezone())
            .defaultLocale(user.getLocale())
            .maxUsers(plan.getMaxUsers())
            .maxServices(plan.getMaxServices())
            .maxWidgets(plan.getMaxWidgets())
            .build();
        tenantRepository.save(tenant);
    }

    private ScheduleEntity createSchedule(UserEntity user) {
        ScheduleEntity schedule = ScheduleEntity.builder()
            .user(user)
            .title(DEFAULT_SCHEDULE_TITLE)
            .timezone(user.getTimezone())
            .build();
        scheduleRepository.save(schedule);
        return schedule;
    }

    private void createAvailability(UserEntity user, ScheduleEntity schedule) {
        AvailabilityEntity availability = AvailabilityEntity.builder()
            .user(user)
            .schedule(schedule)
            .days(WEEKDAYS)
            .startTime(WORK_DAY_START)
            .endTime(WORK_DAY_END)
            .build();
        availabilityRepository.save(availability);
    }

    private ServiceEntity createDefaultService(
        UserEntity user,
        ProfileEntity profile,
        OrganizationEntity organization,
        ScheduleEntity schedule
    ) {
        ServiceEntity service = ServiceEntity.builder()
            .title(DEFAULT_SERVICE_TITLE)
            .slug(DEFAULT_SERVICE_SLUG)
            .interfaceLanguage(user.getLocale())
            .user(user)
            .profile(profile)
            .organization(organization)
            .schedule(schedule)
            .timezone(user.getTimezone())
            .build();
        serviceRepository.save(service);
        return service;
    }

    private void createDefaultBookingPolicy(ServiceEntity service) {
        BookingPolicyEntity bookingPolicy = BookingPolicyEntity.builder()
            .service(service)
            .defaultDuration(DEFAULT_DURATION_MINUTES)
            .minimumNotice(DEFAULT_MINIMUM_NOTICE_MINUTES)
            .maximumAdvanceBooking(DEFAULT_MAXIMUM_ADVANCE_BOOKING_MINUTES)
            .bookingWindowType(DEFAULT_BOOKING_WINDOW_TYPE)
            .capacity(DEFAULT_CAPACITY)
            .build();
        bookingPolicyRepository.save(bookingPolicy);
    }

    private RoleEntity requireRole(String name) {
        return roleRepository.findByName(name)
            .orElseThrow(() -> new RoleNotFoundException("Role not found: " + name));
    }

    private TenantRegion resolveRegion(String timezone) {
        if (timezone.startsWith("Australia/")) return TenantRegion.AU;
        if (timezone.equals("Europe/London")) return TenantRegion.UK;
        if (timezone.startsWith("America/")) return TenantRegion.US;
        if (timezone.equals("Asia/Singapore")) return TenantRegion.SG;

        // Map the launch market region
        return TenantRegion.VN;
    }
}
