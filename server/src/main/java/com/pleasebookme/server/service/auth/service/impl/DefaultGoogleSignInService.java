package com.pleasebookme.server.service.auth.service.impl;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.security.oauth.google.identity.GoogleIdentity;
import com.pleasebookme.server.security.oauth.google.verifier.GoogleTokenVerifier;
import com.pleasebookme.server.service.auth.dto.LoginResponse;
import com.pleasebookme.server.service.auth.service.GoogleAccountResolver;
import com.pleasebookme.server.service.auth.service.GoogleSignInService;
import com.pleasebookme.server.service.auth.service.SessionIssuer;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultGoogleSignInService implements GoogleSignInService {
    private final GoogleTokenVerifier googleTokenVerifier;
    private final GoogleAccountResolver googleAccountResolver;
    private final SessionIssuer sessionIssuer;

    @Override
    @Transactional
    public LoginResponse signIn(String idToken) {
        GoogleIdentity identity = googleTokenVerifier.verify(idToken);
        UserEntity user = googleAccountResolver.resolve(identity);

        return sessionIssuer.issue(user);
    }
}
