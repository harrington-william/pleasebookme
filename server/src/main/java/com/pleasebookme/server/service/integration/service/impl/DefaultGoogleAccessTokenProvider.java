package com.pleasebookme.server.service.integration.service.impl;

import com.pleasebookme.server.integration.enums.OAuthConnectionStatus;
import com.pleasebookme.server.integration.oauthconnection.entity.OAuthConnectionEntity;
import com.pleasebookme.server.integration.oauthconnection.exception.OAuthConnectionNotFoundException;
import com.pleasebookme.server.integration.oauthconnection.repository.OAuthConnectionRepository;
import com.pleasebookme.server.security.crypto.TokenCipher;
import com.pleasebookme.server.security.oauth.google.client.GoogleTokenClient;
import com.pleasebookme.server.security.oauth.google.client.dto.GoogleTokenResponse;
import com.pleasebookme.server.security.oauth.google.exception.GoogleTokenRefreshException;
import com.pleasebookme.server.service.integration.service.GoogleAccessTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultGoogleAccessTokenProvider implements GoogleAccessTokenProvider {
    // A token with three seconds left is already expired by the time the API
    // call it was fetched for actually lands.
    private static final Duration EXPIRY_SKEW = Duration.ofSeconds(60);

    private final OAuthConnectionRepository oauthConnectionRepository;
    private final GoogleTokenClient googleTokenClient;
    private final TokenCipher tokenCipher;

    @Override
    public String accessTokenFor(BigInteger oauthConnectionId) {
        OAuthConnectionEntity connection = oauthConnectionRepository
            .findById(oauthConnectionId)
            .orElseThrow(() -> new OAuthConnectionNotFoundException(
                "OAuth connection not found: " + oauthConnectionId
            ));

        if (connection.getStatus() != OAuthConnectionStatus.ACTIVE) {
            throw new GoogleTokenRefreshException(
                "OAuth connection " + oauthConnectionId + " is " + connection.getStatus(),
                true
            );
        }

        String accessToken = isFresh(connection)
            ? tokenCipher.decrypt(connection.getAccessToken(), connection.getTokenKeyVersion())
            : refresh(connection);

        connection.setLastUsedAt(Instant.now());
        oauthConnectionRepository.save(connection);

        return accessToken;
    }

    private boolean isFresh(OAuthConnectionEntity connection) {
        return connection.getTokenExpiresAt() != null
            && connection.getTokenExpiresAt().isAfter(Instant.now().plus(EXPIRY_SKEW));
    }

    private String refresh(OAuthConnectionEntity connection) {
        String refreshToken = tokenCipher.decrypt(
            connection.getRefreshToken(),
            connection.getTokenKeyVersion()
        );

        GoogleTokenResponse tokens;

        try {
            tokens = googleTokenClient.refreshAccessToken(refreshToken);
        } catch (GoogleTokenRefreshException exception) {
            if (exception.isInvalidGrant()) {
                // The user revoked access, changed their password, or the grant
                // expired. Terminal: the connection needs re-consent, not a retry.
                log.warn(
                    "Google refresh token for connection {} is no longer valid; marking REVOKED",
                    connection.getOauthConnectionId()
                );

                connection.setStatus(OAuthConnectionStatus.REVOKED);
                connection.setRevokedAt(Instant.now());
                oauthConnectionRepository.save(connection);
            }

            throw exception;
        }

        short keyVersion = tokenCipher.currentKeyVersion();

        connection.setAccessToken(tokenCipher.encrypt(tokens.accessToken()));

        // Google normally does not reissue a refresh token on refresh.
        if (tokens.refreshToken() != null && !tokens.refreshToken().isBlank()) {
            connection.setRefreshToken(tokenCipher.encrypt(tokens.refreshToken()));
        }

        connection.setTokenKeyVersion(keyVersion);
        connection.setTokenExpiresAt(
            Instant.now().plusSeconds(tokens.expiresIn() != null ? tokens.expiresIn() : 3600L)
        );
        connection.setLastRefreshedAt(Instant.now());

        return tokens.accessToken();
    }
}
