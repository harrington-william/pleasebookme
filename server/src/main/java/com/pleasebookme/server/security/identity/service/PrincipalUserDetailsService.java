package com.pleasebookme.server.security.identity.service;

import com.pleasebookme.server.auth.password.entity.UserPasswordEntity;
import com.pleasebookme.server.auth.password.service.UserPasswordService;
import com.pleasebookme.server.security.identity.adapter.UserDetailsAdapter;
import com.pleasebookme.server.security.identity.aggregation.AuthenticationAggregation;
import com.pleasebookme.server.security.identity.loader.user.UserIdentityLoader;
import com.pleasebookme.server.security.identity.mapper.UserPrincipalMapper;
import com.pleasebookme.server.security.identity.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Service
@RequiredArgsConstructor
public class PrincipalUserDetailsService implements UserDetailsService {
    private final UserPasswordService userPasswordService;
    private final UserIdentityLoader userIdentityLoader;
    private final UserPrincipalMapper userPrincipalMapper;
    private final UserDetailsAdapter userDetailsAdapter;

    @Override
    public UserDetails loadUserByUsername(String username)
        throws UsernameNotFoundException {

        AuthenticationAggregation aggregation =
            userIdentityLoader.loadByUsername(username);

        UserPrincipal principal =
            userPrincipalMapper.map(aggregation);

        // Load password
        BigInteger userId = aggregation.user().getUserId();
        UserPasswordEntity password =
            userPasswordService.getUserPasswordById(userId);

        String hash = password.getHash();

        return userDetailsAdapter.adapt(principal, hash);
    }
}
