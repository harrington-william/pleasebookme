package com.pleasebookme.server.security.identity.loader.widget;

import com.pleasebookme.server.security.identity.verifier.WidgetVerifier;
import com.pleasebookme.server.tenant.tenants.entity.TenantEntity;
import com.pleasebookme.server.widget.enums.WidgetStatus;
import com.pleasebookme.server.widget.widgetorigin.repository.WidgetOriginRepository;
import com.pleasebookme.server.widget.widgets.entity.WidgetEntity;
import com.pleasebookme.server.widget.widgets.exception.WidgetExpiredException;
import com.pleasebookme.server.widget.widgets.exception.WidgetNotActiveException;
import com.pleasebookme.server.widget.widgets.repository.WidgetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultWidgetIdentityLoaderTest {
    private final WidgetRepository widgets = mock(WidgetRepository.class);
    private final WidgetOriginRepository origins = mock(WidgetOriginRepository.class);
    private final WidgetVerifier verifier = mock(WidgetVerifier.class);
    private final DefaultWidgetIdentityLoader loader = new DefaultWidgetIdentityLoader(widgets, origins, verifier);
    private WidgetEntity widget;

    @BeforeEach
    void setUp() {
        widget = WidgetEntity.builder().status(WidgetStatus.ACTIVE).originValidation(false)
            .tenant(TenantEntity.builder().tenantId(BigInteger.ONE).build()).build();
        when(widgets.findByPublicKey("public")).thenReturn(Optional.of(widget));
        when(verifier.verify("secret", widget)).thenReturn(true);
    }

    @Test
    void revokedWithoutOriginValidationIsRefused() {
        widget.setStatus(WidgetStatus.REVOKED);
        assertThatThrownBy(() -> loader.loadByPublicKey("public", "secret", null))
            .isInstanceOf(WidgetNotActiveException.class);
        verifyNoInteractions(origins);
    }

    @Test
    void expiredWithoutOriginValidationIsRefused() {
        widget.setExpiresAt(Instant.now().minusSeconds(60));
        assertThatThrownBy(() -> loader.loadByPublicKey("public", "secret", null))
            .isInstanceOf(WidgetExpiredException.class);
        verifyNoInteractions(origins);
    }

    @Test
    void activeWithoutOriginValidationNeedsNoOrigin() {
        assertThat(loader.loadByPublicKey("public", "secret", null).isActive()).isTrue();
        verifyNoInteractions(origins);
    }
}
