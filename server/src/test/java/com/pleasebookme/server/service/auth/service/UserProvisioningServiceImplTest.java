package com.pleasebookme.server.service.auth.service;

import com.pleasebookme.server.auth.role.entity.RoleEntity;
import com.pleasebookme.server.auth.role.repository.RoleRepository;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import com.pleasebookme.server.auth.userrole.repository.UserRoleRepository;
import com.pleasebookme.server.global.enums.Locale;
import com.pleasebookme.server.organization.membership.repository.MembershipRepository;
import com.pleasebookme.server.organization.organizations.repository.OrganizationRepository;
import com.pleasebookme.server.organization.profile.repository.ProfileRepository;
import com.pleasebookme.server.service.auth.service.impl.UserProvisioningServiceImpl;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProvisioningServiceImplTest {
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private UserRoleRepository userRoleRepository;
    @Mock private OrganizationRepository organizationRepository;
    @Mock private MembershipRepository membershipRepository;
    @Mock private ProfileRepository profileRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private EcosystemRepository ecosystemRepository;
    @Mock private TenantPlanRepository tenantPlanRepository;

    private UserProvisioningServiceImpl service;
    private EcosystemEntity ecosystem;
    private TenantPlanEntity plan;

    @BeforeEach
    void setUp() {
        service = new UserProvisioningServiceImpl(
            userRepository,
            roleRepository,
            userRoleRepository,
            organizationRepository,
            membershipRepository,
            profileRepository,
            tenantRepository,
            ecosystemRepository,
            tenantPlanRepository
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
    }

    @Test
    void provisionUser_createsFreeTenantFromOrganizationAndUserDefaults() {
        stubPrerequisites();

        service.provisionUser(
            "jane",
            "Jane Doe",
            "jane@example.com",
            null,
            Locale.en,
            "America/New_York"
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
    void provisionUser_missingQuotaLimitFailsBeforeTenantSave() {
        plan.setMaxWidgets(null);
        stubPrerequisites();

        assertThatThrownBy(() -> service.provisionUser(
            "jane", "Jane", "jane@example.com", null, Locale.en, "Asia/Ho_Chi_Minh"
        ))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Plan FREE is missing quota limits; apply V139");

        verify(tenantRepository, never()).save(any());
    }

    @Test
    void provisionUser_tenantSlugCollisionFallsBackToUserUid() {
        stubPrerequisites();
        when(tenantRepository.existsBySlug("jane-organization")).thenReturn(true);

        var user = service.provisionUser(
            "jane", "Jane", "jane@example.com", null, Locale.en, "Asia/Singapore"
        );

        ArgumentCaptor<TenantEntity> tenantCaptor = ArgumentCaptor.forClass(TenantEntity.class);
        verify(tenantRepository).save(tenantCaptor.capture());
        assertThat(tenantCaptor.getValue().getSlug()).isEqualTo(user.getUserUid().toString());
        assertThat(tenantCaptor.getValue().getRegion()).isEqualTo(TenantRegion.SG);
    }

    @Test
    void provisionUser_missingEcosystemThrowsTypedException() {
        stubIdentityPrerequisites();
        when(ecosystemRepository.findByCode("GENERAL")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.provisionUser(
            "jane", "Jane", "jane@example.com", null, Locale.en, null
        ))
            .isInstanceOf(EcosystemNotFoundException.class)
            .hasMessage("Ecosystem not found: GENERAL");

        verify(tenantRepository, never()).save(any());
    }

    @Test
    void provisionUser_missingPlanThrowsTypedException() {
        stubIdentityPrerequisites();
        when(ecosystemRepository.findByCode("GENERAL")).thenReturn(Optional.of(ecosystem));
        when(tenantPlanRepository.findByCode("FREE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.provisionUser(
            "jane", "Jane", "jane@example.com", null, Locale.en, null
        ))
            .isInstanceOf(TenantPlanNotFoundException.class)
            .hasMessage("Tenant plan not found: FREE");

        verify(tenantRepository, never()).save(any());
    }

    private void stubPrerequisites() {
        stubIdentityPrerequisites();
        when(ecosystemRepository.findByCode("GENERAL")).thenReturn(Optional.of(ecosystem));
        when(tenantPlanRepository.findByCode("FREE")).thenReturn(Optional.of(plan));
    }

    private void stubIdentityPrerequisites() {
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(RoleEntity.builder().name("USER").build()));
    }
}
