package com.pleasebookme.server.service.widget.fullpage;

import com.pleasebookme.server.organization.organizations.entity.OrganizationEntity;
import com.pleasebookme.server.organization.organizations.exception.OrganizationNotFoundException;
import com.pleasebookme.server.organization.organizations.repository.OrganizationRepository;
import com.pleasebookme.server.tenant.tenants.repository.TenantRepository;
import com.pleasebookme.server.tenant.tenants.entity.TenantEntity;
import com.pleasebookme.server.tenant.ecosystem.entity.EcosystemEntity;
import com.pleasebookme.server.tenant.enums.TenantStatus;
import org.junit.jupiter.api.Test;
import java.math.BigInteger;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class SlugOrganizationResolverTest {
    private final OrganizationRepository organizations = mock(OrganizationRepository.class);
    private final TenantRepository tenants = mock(TenantRepository.class);
    private final SlugOrganizationResolver resolver = new SlugOrganizationResolver(organizations, tenants);
    private final OrganizationEntity organization = OrganizationEntity.builder().organizationId(BigInteger.ONE).build();

    @Test
    void unknownSlugStopsBeforeTenantLookup() {
        assertThatThrownBy(() -> resolver.resolve("login")).isInstanceOf(OrganizationNotFoundException.class);
        verify(organizations).findBySlug("login");
        verifyNoInteractions(tenants);
    }

    @Test
    void missingTenantIsNotFound() {
        when(organizations.findBySlug("acme")).thenReturn(Optional.of(organization));
        assertThatThrownBy(() -> resolver.resolve("acme")).isInstanceOf(OrganizationNotFoundException.class);
    }

    @Test
    void suspendedTenantIsNotFound() {
        stub(TenantStatus.SUSPENDED);
        assertThatThrownBy(() -> resolver.resolve("acme"))
            .isInstanceOf(OrganizationNotFoundException.class).hasMessage("Organization not found: acme");
    }

    @Test
    void servedTenantIncludesEcosystem() {
        for (TenantStatus status : new TenantStatus[]{TenantStatus.ACTIVE, TenantStatus.TRIAL}) {
            stub(status);
            var served = resolver.resolve("acme");
            assertThat(served.organization()).isSameAs(organization);
            assertThat(served.ecosystemCode()).isEqualTo("GENERAL");
        }
    }

    private void stub(TenantStatus status) {
        when(organizations.findBySlug("acme")).thenReturn(Optional.of(organization));
        when(tenants.findByOrganizationOrganizationId(BigInteger.ONE)).thenReturn(Optional.of(
            TenantEntity.builder().status(status).ecosystem(EcosystemEntity.builder().code("GENERAL").build()).build()));
    }
}
