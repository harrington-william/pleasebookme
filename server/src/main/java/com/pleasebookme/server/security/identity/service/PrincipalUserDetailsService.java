package com.pleasebookme.server.security.identity.service;

import com.pleasebookme.server.security.identity.adapter.UserDetailsAdapter;
import com.pleasebookme.server.security.identity.aggregation.AuthenticationAggregation;
import com.pleasebookme.server.security.identity.loader.DefaultIdentityLoader;
import com.pleasebookme.server.security.identity.loader.IdentityLoader;
import com.pleasebookme.server.security.identity.mapper.UserPrincipalMapper;
import com.pleasebookme.server.security.identity.principal.AuthenticatedPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PrincipalUserDetailsService implements UserDetailsService {
    private final IdentityLoader identityLoader;
    private final UserPrincipalMapper userPrincipalMapper;
    private final UserDetailsAdapter userDetailsAdapter;

    @Override
    public UserDetails loadUserByUsername(String username)
        throws UsernameNotFoundException {

        AuthenticationAggregation aggregation =
            identityLoader.loadByUsername(username);

        AuthenticatedPrincipal principal =
            userPrincipalMapper.map(aggregation);

        return userDetailsAdapter.adapt(principal);
    }
}
