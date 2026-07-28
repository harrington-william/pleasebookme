package com.pleasebookme.server.security.token.authentication;

import com.pleasebookme.server.security.identity.adapter.PrincipalUserDetails;
import com.pleasebookme.server.security.identity.adapter.UserDetailsAdapter;
import com.pleasebookme.server.security.identity.principal.AuthenticatedPrincipal;
import com.pleasebookme.server.security.identity.principal.UserPrincipal;
import com.pleasebookme.server.security.identity.principal.WidgetPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class DefaultAuthenticationTokenFactory
    implements AuthenticationTokenFactory {
    private final UserDetailsAdapter userDetailsAdapter;

    @Override
    public Authentication create(AuthenticatedPrincipal principal) {
        return switch (principal) {
            case UserPrincipal user -> createUserAuthentication(user);
            case WidgetPrincipal widget -> createWidgetAuthentication(widget);
        };
    }

    private Authentication createUserAuthentication(UserPrincipal principal) {
        PrincipalUserDetails userDetails = userDetailsAdapter.adapt(principal);

        return UsernamePasswordAuthenticationToken.authenticated(
            userDetails,
            null,
            userDetails.getAuthorities()
        );
    }

    private Authentication createWidgetAuthentication(WidgetPrincipal principal) {
        return new PreAuthenticatedAuthenticationToken(
            principal,
            null,
            Collections.emptyList()
        );
    }
}
