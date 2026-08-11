package com.pleasebookme.server.service.integration.service.impl;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.integration.enums.OAuthConnectionStatus;
import com.pleasebookme.server.integration.enums.OAuthProvider;
import com.pleasebookme.server.integration.oauthconnection.entity.OAuthConnectionEntity;
import com.pleasebookme.server.integration.oauthconnection.repository.OAuthConnectionRepository;
import com.pleasebookme.server.security.crypto.TokenCipher;
import com.pleasebookme.server.security.oauth.google.client.dto.GoogleTokenResponse;
import com.pleasebookme.server.security.oauth.google.identity.GoogleIdentity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DefaultGoogleConnectionWriterTest {

    @Mock private OAuthConnectionRepository oauthConnectionRepository;
    @Mock private TokenCipher tokenCipher;

    private DefaultGoogleConnectionWriter writer;
    private UserEntity user;

    private static final GoogleIdentity IDENTITY =
        new GoogleIdentity("google-sub", "jane@gmail.com", true, "Jane", null);

    private static GoogleTokenResponse tokens(String refreshToken, String scope) {
        return new GoogleTokenResponse(
            "ya29.access", refreshToken, "id.token.here", "Bearer", 3599L, scope
        );
    }

    @BeforeEach
    void setUp() {
        writer = new DefaultGoogleConnectionWriter(oauthConnectionRepository, tokenCipher);

        user = UserEntity.builder().userUid(UUID.randomUUID()).username("jane").build();
        user.setUserId(BigInteger.ONE);

        when(tokenCipher.encrypt(anyString())).thenAnswer(i -> "enc(" + i.getArgument(0) + ")");
        when(tokenCipher.currentKeyVersion()).thenReturn((short) 1);
    }

    private OAuthConnectionEntity captureSaved() {
        ArgumentCaptor<OAuthConnectionEntity> captor =
            ArgumentCaptor.forClass(OAuthConnectionEntity.class);
        verify(oauthConnectionRepository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void persist_newConnection_encryptsTokensAndSplitsGrantedScopes() {
        when(oauthConnectionRepository.findByUserUserIdAndProviderAndProviderAccountId(
            any(), eq(OAuthProvider.GOOGLE), eq("google-sub")
        )).thenReturn(Optional.empty());

        writer.persist(
            user,
            tokens("1//refresh", "openid https://www.googleapis.com/auth/calendar"),
            IDENTITY
        );

        OAuthConnectionEntity saved = captureSaved();
        assertThat(saved.getProviderAccountId()).isEqualTo("google-sub");
        assertThat(saved.getProviderEmail()).isEqualTo("jane@gmail.com");
        assertThat(saved.getStatus()).isEqualTo(OAuthConnectionStatus.ACTIVE);
        assertThat(saved.getTokenKeyVersion()).isEqualTo((short) 1);
        // Never stored in the clear.
        assertThat(saved.getAccessToken()).isEqualTo("enc(ya29.access)");
        assertThat(saved.getRefreshToken()).isEqualTo("enc(1//refresh)");
        // Space-delimited scope string is split, not stored as one blob.
        assertThat(saved.getScopes())
            .containsExactlyInAnyOrder("openid", "https://www.googleapis.com/auth/calendar");
        // expires_in is relative seconds, not a timestamp.
        assertThat(saved.getTokenExpiresAt()).isAfter(Instant.now().plusSeconds(3500));
    }

    @Test
    void persist_reconsentWithoutRefreshToken_keepsTheStoredOne() {
        OAuthConnectionEntity existing = OAuthConnectionEntity.builder()
            .user(user)
            .provider(OAuthProvider.GOOGLE)
            .providerAccountId("google-sub")
            .accessToken("enc(old-access)")
            .refreshToken("enc(original-refresh)")
            .scopes(new String[]{"openid"})
            .build();

        when(oauthConnectionRepository.findByUserUserIdAndProviderAndProviderAccountId(
            any(), eq(OAuthProvider.GOOGLE), eq("google-sub")
        )).thenReturn(Optional.of(existing));

        // Google omits refresh_token on some re-consents.
        writer.persist(
            user,
            tokens(null, "openid https://www.googleapis.com/auth/spreadsheets"),
            IDENTITY
        );

        OAuthConnectionEntity saved = captureSaved();
        assertThat(saved.getRefreshToken()).isEqualTo("enc(original-refresh)");
        assertThat(saved.getAccessToken()).isEqualTo("enc(ya29.access)");
        // Previously granted scopes survive a narrower response.
        assertThat(saved.getScopes())
            .contains("openid", "https://www.googleapis.com/auth/spreadsheets");
    }

    @Test
    void persist_newConnectionWithoutRefreshToken_isSkippedRatherThanFailingTheTransaction() {
        // refresh_token is NOT NULL and there is no stored value to fall back on,
        // so writing would abort the transaction. During onboarding that same
        // transaction is what provisions the account, and losing a registration
        // is far worse than losing a calendar connection.
        when(oauthConnectionRepository.findByUserUserIdAndProviderAndProviderAccountId(
            any(), eq(OAuthProvider.GOOGLE), eq("google-sub")
        )).thenReturn(Optional.empty());

        writer.persist(user, tokens(null, "openid"), IDENTITY);

        verify(oauthConnectionRepository, never()).save(any());
    }

    @Test
    void persist_reconnectingARevokedConnection_clearsTheRevocation() {
        OAuthConnectionEntity existing = OAuthConnectionEntity.builder()
            .user(user)
            .provider(OAuthProvider.GOOGLE)
            .providerAccountId("google-sub")
            .status(OAuthConnectionStatus.REVOKED)
            .revokedAt(Instant.now())
            .scopes(new String[]{"openid"})
            .build();

        when(oauthConnectionRepository.findByUserUserIdAndProviderAndProviderAccountId(
            any(), eq(OAuthProvider.GOOGLE), eq("google-sub")
        )).thenReturn(Optional.of(existing));

        writer.persist(user, tokens("1//new-refresh", "openid"), IDENTITY);

        OAuthConnectionEntity saved = captureSaved();
        assertThat(saved.getStatus()).isEqualTo(OAuthConnectionStatus.ACTIVE);
        assertThat(saved.getRevokedAt()).isNull();
    }
}
