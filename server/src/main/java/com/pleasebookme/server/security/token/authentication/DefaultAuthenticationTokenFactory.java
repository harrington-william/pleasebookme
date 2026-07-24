package com.pleasebookme.server.security.token.authentication;

import com.pleasebookme.server.security.identity.adapter.PrincipalUserDetails;
import com.pleasebookme.server.security.identity.adapter.UserDetailsAdapter;
import com.pleasebookme.server.security.identity.principal.AuthenticatedPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultAuthenticationTokenFactory
    implements AuthenticationTokenFactory {
    private final UserDetailsAdapter userDetailsAdapter;

    @Override
    public Authentication create(AuthenticatedPrincipal principal) {
        PrincipalUserDetails userDetails = userDetailsAdapter.adapt(principal);

        return UsernamePasswordAuthenticationToken.authenticated(
            userDetails,
            null,
            userDetails.getAuthorities()
        );
    }
}
