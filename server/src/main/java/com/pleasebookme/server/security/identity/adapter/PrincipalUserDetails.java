package com.pleasebookme.server.security.identity.adapter;

import com.pleasebookme.server.auth.enums.AccountStatus;
import com.pleasebookme.server.security.identity.principal.UserPrincipal;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class PrincipalUserDetails implements UserDetails {
    private final UserPrincipal principal;
    private final String passwordHash;
    private final Collection<? extends GrantedAuthority> authorities;

    public PrincipalUserDetails(
        UserPrincipal userPrincipal,
        String passwordHash,
        Collection<? extends GrantedAuthority> authorities
    ) {
        this.principal = userPrincipal;
        this.passwordHash = passwordHash;
        this.authorities = authorities;
    }

    public UserPrincipal getUserPrincipal() {
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
        return passwordHash;
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
