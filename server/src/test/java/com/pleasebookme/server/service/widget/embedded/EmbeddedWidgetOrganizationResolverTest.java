package com.pleasebookme.server.service.widget.embedded;

import com.pleasebookme.server.security.identity.context.principal.DefaultCurrentPrincipalProvider;
import com.pleasebookme.server.security.identity.enums.AuthenticatedActorType;
import com.pleasebookme.server.security.identity.exception.ForbiddenActorException;
import com.pleasebookme.server.security.identity.exception.UnauthenticatedException;
import com.pleasebookme.server.security.identity.principal.UserPrincipal;
import com.pleasebookme.server.security.identity.principal.WidgetPrincipal;
import com.pleasebookme.server.widget.enums.WidgetStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import java.math.BigInteger;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.pleasebookme.server.organization.organizations.entity.OrganizationEntity;
import com.pleasebookme.server.organization.organizations.exception.OrganizationNotFoundException;
import com.pleasebookme.server.tenant.tenants.entity.TenantEntity;
import com.pleasebookme.server.tenant.tenants.repository.TenantRepository;
import com.pleasebookme.server.tenant.ecosystem.entity.EcosystemEntity;
import com.pleasebookme.server.tenant.enums.TenantStatus;
import com.pleasebookme.server.widget.widgets.exception.WidgetNotActiveException;
import java.util.Optional;

class EmbeddedWidgetOrganizationResolverTest {
    private final TenantRepository tenants = mock(TenantRepository.class);
    private final EmbeddedWidgetOrganizationResolver resolver = new EmbeddedWidgetOrganizationResolver(new DefaultCurrentPrincipalProvider(), tenants);
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }

    @Test void userIsForbiddenBeforeLookup() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(mock(UserPrincipal.class), null, "test"));
        assertThatThrownBy(resolver::resolve).isInstanceOf(ForbiddenActorException.class);
        verifyNoInteractions(tenants);
    }
    @Test void noPrincipalIsUnauthenticated() {
        SecurityContextHolder.clearContext();
        assertThatThrownBy(resolver::resolve).isInstanceOf(UnauthenticatedException.class);
        verifyNoInteractions(tenants);
    }
    @Test void inactiveWidgetIsRefusedBeforeLookup() {
        authenticate(WidgetStatus.REVOKED);
        assertThatThrownBy(resolver::resolve).isInstanceOf(WidgetNotActiveException.class);
        verifyNoInteractions(tenants);
    }
    @Test void suspendedTenantIsNotFound() {
        authenticate(WidgetStatus.ACTIVE);
        when(tenants.findById(BigInteger.valueOf(7))).thenReturn(Optional.of(TenantEntity.builder().status(TenantStatus.SUSPENDED).build()));
        assertThatThrownBy(resolver::resolve).isInstanceOf(OrganizationNotFoundException.class).hasMessage("Organization not found for widget");
    }
    @Test void missingTenantIsNotFound() {
        authenticate(WidgetStatus.ACTIVE);
        assertThatThrownBy(resolver::resolve).isInstanceOf(OrganizationNotFoundException.class).hasMessage("Organization not found for widget");
    }
    @Test void principalTenantDeterminesOrganizationAndEcosystem() {
        authenticate(WidgetStatus.ACTIVE);
        var organization = OrganizationEntity.builder().organizationId(BigInteger.TEN).build();
        when(tenants.findById(BigInteger.valueOf(7))).thenReturn(Optional.of(TenantEntity.builder()
            .status(TenantStatus.ACTIVE).organization(organization)
            .ecosystem(EcosystemEntity.builder().code("BARBERSHOP").build()).build()));
        var served = resolver.resolve();
        assertThat(served.organization()).isSameAs(organization);
        assertThat(served.ecosystemCode()).isEqualTo("BARBERSHOP");
        verify(tenants).findById(BigInteger.valueOf(7));
        verifyNoMoreInteractions(tenants);
    }
    private void authenticate(WidgetStatus status) {
        var widget = new WidgetPrincipal(AuthenticatedActorType.WIDGET, UUID.randomUUID(), UUID.randomUUID(), BigInteger.valueOf(7), status);
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(widget, null, "test"));
    }
}
