package com.pleasebookme.server.service.integration.service.impl;

import com.pleasebookme.server.integration.enums.OAuthConnectionStatus;
import com.pleasebookme.server.integration.oauthconnection.entity.OAuthConnectionEntity;
import com.pleasebookme.server.integration.oauthconnection.exception.OAuthConnectionNotFoundException;
import com.pleasebookme.server.integration.oauthconnection.repository.OAuthConnectionRepository;
import com.pleasebookme.server.security.crypto.TokenCipher;
import com.pleasebookme.server.security.oauth.google.client.GoogleTokenClient;
import com.pleasebookme.server.security.oauth.google.client.dto.GoogleTokenResponse;
import com.pleasebookme.server.security.oauth.google.exception.GoogleTokenRefreshException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DefaultGoogleAccessTokenProviderTest {

    private static final BigInteger CONNECTION_ID = BigInteger.ONE;

    @Mock private OAuthConnectionRepository oauthConnectionRepository;
    @Mock private GoogleTokenClient googleTokenClient;
    @Mock private TokenCipher tokenCipher;

    @InjectMocks private DefaultGoogleAccessTokenProvider provider;

    private static OAuthConnectionEntity connection(Instant expiresAt) {
        return OAuthConnectionEntity.builder()
            .accessToken("enc(access)")
            .refreshToken("enc(refresh)")
            .tokenKeyVersion((short) 1)
            .tokenExpiresAt(expiresAt)
            .status(OAuthConnectionStatus.ACTIVE)
            .build();
    }

    @BeforeEach
    void setUp() {
        when(tokenCipher.decrypt("enc(access)", (short) 1)).thenReturn("ya29.access");
        when(tokenCipher.decrypt("enc(refresh)", (short) 1)).thenReturn("1//refresh");
        when(tokenCipher.encrypt(anyString())).thenAnswer(i -> "enc(" + i.getArgument(0) + ")");
        when(tokenCipher.currentKeyVersion()).thenReturn((short) 1);
    }

    @Test
    void accessTokenFor_freshToken_decryptsWithoutCallingGoogle() {
        OAuthConnectionEntity connection = connection(Instant.now().plusSeconds(600));
        when(oauthConnectionRepository.findById(CONNECTION_ID)).thenReturn(Optional.of(connection));

        assertThat(provider.accessTokenFor(CONNECTION_ID)).isEqualTo("ya29.access");

        verify(googleTokenClient, never()).refreshAccessToken(anyString());
        assertThat(connection.getLastUsedAt()).isNotNull();
    }

    @Test
    void accessTokenFor_expiredToken_refreshesAndReEncrypts() {
        OAuthConnectionEntity connection = connection(Instant.now().minusSeconds(10));
        when(oauthConnectionRepository.findById(CONNECTION_ID)).thenReturn(Optional.of(connection));
        when(googleTokenClient.refreshAccessToken("1//refresh")).thenReturn(
            new GoogleTokenResponse("ya29.new", null, null, "Bearer", 3599L, null)
        );

        assertThat(provider.accessTokenFor(CONNECTION_ID)).isEqualTo("ya29.new");

        assertThat(connection.getAccessToken()).isEqualTo("enc(ya29.new)");
        // Google does not reissue a refresh token on refresh; the stored one stands.
        assertThat(connection.getRefreshToken()).isEqualTo("enc(refresh)");
        assertThat(connection.getTokenExpiresAt()).isAfter(Instant.now().plusSeconds(3500));
        assertThat(connection.getLastRefreshedAt()).isNotNull();
    }

    @Test
    void accessTokenFor_tokenInsideSkewWindow_isTreatedAsExpired() {
        // 30s left: valid now, but expired by the time the API call lands.
        OAuthConnectionEntity connection = connection(Instant.now().plusSeconds(30));
        when(oauthConnectionRepository.findById(CONNECTION_ID)).thenReturn(Optional.of(connection));
        when(googleTokenClient.refreshAccessToken("1//refresh")).thenReturn(
            new GoogleTokenResponse("ya29.new", null, null, "Bearer", 3599L, null)
        );

        assertThat(provider.accessTokenFor(CONNECTION_ID)).isEqualTo("ya29.new");

        verify(googleTokenClient).refreshAccessToken("1//refresh");
    }

    @Test
    void accessTokenFor_invalidGrantOnRefresh_marksConnectionRevoked() {
        OAuthConnectionEntity connection = connection(Instant.now().minusSeconds(10));
        when(oauthConnectionRepository.findById(CONNECTION_ID)).thenReturn(Optional.of(connection));
        when(googleTokenClient.refreshAccessToken("1//refresh")).thenThrow(
            new GoogleTokenRefreshException("invalid_grant", true)
        );

        assertThatThrownBy(() -> provider.accessTokenFor(CONNECTION_ID))
            .isInstanceOf(GoogleTokenRefreshException.class);

        assertThat(connection.getStatus()).isEqualTo(OAuthConnectionStatus.REVOKED);
        assertThat(connection.getRevokedAt()).isNotNull();
    }

    @Test
    void accessTokenFor_transientRefreshFailure_doesNotRevoke() {
        OAuthConnectionEntity connection = connection(Instant.now().minusSeconds(10));
        when(oauthConnectionRepository.findById(CONNECTION_ID)).thenReturn(Optional.of(connection));
        when(googleTokenClient.refreshAccessToken("1//refresh")).thenThrow(
            new GoogleTokenRefreshException("503 from Google", false)
        );

        assertThatThrownBy(() -> provider.accessTokenFor(CONNECTION_ID))
            .isInstanceOf(GoogleTokenRefreshException.class);

        // A transient upstream failure must not burn the connection.
        assertThat(connection.getStatus()).isEqualTo(OAuthConnectionStatus.ACTIVE);
        assertThat(connection.getRevokedAt()).isNull();
    }

    @Test
    void accessTokenFor_revokedConnection_isRejectedWithoutCallingGoogle() {
        OAuthConnectionEntity connection = connection(Instant.now().plusSeconds(600));
        connection.setStatus(OAuthConnectionStatus.REVOKED);
        when(oauthConnectionRepository.findById(CONNECTION_ID)).thenReturn(Optional.of(connection));

        assertThatThrownBy(() -> provider.accessTokenFor(CONNECTION_ID))
            .isInstanceOf(GoogleTokenRefreshException.class);

        verify(googleTokenClient, never()).refreshAccessToken(anyString());
    }

    @Test
    void accessTokenFor_unknownConnection_throwsNotFound() {
        when(oauthConnectionRepository.findById(CONNECTION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> provider.accessTokenFor(CONNECTION_ID))
            .isInstanceOf(OAuthConnectionNotFoundException.class);
    }
}
