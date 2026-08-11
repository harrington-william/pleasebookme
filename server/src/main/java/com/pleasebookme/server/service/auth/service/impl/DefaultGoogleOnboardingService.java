package com.pleasebookme.server.service.auth.service.impl;

import com.pleasebookme.server.auth.handoff.store.SessionHandoffStore;
import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.user.exception.UserNotFoundException;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import com.pleasebookme.server.security.oauth.google.client.dto.GoogleTokenResponse;
import com.pleasebookme.server.security.oauth.google.identity.GoogleIdentity;
import com.pleasebookme.server.service.auth.dto.LoginResponse;
import com.pleasebookme.server.service.auth.exception.InvalidSessionHandoffException;
import com.pleasebookme.server.service.auth.service.GoogleAccountResolver;
import com.pleasebookme.server.service.auth.service.GoogleOnboardingService;
import com.pleasebookme.server.service.auth.service.SessionIssuer;
import com.pleasebookme.server.service.integration.service.GoogleConnectionWriter;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DefaultGoogleOnboardingService implements GoogleOnboardingService {
    private static final Duration HANDOFF_TTL = Duration.ofSeconds(60);

    private final GoogleAccountResolver googleAccountResolver;
    private final GoogleConnectionWriter googleConnectionWriter;
    private final SessionHandoffStore sessionHandoffStore;
    private final SessionIssuer sessionIssuer;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public String finalizeOnboarding(
        GoogleTokenResponse tokens,
        GoogleIdentity identity
    ) {
        UserEntity user = googleAccountResolver.resolve(identity);

        googleConnectionWriter.persist(user, tokens, identity);

        return sessionHandoffStore.issue(user.getUserUid(), HANDOFF_TTL);
    }

    @Override
    @Transactional
    public LoginResponse exchangeHandoff(String code) {
        UUID userUid = sessionHandoffStore.consume(code)
            .orElseThrow(() -> new InvalidSessionHandoffException(
                "Session handoff code is invalid or has already been used"
            ));

        UserEntity user = userRepository
            .findByUserUid(userUid)
            .orElseThrow(() -> new UserNotFoundException(
                "User not found: " + userUid
            ));

        return sessionIssuer.issue(user);
    }
}
