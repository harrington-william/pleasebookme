package com.pleasebookme.server.service.auth.service.impl;

import com.pleasebookme.server.auth.account.entity.AccountEntity;
import com.pleasebookme.server.auth.account.repository.AccountRepository;
import com.pleasebookme.server.auth.refreshtoken.entity.RefreshTokenEntity;
import com.pleasebookme.server.auth.refreshtoken.enums.RefreshOwner;
import com.pleasebookme.server.auth.refreshtoken.repository.RefreshTokenRepository;
import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import com.pleasebookme.server.security.identity.aggregation.AuthenticationAggregation;
import com.pleasebookme.server.security.identity.loader.user.UserIdentityLoader;
import com.pleasebookme.server.security.identity.mapper.UserPrincipalMapper;
import com.pleasebookme.server.security.identity.principal.UserPrincipal;
import com.pleasebookme.server.security.oauth.google.identity.GoogleIdentity;
import com.pleasebookme.server.security.oauth.google.verifier.GoogleTokenVerifier;
import com.pleasebookme.server.security.token.jwt.config.JwtProperties;
import com.pleasebookme.server.security.token.jwt.engine.JwtEngine;
import com.pleasebookme.server.service.auth.dto.LoginResponse;
import com.pleasebookme.server.service.auth.exception.GoogleAccountEmailNotVerifiedException;
import com.pleasebookme.server.service.auth.service.GoogleSignInService;
import com.pleasebookme.server.service.auth.service.UserProvisioningService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DefaultGoogleSignInService implements GoogleSignInService {
    private static final String GOOGLE_PROVIDER = "GOOGLE";

    private final GoogleTokenVerifier googleTokenVerifier;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final UserProvisioningService userProvisioningService;

    private final UserIdentityLoader userIdentityLoader;
    private final UserPrincipalMapper userPrincipalMapper;

    private final JwtEngine jwtEngine;
    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional
    public LoginResponse signIn(String idToken) {
        GoogleIdentity identity = googleTokenVerifier.verify(idToken);

        UserEntity user = accountRepository
            .findByProviderAndProviderAccountId(GOOGLE_PROVIDER, identity.sub())
            .map(AccountEntity::getUser)
            .orElseGet(() -> resolveOrProvisionUser(identity));

        AuthenticationAggregation aggregation = userIdentityLoader.loadByUsername(user.getUsername());
        UserPrincipal principal = userPrincipalMapper.map(aggregation);

        String accessToken = jwtEngine.issueAccessToken(principal);
        String refreshToken = jwtEngine.issueRefreshToken(principal);

        Instant now = Instant.now();
        RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.builder()
            .secret(refreshToken)
            .owner(RefreshOwner.USER)
            .user(user)
            .deviceName(null)
            .createdAt(now)
            .expiresAt(now.plusSeconds(
                jwtProperties.refreshTokenLifeTime().toSeconds()
            ))
            .build();
        refreshTokenRepository.save(refreshTokenEntity);

        return new LoginResponse(accessToken, refreshToken);
    }

    private UserEntity resolveOrProvisionUser(GoogleIdentity identity) {
        return userRepository.findByEmail(identity.email())
            .map(existingUser -> linkGoogleAccount(existingUser, identity))
            .orElseGet(() -> provisionAndLinkNewUser(identity));
    }

    private UserEntity linkGoogleAccount(UserEntity existingUser, GoogleIdentity identity) {
        if (!identity.emailVerified()) {
            throw new GoogleAccountEmailNotVerifiedException(
                "Cannot link unverified Google account: " + identity.email()
            );
        }

        AccountEntity account = AccountEntity.builder()
            .user(existingUser)
            .type("oauth")
            .provider(GOOGLE_PROVIDER)
            .providerAccountId(identity.sub())
            .providerEmail(identity.email())
            .build();
        accountRepository.save(account);

        return existingUser;
    }

    private UserEntity provisionAndLinkNewUser(GoogleIdentity identity) {
        String username = resolveAvailableUsername(identity.email());

        UserEntity user = userProvisioningService.provisionUser(
            username,
            identity.name() != null ? identity.name() : identity.email(),
            identity.email(),
            null,
            null,
            null
        );

        AccountEntity account = AccountEntity.builder()
            .user(user)
            .type("oauth")
            .provider(GOOGLE_PROVIDER)
            .providerAccountId(identity.sub())
            .providerEmail(identity.email())
            .build();
        accountRepository.save(account);

        return user;
    }

    private String resolveAvailableUsername(String email) {
        String base = email
            .substring(0, Math.max(email.indexOf('@'), 0))
            .toLowerCase()
            .replaceAll("[^a-z0-9]", "");

        if (base.isBlank()) {
            base = "user";
        }

        if (!userRepository.existsByUsername(base)) {
            return base;
        }

        return base + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
