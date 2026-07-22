package com.pleasebookme.server.security.identity.adapter;

import com.pleasebookme.server.security.identity.principal.AuthenticatedPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserDetailsAdapter {
    private final GrantedAuthorityAdapter grantedAuthorityAdapter;

    public PrincipalUserDetails adapt(
        AuthenticatedPrincipal principal
    ) {
        return new PrincipalUserDetails(
            principal,
            grantedAuthorityAdapter.adapt(principal)
        );
    }
}
