package com.pleasebookme.server.security.identity.context;

import com.pleasebookme.server.auth.enums.AccountStatus;
import com.pleasebookme.server.security.identity.adapter.PrincipalUserDetails;
import com.pleasebookme.server.security.identity.exception.ForbiddenActorException;
import com.pleasebookme.server.security.identity.exception.UnauthenticatedException;
import com.pleasebookme.server.security.identity.enums.AuthenticatedActorType;
import com.pleasebookme.server.security.identity.principal.UserPrincipal;
import com.pleasebookme.server.security.identity.principal.WidgetPrincipal;
import com.pleasebookme.server.widget.enums.WidgetStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultCurrentPrincipalProviderTest {

    private final DefaultCurrentPrincipalProvider provider = new DefaultCurrentPrincipalProvider();

    private static UserPrincipal userPrincipal() {
        return new UserPrincipal(
            AuthenticatedActorType.USER,
            UUID.randomUUID(), BigInteger.ONE, null,
            "jane", "jane@example.com", "Jane Doe", null,
            "Australia/Sydney", AccountStatus.ACTIVE, Set.of(), Set.of(), Map.of()
        );
    }

    private static WidgetPrincipal widgetPrincipal() {
        return new WidgetPrincipal(
            AuthenticatedActorType.WIDGET,
            UUID.randomUUID(),
            UUID.randomUUID(),
            BigInteger.ONE,
            WidgetStatus.ACTIVE
        );
    }

    @BeforeEach
    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void find_userWrappedInPrincipalUserDetails_unwrapsToUserPrincipal() {
        UserPrincipal principal = userPrincipal();
        PrincipalUserDetails details = new PrincipalUserDetails(
            principal, "hash", List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities())
        );

        assertThat(provider.find()).contains(principal);
        assertThat(provider.requireUser()).isEqualTo(principal);
    }

    @Test
    void find_bareWidgetPrincipal_returnsItDirectly() {
        WidgetPrincipal principal = widgetPrincipal();

        SecurityContextHolder.getContext().setAuthentication(
            new PreAuthenticatedAuthenticationToken(principal, null, List.of())
        );

        assertThat(provider.find()).contains(principal);
        assertThat(provider.require()).isEqualTo(principal);
    }

    @Test
    void requireUser_widgetActor_isForbidden() {
        SecurityContextHolder.getContext().setAuthentication(
            new PreAuthenticatedAuthenticationToken(widgetPrincipal(), null, List.of())
        );

        assertThatThrownBy(provider::requireUser)
            .isInstanceOf(ForbiddenActorException.class)
            // Naming the rejected actor type is the point: this lands in a log
            // where "forbidden" alone would not say which credential was used.
            .hasMessageContaining("WIDGET");
    }

    @Test
    void find_emptyContext_returnsEmpty() {
        assertThat(provider.find()).isEmpty();

        assertThatThrownBy(provider::require)
            .isInstanceOf(UnauthenticatedException.class);
    }

    @Test
    void find_anonymousToken_isTreatedAsUnauthenticated() {
        SecurityContextHolder.getContext().setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymousUser", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
            )
        );

        assertThat(provider.find()).isEmpty();
    }

    @Test
    void find_unknownPrincipalType_returnsEmpty() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("plain-string-principal", null, List.of())
        );

        assertThat(provider.find()).isEmpty();
    }
}
