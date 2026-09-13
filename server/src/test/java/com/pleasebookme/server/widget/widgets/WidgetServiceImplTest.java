package com.pleasebookme.server.widget.widgets;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.organization.membership.entity.MembershipEntity;
import com.pleasebookme.server.organization.organizations.entity.OrganizationEntity;
import com.pleasebookme.server.service.organization.context.OrganizationContext;
import com.pleasebookme.server.service.organization.service.CurrentOrganizationProvider;
import com.pleasebookme.server.service.tenant.service.CurrentTenantProvider;
import com.pleasebookme.server.tenant.tenants.entity.TenantEntity;
import com.pleasebookme.server.widget.enums.WidgetStatus;
import com.pleasebookme.server.widget.enums.WidgetType;
import com.pleasebookme.server.widget.widgetorigin.entity.WidgetOriginEntity;
import com.pleasebookme.server.widget.widgetorigin.repository.WidgetOriginRepository;
import com.pleasebookme.server.widget.widgets.credential.WidgetCredentialGenerator;
import com.pleasebookme.server.widget.widgets.dto.WidgetCreateRequest;
import com.pleasebookme.server.widget.widgets.dto.WidgetCredentialRequest;
import com.pleasebookme.server.widget.widgets.dto.WidgetFilter;
import com.pleasebookme.server.widget.widgets.dto.WidgetUpdateRequest;
import com.pleasebookme.server.widget.widgets.entity.WidgetEntity;
import com.pleasebookme.server.widget.widgets.exception.DuplicateWidgetException;
import com.pleasebookme.server.widget.widgets.exception.WidgetRevokedException;
import com.pleasebookme.server.widget.widgets.repository.WidgetRepository;
import com.pleasebookme.server.widget.widgets.service.impl.WidgetServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WidgetServiceImplTest {
    private static final BigInteger ORGANIZATION_ID = BigInteger.valueOf(7);
    private static final BigInteger TENANT_ID = BigInteger.valueOf(11);
    private static final BigInteger WIDGET_ID = BigInteger.valueOf(19);
    private static final String PUBLIC_KEY = "pbm_pk_1234567890123456789012";
    private static final String SECRET_KEY = "pbm_sk_1234567890123456789012345678901234567890123";

    @Mock private WidgetRepository widgetRepository;
    @Mock private WidgetOriginRepository widgetOriginRepository;
    @Mock private CurrentOrganizationProvider currentOrganizationProvider;
    @Mock private CurrentTenantProvider currentTenantProvider;
    @Mock private WidgetCredentialGenerator widgetCredentialGenerator;
    @Mock private PasswordEncoder passwordEncoder;

    private WidgetServiceImpl service;
    private UserEntity user;
    private OrganizationContext organizationContext;
    private TenantEntity tenant;

    @BeforeEach
    void setUp() {
        service = new WidgetServiceImpl(
            widgetRepository,
            widgetOriginRepository,
            currentOrganizationProvider,
            currentTenantProvider,
            widgetCredentialGenerator,
            passwordEncoder
        );

        user = UserEntity.builder().username("jane").build();
        user.setUserId(BigInteger.valueOf(3));
        OrganizationEntity organization = OrganizationEntity.builder().name("Studio").slug("studio").build();
        organization.setOrganizationId(ORGANIZATION_ID);
        MembershipEntity membership = MembershipEntity.builder()
            .user(user)
            .organization(organization)
            .accepted(true)
            .build();
        organizationContext = new OrganizationContext(user, membership, organization);
        tenant = TenantEntity.builder().organization(organization).build();
        tenant.setTenantId(TENANT_ID);
    }

    @Test
    void createWidget_hashesSecretSetsActiveAndCreatesNormalizedOrigin() {
        when(currentOrganizationProvider.requireCurrent()).thenReturn(organizationContext);
        when(currentTenantProvider.requireCurrent()).thenReturn(tenant);
        when(passwordEncoder.encode(SECRET_KEY)).thenReturn("$2a$11$hash");
        when(widgetRepository.save(any())).thenAnswer(invocation -> {
            WidgetEntity widget = invocation.getArgument(0);
            widget.setWidgetId(WIDGET_ID);
            return widget;
        });
        when(widgetOriginRepository.findAllByWidgetWidgetIdIn(List.of(WIDGET_ID))).thenReturn(List.of());
        when(widgetOriginRepository.save(any())).thenAnswer(invocation -> {
            WidgetOriginEntity origin = invocation.getArgument(0);
            origin.setWidgetOriginId(BigInteger.ONE);
            return origin;
        });

        var detail = service.createWidget(new WidgetCreateRequest(
            "Booking widget",
            WidgetType.INLINE,
            "Barbershop.com/book?x=1",
            credentials()
        ));

        assertThat(detail.widget().getTenant()).isSameAs(tenant);
        assertThat(detail.widget().getStatus()).isEqualTo(WidgetStatus.ACTIVE);
        assertThat(detail.widget().getSecretKey()).isEqualTo("$2a$11$hash");
        assertThat(detail.widget().getIssuedAt()).isNotNull();
        assertThat(detail.widget().getExpiresAt()).isNull();
        assertThat(detail.widget().getOriginValidation()).isTrue();
        assertThat(detail.origin().getOrigin()).isEqualTo("https://barbershop.com");
        assertThat(detail.origin().getCreatedBy()).isSameAs(user);
    }

    @Test
    void createWidget_duplicatePublicKeyFailsBeforeAnySave() {
        when(currentOrganizationProvider.requireCurrent()).thenReturn(organizationContext);
        when(currentTenantProvider.requireCurrent()).thenReturn(tenant);
        when(widgetRepository.existsByPublicKey(PUBLIC_KEY)).thenReturn(true);

        assertThatThrownBy(() -> service.createWidget(new WidgetCreateRequest(
            "Booking widget", WidgetType.INLINE, null, credentials()
        )))
            .isInstanceOf(DuplicateWidgetException.class);

        verify(widgetRepository, never()).save(any());
        verify(widgetOriginRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void updateWidget_withoutCredentialsLeavesKeysAndIssuedAtUnchanged() {
        Instant issuedAt = Instant.parse("2026-09-11T00:00:00Z");
        WidgetEntity widget = widget(WidgetStatus.ACTIVE);
        widget.setSecretKey("stored-hash");
        widget.setIssuedAt(issuedAt);
        when(widgetRepository.findById(WIDGET_ID)).thenReturn(Optional.of(widget));
        when(widgetOriginRepository.findAllByWidgetWidgetIdIn(List.of(WIDGET_ID))).thenReturn(List.of());
        when(widgetRepository.save(widget)).thenReturn(widget);

        var detail = service.updateWidget(WIDGET_ID, new WidgetUpdateRequest(
            "Renamed", WidgetType.POPUP, WidgetStatus.DISABLED, null, null
        ));

        assertThat(detail.widget().getName()).isEqualTo("Renamed");
        assertThat(detail.widget().getStatus()).isEqualTo(WidgetStatus.DISABLED);
        assertThat(detail.widget().getPublicKey()).isEqualTo(PUBLIC_KEY);
        assertThat(detail.widget().getSecretKey()).isEqualTo("stored-hash");
        assertThat(detail.widget().getIssuedAt()).isEqualTo(issuedAt);
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void updateWidget_revokedWidgetCannotBeResurrected() {
        when(widgetRepository.findById(WIDGET_ID)).thenReturn(Optional.of(widget(WidgetStatus.REVOKED)));

        assertThatThrownBy(() -> service.updateWidget(WIDGET_ID, new WidgetUpdateRequest(
            "Renamed", null, WidgetStatus.ACTIVE, null, null
        )))
            .isInstanceOf(WidgetRevokedException.class);

        verify(widgetRepository, never()).save(any());
        verify(widgetOriginRepository, never()).findAllByWidgetWidgetIdIn(any());
    }

    @Test
    void deleteWidget_setsRevokedAndKeepsRow() {
        WidgetEntity widget = widget(WidgetStatus.ACTIVE);
        when(widgetRepository.findById(WIDGET_ID)).thenReturn(Optional.of(widget));

        service.deleteWidget(WIDGET_ID);

        assertThat(widget.getStatus()).isEqualTo(WidgetStatus.REVOKED);
        verify(widgetRepository).save(widget);
        verify(widgetRepository, never()).delete(any(WidgetEntity.class));
        verify(widgetOriginRepository, never()).deleteAll(any());
    }

    @Test
    void deleteWidget_isIdempotentWhenAlreadyRevoked() {
        WidgetEntity widget = widget(WidgetStatus.REVOKED);
        when(widgetRepository.findById(WIDGET_ID)).thenReturn(Optional.of(widget));

        service.deleteWidget(WIDGET_ID);

        verify(widgetRepository, never()).save(any());
    }

    @Test
    void getWidgets_hydratesOriginsWithExactlyOneBatchedQuery() {
        WidgetEntity widget = widget(WidgetStatus.ACTIVE);
        WidgetOriginEntity origin = WidgetOriginEntity.builder()
            .widget(widget)
            .origin("https://example.com")
            .build();
        origin.setWidgetOriginId(BigInteger.ONE);
        Pageable pageable = PageRequest.of(0, 20);

        when(currentOrganizationProvider.requireCurrent()).thenReturn(organizationContext);
        when(currentTenantProvider.requireByOrganizationId(ORGANIZATION_ID)).thenReturn(tenant);
        when(widgetRepository.findAll(
            org.mockito.ArgumentMatchers.<Specification<WidgetEntity>>any(),
            any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(widget), pageable, 1));
        when(widgetOriginRepository.findAllByWidgetWidgetIdIn(List.of(WIDGET_ID)))
            .thenReturn(List.of(origin));

        var page = service.getWidgetsByOrganizationId(
            ORGANIZATION_ID,
            WidgetFilter.none(),
            pageable
        );

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).origin()).isSameAs(origin);
        verify(widgetOriginRepository, times(1)).findAllByWidgetWidgetIdIn(List.of(WIDGET_ID));
    }

    @Test
    void getWidgets_foreignOrganizationReturnsEmptyWithoutTenantLookup() {
        when(currentOrganizationProvider.requireCurrent()).thenReturn(organizationContext);

        var page = service.getWidgetsByOrganizationId(
            BigInteger.valueOf(999),
            WidgetFilter.none(),
            PageRequest.of(0, 20)
        );

        assertThat(page).isEmpty();
        verify(currentTenantProvider, never()).requireByOrganizationId(any());
        verify(widgetRepository, never()).findAll(
            org.mockito.ArgumentMatchers.<Specification<WidgetEntity>>any(),
            any(Pageable.class)
        );
    }

    private WidgetCredentialRequest credentials() {
        return new WidgetCredentialRequest(PUBLIC_KEY, SECRET_KEY);
    }

    private WidgetEntity widget(WidgetStatus status) {
        WidgetEntity widget = WidgetEntity.builder()
            .tenant(tenant)
            .name("Widget")
            .status(status)
            .publicKey(PUBLIC_KEY)
            .secretKey("stored-hash")
            .build();
        widget.setWidgetId(WIDGET_ID);
        return widget;
    }
}
