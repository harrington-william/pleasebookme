package com.pleasebookme.server.service.workspace.service;

import com.pleasebookme.server.auth.role.entity.RoleEntity;
import com.pleasebookme.server.auth.role.exception.RoleNotFoundException;
import com.pleasebookme.server.auth.role.repository.RoleRepository;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import com.pleasebookme.server.auth.userrole.repository.UserRoleRepository;
import com.pleasebookme.server.core.availability.entity.AvailabilityEntity;
import com.pleasebookme.server.core.availability.repository.AvailabilityRepository;
import com.pleasebookme.server.core.bookingpolicy.entity.BookingPolicyEntity;
import com.pleasebookme.server.core.bookingpolicy.repository.BookingPolicyRepository;
import com.pleasebookme.server.core.enums.BookingMode;
import com.pleasebookme.server.core.schedule.entity.ScheduleEntity;
import com.pleasebookme.server.core.schedule.repository.ScheduleRepository;
import com.pleasebookme.server.core.service.entity.ServiceEntity;
import com.pleasebookme.server.core.service.repository.ServiceRepository;
import com.pleasebookme.server.global.enums.Locale;
import com.pleasebookme.server.organization.membership.repository.MembershipRepository;
import com.pleasebookme.server.organization.membershiprole.entity.MembershipRoleEntity;
import com.pleasebookme.server.organization.membershiprole.repository.MembershipRoleRepository;
import com.pleasebookme.server.organization.organizations.repository.OrganizationRepository;
import com.pleasebookme.server.organization.profile.repository.ProfileRepository;
import com.pleasebookme.server.service.workspace.dto.WorkspaceProvisionRequest;
import com.pleasebookme.server.service.workspace.service.impl.WorkspaceProvisioningServiceImpl;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkspaceProvisioningServiceImplTest {
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private UserRoleRepository userRoleRepository;
    @Mock private OrganizationRepository organizationRepository;
    @Mock private MembershipRepository membershipRepository;
    @Mock private MembershipRoleRepository membershipRoleRepository;
    @Mock private ProfileRepository profileRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private EcosystemRepository ecosystemRepository;
    @Mock private TenantPlanRepository tenantPlanRepository;
    @Mock private ScheduleRepository scheduleRepository;
    @Mock private AvailabilityRepository availabilityRepository;
    @Mock private ServiceRepository serviceRepository;
    @Mock private BookingPolicyRepository bookingPolicyRepository;

    private WorkspaceProvisioningServiceImpl service;
    private EcosystemEntity ecosystem;
    private TenantPlanEntity plan;
    private RoleEntity userRole;
    private RoleEntity organizationOwnerRole;

    @BeforeEach
    void setUp() {
        service = new WorkspaceProvisioningServiceImpl(
            userRepository,
            roleRepository,
            userRoleRepository,
            organizationRepository,
            membershipRepository,
            membershipRoleRepository,
            profileRepository,
            tenantRepository,
            ecosystemRepository,
            tenantPlanRepository,
            scheduleRepository,
            availabilityRepository,
            serviceRepository,
            bookingPolicyRepository
        );

        ecosystem = EcosystemEntity.builder().code("GENERAL").name("General").build();
        ecosystem.setEcosystemId(BigInteger.ONE);
        plan = TenantPlanEntity.builder()
            .code("FREE")
            .maxUsers(1)
            .maxServices(10)
            .maxWidgets(3)
            .build();
        plan.setTenantPlanId(BigInteger.ONE);
        userRole = RoleEntity.builder().name("USER").build();
        organizationOwnerRole = RoleEntity.builder().name("ORGANIZATION_OWNER").build();
    }

    @Test
    void provision_createsFreeTenantFromOrganizationAndUserDefaults() {
        stubPrerequisites();

        service.provision(
            new WorkspaceProvisionRequest(
                "jane",
                "Jane Doe",
                "jane@example.com",
                null,
                Locale.en,
                "America/New_York"
            )
        );

        ArgumentCaptor<TenantEntity> tenantCaptor = ArgumentCaptor.forClass(TenantEntity.class);
        verify(tenantRepository).save(tenantCaptor.capture());
        TenantEntity tenant = tenantCaptor.getValue();

        assertThat(tenant.getOrganization().getName()).isEqualTo("Jane Doe's Organization");
        assertThat(tenant.getOwnerUser().getUsername()).isEqualTo("jane");
        assertThat(tenant.getEcosystem()).isSameAs(ecosystem);
        assertThat(tenant.getPlan()).isSameAs(plan);
        assertThat(tenant.getName()).isEqualTo("Jane Doe's Organization");
        assertThat(tenant.getSlug()).isEqualTo("janedoe-organization");
        assertThat(tenant.getStatus()).isEqualTo(TenantStatus.ACTIVE);
        assertThat(tenant.getRegion()).isEqualTo(TenantRegion.US);
        assertThat(tenant.getDefaultTimezone()).isEqualTo("America/New_York");
        assertThat(tenant.getDefaultLocale()).isEqualTo(Locale.en);
        assertThat(tenant.getMaxUsers()).isEqualTo(1);
        assertThat(tenant.getMaxServices()).isEqualTo(10);
        assertThat(tenant.getMaxWidgets()).isEqualTo(3);
    }

    @Test
    void provision_assignsOrganizationOwnerRoleToTheNewMembership() {
        stubPrerequisites();

        var user = service.provision(
            new WorkspaceProvisionRequest(
                "jane",
                "Jane Doe",
                "jane@example.com",
                null,
                Locale.en,
                "America/New_York"
            )
        );

        ArgumentCaptor<MembershipRoleEntity> captor = ArgumentCaptor.forClass(MembershipRoleEntity.class);
        verify(membershipRoleRepository).save(captor.capture());
        MembershipRoleEntity membershipRole = captor.getValue();

        assertThat(membershipRole.getRole()).isSameAs(organizationOwnerRole);
        assertThat(membershipRole.getMembership().getUser()).isSameAs(user);
        assertThat(membershipRole.getMembership().getOrganization().getName())
            .isEqualTo("Jane Doe's Organization");
        assertThat(membershipRole.getMembership().getAccepted()).isTrue();
    }

    @Test
    void provision_createsWeekdayNineToFiveScheduleInUserTimezone() {
        stubPrerequisites();

        var user = service.provision(
            new WorkspaceProvisionRequest(
                "jane",
                "Jane Doe",
                "jane@example.com",
                null,
                Locale.en,
                "America/New_York"
            )
        );

        ArgumentCaptor<ScheduleEntity> scheduleCaptor = ArgumentCaptor.forClass(ScheduleEntity.class);
        verify(scheduleRepository).save(scheduleCaptor.capture());
        ScheduleEntity schedule = scheduleCaptor.getValue();
        assertThat(schedule.getUser()).isSameAs(user);
        assertThat(schedule.getTitle()).isEqualTo("Working Hours");
        assertThat(schedule.getTimezone()).isEqualTo("America/New_York");

        ArgumentCaptor<AvailabilityEntity> availabilityCaptor = ArgumentCaptor.forClass(AvailabilityEntity.class);
        verify(availabilityRepository).save(availabilityCaptor.capture());
        AvailabilityEntity availability = availabilityCaptor.getValue();
        assertThat(availability.getUser()).isSameAs(user);
        assertThat(availability.getSchedule()).isSameAs(schedule);
        // ISO weekdays: Monday = 1 ... Friday = 5, never the dashboard's Sunday = 0 form.
        assertThat(availability.getDays()).containsExactly(1, 2, 3, 4, 5);
        assertThat(availability.getStartTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(availability.getEndTime()).isEqualTo(LocalTime.of(17, 0));
    }

    @Test
    void provision_createsConsultantMeetingServiceBoundToWorkspaceAndSchedule() {
        stubPrerequisites();

        var user = service.provision(
            new WorkspaceProvisionRequest(
                "jane",
                "Jane Doe",
                "jane@example.com",
                null,
                Locale.vi,
                "Asia/Ho_Chi_Minh"
            )
        );

        ArgumentCaptor<ScheduleEntity> scheduleCaptor = ArgumentCaptor.forClass(ScheduleEntity.class);
        verify(scheduleRepository).save(scheduleCaptor.capture());

        ArgumentCaptor<ServiceEntity> serviceCaptor = ArgumentCaptor.forClass(ServiceEntity.class);
        verify(serviceRepository).save(serviceCaptor.capture());
        ServiceEntity businessService = serviceCaptor.getValue();

        assertThat(businessService.getTitle()).isEqualTo("Consultant Meeting");
        assertThat(businessService.getSlug()).isEqualTo("consultant-meeting");
        assertThat(businessService.getUser()).isSameAs(user);
        assertThat(businessService.getProfile().getUser()).isSameAs(user);
        assertThat(businessService.getProfile().getUsername()).isEqualTo("jane");
        assertThat(businessService.getOrganization().getName()).isEqualTo("Jane Doe's Organization");
        assertThat(businessService.getSchedule()).isSameAs(scheduleCaptor.getValue());
        assertThat(businessService.getInterfaceLanguage()).isEqualTo(Locale.vi);
        assertThat(businessService.getTimezone()).isEqualTo("Asia/Ho_Chi_Minh");
    }

    @Test
    void provision_createsThirtyMinuteRollingBookingPolicyForDefaultService() {
        stubPrerequisites();

        service.provision(
            new WorkspaceProvisionRequest(
                "jane",
                "Jane Doe",
                "jane@example.com",
                null,
                Locale.en,
                "Australia/Sydney"
            )
        );

        ArgumentCaptor<ServiceEntity> serviceCaptor = ArgumentCaptor.forClass(ServiceEntity.class);
        verify(serviceRepository).save(serviceCaptor.capture());

        ArgumentCaptor<BookingPolicyEntity> policyCaptor = ArgumentCaptor.forClass(BookingPolicyEntity.class);
        verify(bookingPolicyRepository).save(policyCaptor.capture());
        BookingPolicyEntity policy = policyCaptor.getValue();

        assertThat(policy.getService()).isSameAs(serviceCaptor.getValue());
        assertThat(policy.getDefaultDuration()).isEqualTo(30);
        assertThat(policy.getSlotInterval()).isEqualTo(30);
        assertThat(policy.getMinimumNotice()).isEqualTo(120);
        assertThat(policy.getMaximumAdvanceBooking()).isEqualTo(30 * 24 * 60);
        assertThat(policy.getBookingWindowType()).isEqualTo("ROLLING");
        assertThat(policy.getCapacity()).isEqualTo(1);
        assertThat(policy.getBookingMode()).isEqualTo(BookingMode.FIXED);
        assertThat(policy.getBeforeBuffer()).isZero();
        assertThat(policy.getAfterBuffer()).isZero();
        assertThat(policy.getAllowOverlap()).isFalse();
        assertThat(policy.getRequiresPayment()).isFalse();
        assertThat(policy.getAutoConfirm()).isTrue();
    }

    @Test
    void provision_missingQuotaLimitFailsBeforeTenantSave() {
        plan.setMaxWidgets(null);
        stubPrerequisites();

        assertThatThrownBy(() -> service.provision(
            new WorkspaceProvisionRequest(
                "jane",
                "Jane",
                "jane@example.com",
                null,
                Locale.en,
                "Asia/Ho_Chi_Minh"
            )
        ))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Plan FREE is missing quota limits; apply V139");

        verify(tenantRepository, never()).save(any());
        verify(scheduleRepository, never()).save(any());
        verify(serviceRepository, never()).save(any());
    }

    @Test
    void provision_tenantSlugCollisionFallsBackToUserUid() {
        stubPrerequisites();
        when(tenantRepository.existsBySlug("jane-organization")).thenReturn(true);

        var user = service.provision(
            new WorkspaceProvisionRequest(
                "jane",
                "Jane",
                "jane@example.com",
                null,
                Locale.en,
                "Asia/Singapore"
            )
        );

        ArgumentCaptor<TenantEntity> tenantCaptor = ArgumentCaptor.forClass(TenantEntity.class);
        verify(tenantRepository).save(tenantCaptor.capture());
        assertThat(tenantCaptor.getValue().getSlug()).isEqualTo(user.getUserUid().toString());
        assertThat(tenantCaptor.getValue().getRegion()).isEqualTo(TenantRegion.SG);
    }

    @Test
    void provision_missingEcosystemThrowsTypedException() {
        stubIdentityPrerequisites();
        when(ecosystemRepository.findByCode("GENERAL")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.provision(
            new WorkspaceProvisionRequest(
                "jane",
                "Jane",
                "jane@example.com",
                null,
                Locale.en,
                null
            )
        ))
            .isInstanceOf(EcosystemNotFoundException.class)
            .hasMessage("Ecosystem not found: GENERAL");

        verify(tenantRepository, never()).save(any());
    }

    @Test
    void provision_missingPlanThrowsTypedException() {
        stubIdentityPrerequisites();
        when(ecosystemRepository.findByCode("GENERAL")).thenReturn(Optional.of(ecosystem));
        when(tenantPlanRepository.findByCode("FREE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.provision(
            new WorkspaceProvisionRequest(
                "jane",
                "Jane",
                "jane@example.com",
                null,
                Locale.en,
                null
            )
        ))
            .isInstanceOf(TenantPlanNotFoundException.class)
            .hasMessage("Tenant plan not found: FREE");

        verify(tenantRepository, never()).save(any());
    }

    @Test
    void provision_missingOrganizationOwnerRoleThrowsBeforeProfileOrTenant() {
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(roleRepository.findByName("ORGANIZATION_OWNER")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.provision(
            new WorkspaceProvisionRequest(
                "jane",
                "Jane",
                "jane@example.com",
                null,
                Locale.en,
                null
            )
        ))
            .isInstanceOf(RoleNotFoundException.class)
            .hasMessage("Role not found: ORGANIZATION_OWNER");

        verify(membershipRoleRepository, never()).save(any());
        verify(profileRepository, never()).save(any());
        verify(tenantRepository, never()).save(any());
    }

    private void stubPrerequisites() {
        stubIdentityPrerequisites();
        when(ecosystemRepository.findByCode("GENERAL")).thenReturn(Optional.of(ecosystem));
        when(tenantPlanRepository.findByCode("FREE")).thenReturn(Optional.of(plan));
    }

    private void stubIdentityPrerequisites() {
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(roleRepository.findByName("ORGANIZATION_OWNER")).thenReturn(Optional.of(organizationOwnerRole));
    }
}
