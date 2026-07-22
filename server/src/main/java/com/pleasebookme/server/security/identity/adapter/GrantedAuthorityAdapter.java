package com.pleasebookme.server.security.identity.adapter;

import com.pleasebookme.server.security.identity.principal.AuthenticatedPrincipal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class GrantedAuthorityAdapter {
    public Collection<GrantedAuthority> adapt(
        AuthenticatedPrincipal principal
    ) {
        return Stream.concat(
            principal.roles()
                .stream()
                .map(role -> "ROLE_" + role),

            principal.permissions()
                .stream()
        )
            .distinct()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toUnmodifiableSet());
    }
}
