package com.pleasebookme.server.security.identity.context;

import com.pleasebookme.server.security.identity.context.DefaultCurrentPrincipalProvider;
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

class DefaultCurrentPrincipalProviderTest {
    private final DefaultCurrentPrincipalProvider provider = new DefaultCurrentPrincipalProvider();
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }

    @Test void widgetPassesThrough() {
        var widget = new WidgetPrincipal(AuthenticatedActorType.WIDGET, UUID.randomUUID(), UUID.randomUUID(), BigInteger.ONE, WidgetStatus.ACTIVE);
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(widget, null, "test"));
        assertThat(provider.requireWidget()).isSameAs(widget);
    }
    @Test void userIsForbidden() {
        var user = mock(UserPrincipal.class);
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(user, null, "test"));
        assertThatThrownBy(provider::requireWidget).isInstanceOf(ForbiddenActorException.class);
    }
    @Test void noAuthenticationIsUnauthenticated() {
        SecurityContextHolder.clearContext();
        assertThatThrownBy(provider::requireWidget).isInstanceOf(UnauthenticatedException.class);
    }
}
