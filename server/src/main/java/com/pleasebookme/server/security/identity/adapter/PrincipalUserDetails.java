package com.pleasebookme.server.security.identity.adapter;

import com.pleasebookme.server.auth.enums.AccountStatus;
import com.pleasebookme.server.security.identity.principal.AuthenticatedPrincipal;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class PrincipalUserDetails implements UserDetails {
    private final AuthenticatedPrincipal principal;
    private final Collection<? extends GrantedAuthority> authorities;

    public PrincipalUserDetails(
        AuthenticatedPrincipal authenticatedPrincipal,
        Collection<? extends GrantedAuthority> authorities
    ) {
        this.principal = authenticatedPrincipal;
        this.authorities = authorities;
    }

    public AuthenticatedPrincipal getAuthenticatedPrincipal() {
        return principal;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getUsername() {
        return principal.username();
    }

    @Override
    public @Nullable String getPassword() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return principal.isActive();
    }

    @Override
    public boolean isAccountNonLocked() {
        return principal.accountStatus() != AccountStatus.LOCKED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return principal.isActive();
    }
}
