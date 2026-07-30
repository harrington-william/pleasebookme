package com.pleasebookme.server.security.identity.adapter;

import com.pleasebookme.server.security.identity.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserDetailsAdapter {
    private final GrantedAuthorityAdapter grantedAuthorityAdapter;

    public PrincipalUserDetails adapt(
        UserPrincipal principal
    ) {
        return new PrincipalUserDetails(
            principal,
            null,
            grantedAuthorityAdapter.adapt(principal)
        );
    }

    public PrincipalUserDetails adapt(
        UserPrincipal principal,
        String passwordHash
    ) {
        return new PrincipalUserDetails(
            principal,
            passwordHash,
            grantedAuthorityAdapter.adapt(principal)
        );
    }
}
