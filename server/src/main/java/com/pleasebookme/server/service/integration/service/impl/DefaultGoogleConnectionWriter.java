package com.pleasebookme.server.service.integration.service.impl;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.integration.enums.OAuthConnectionStatus;
import com.pleasebookme.server.integration.enums.OAuthProvider;
import com.pleasebookme.server.integration.oauthconnection.entity.OAuthConnectionEntity;
import com.pleasebookme.server.integration.oauthconnection.repository.OAuthConnectionRepository;
import com.pleasebookme.server.security.crypto.TokenCipher;
import com.pleasebookme.server.security.oauth.google.client.dto.GoogleTokenResponse;
import com.pleasebookme.server.security.oauth.google.identity.GoogleIdentity;
import com.pleasebookme.server.service.integration.service.GoogleConnectionWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultGoogleConnectionWriter implements GoogleConnectionWriter {
    private static final long FALLBACK_EXPIRES_IN = 3600L;

    private final OAuthConnectionRepository oauthConnectionRepository;
    private final TokenCipher tokenCipher;

    @Override
    public void persist(
        UserEntity user,
        GoogleTokenResponse tokens,
        GoogleIdentity identity
    ) {
        Optional<OAuthConnectionEntity> existing = oauthConnectionRepository
            .findByUserUserIdAndProviderAndProviderAccountId(
                user.getUserId(),
                OAuthProvider.GOOGLE,
                identity.sub()
            );

        boolean hasRefreshToken =
            tokens.refreshToken() != null && !tokens.refreshToken().isBlank();

        if (existing.isEmpty() && !hasRefreshToken) {
            log.warn(
                "Google returned no refresh token for a new connection; " +
                "skipping connection write for user {}",
                user.getUserUid()
            );
            return;
        }

        OAuthConnectionEntity connection = existing.orElseGet(
            () -> OAuthConnectionEntity.builder()
                .user(user)
                .provider(OAuthProvider.GOOGLE)
                .providerAccountId(identity.sub())
                .build()
        );

        connection.setProviderEmail(identity.email());
        connection.setAccessToken(tokenCipher.encrypt(tokens.accessToken()));

        if (hasRefreshToken) {
            connection.setRefreshToken(tokenCipher.encrypt(tokens.refreshToken()));
        }

        connection.setTokenKeyVersion(tokenCipher.currentKeyVersion());
        connection.setTokenExpiresAt(expiresAt(tokens));
        connection.setScopes(mergedScopes(connection, tokens));
        connection.setStatus(OAuthConnectionStatus.ACTIVE);
        connection.setRevokedAt(null);
        connection.setLastRefreshedAt(Instant.now());

        oauthConnectionRepository.save(connection);
    }

    private String[] mergedScopes(
        OAuthConnectionEntity connection,
        GoogleTokenResponse tokens
    ) {
        Set<String> scopes = new LinkedHashSet<>();

        if (connection.getScopes() != null) {
            scopes.addAll(Arrays.asList(connection.getScopes()));
        }

        scopes.addAll(tokens.grantedScopes());

        return scopes.toArray(String[]::new);
    }

    private Instant expiresAt(GoogleTokenResponse tokens) {
        long expiresIn = tokens.expiresIn() != null ? tokens.expiresIn() : FALLBACK_EXPIRES_IN;
        return Instant.now().plusSeconds(expiresIn);
    }
}
