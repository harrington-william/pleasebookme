package com.pleasebookme.server.service.tenant.service.impl;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.organization.membership.entity.MembershipEntity;
import com.pleasebookme.server.organization.organizations.entity.OrganizationEntity;
import com.pleasebookme.server.service.organization.context.OrganizationContext;
import com.pleasebookme.server.service.organization.service.CurrentOrganizationProvider;
import com.pleasebookme.server.tenant.tenants.entity.TenantEntity;
import com.pleasebookme.server.tenant.tenants.exception.TenantNotFoundException;
import com.pleasebookme.server.tenant.tenants.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrentTenantProviderImplTest {
    private static final BigInteger ORGANIZATION_ID = BigInteger.valueOf(7);

    @Mock private TenantRepository tenantRepository;
    @Mock private CurrentOrganizationProvider currentOrganizationProvider;

    private CurrentTenantProviderImpl provider;

    @BeforeEach
    void setUp() {
        provider = new CurrentTenantProviderImpl(tenantRepository, currentOrganizationProvider);
    }

    @Test
    void requireCurrent_mapsCurrentOrganizationToTenant() {
        OrganizationEntity organization = OrganizationEntity.builder().name("Studio").slug("studio").build();
        organization.setOrganizationId(ORGANIZATION_ID);
        UserEntity user = UserEntity.builder().username("jane").build();
        MembershipEntity membership = MembershipEntity.builder()
            .organization(organization)
            .user(user)
            .build();
        TenantEntity tenant = TenantEntity.builder().organization(organization).build();

        when(currentOrganizationProvider.requireCurrent())
            .thenReturn(new OrganizationContext(user, membership, organization));
        when(tenantRepository.findByOrganizationOrganizationId(ORGANIZATION_ID))
            .thenReturn(Optional.of(tenant));

        assertThat(provider.requireCurrent()).isSameAs(tenant);
    }

    @Test
    void requireByOrganizationId_missingTenantThrowsActionableException() {
        when(tenantRepository.findByOrganizationOrganizationId(ORGANIZATION_ID))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> provider.requireByOrganizationId(ORGANIZATION_ID))
            .isInstanceOf(TenantNotFoundException.class)
            .hasMessage("Organization 7 has no tenant; it predates tenant provisioning (V141)");
    }
}
