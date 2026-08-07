package com.pleasebookme.server.service.integration.service.impl;

import com.pleasebookme.server.auth.enums.AccountStatus;
import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import com.pleasebookme.server.integration.enums.OAuthConnectionStatus;
import com.pleasebookme.server.integration.enums.OAuthProvider;
import com.pleasebookme.server.integration.oauthconnection.entity.OAuthConnectionEntity;
import com.pleasebookme.server.integration.oauthconnection.repository.OAuthConnectionRepository;
import com.pleasebookme.server.integration.oauthstate.model.OAuthState;
import com.pleasebookme.server.integration.oauthstate.store.OAuthStateStore;
import com.pleasebookme.server.security.crypto.TokenCipher;
import com.pleasebookme.server.security.identity.enums.AuthenticatedActorType;
import com.pleasebookme.server.security.identity.principal.UserPrincipal;
import com.pleasebookme.server.security.oauth.google.authorization.GoogleAuthorizationUrlBuilder;
import com.pleasebookme.server.security.oauth.google.authorization.GoogleScope;
import com.pleasebookme.server.security.oauth.google.pkce.PkceChallenge;
import com.pleasebookme.server.security.oauth.google.pkce.PkceGenerator;
import com.pleasebookme.server.security.oauth.google.client.GoogleTokenClient;
import com.pleasebookme.server.security.oauth.google.client.dto.GoogleTokenResponse;
import com.pleasebookme.server.security.oauth.google.identity.GoogleIdentity;
import com.pleasebookme.server.security.oauth.google.verifier.GoogleTokenVerifier;
import com.pleasebookme.server.service.integration.dto.GoogleConnectRequest;
import com.pleasebookme.server.service.integration.exception.OAuthConnectionAccessDeniedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DefaultGoogleConnectServiceTest {

    private static final UUID USER_UID = UUID.randomUUID();

    @Mock private OAuthStateStore oauthStateStore;
    @Mock private PkceGenerator pkceGenerator;
    @Mock private GoogleAuthorizationUrlBuilder authorizationUrlBuilder;
    @Mock private GoogleTokenClient googleTokenClient;
    @Mock private GoogleTokenVerifier googleTokenVerifier;
    @Mock private TokenCipher tokenCipher;
    @Mock private UserRepository userRepository;
    @Mock private OAuthConnectionRepository oauthConnectionRepository;

    private DefaultGoogleConnectService service;
    private UserEntity user;

    private static UserPrincipal principal() {
        return new UserPrincipal(
            AuthenticatedActorType.USER,
            USER_UID, null, null, null, null,
            "jane", "jane@example.com", "Jane Doe", null,
            "Australia/Sydney", AccountStatus.ACTIVE, Set.of(), Set.of(), Map.of()
        );
    }

    private static GoogleTokenResponse tokens(String refreshToken, String scope) {
        return new GoogleTokenResponse(
            "ya29.access", refreshToken, "id.token.here", "Bearer", 3599L, scope
        );
    }

    @BeforeEach
    void setUp() {
        service = new DefaultGoogleConnectService(
            oauthStateStore,
            pkceGenerator,
            authorizationUrlBuilder,
            googleTokenClient,
            googleTokenVerifier,
            tokenCipher,
            userRepository,
            oauthConnectionRepository
        );
        ReflectionTestUtils.setField(service, "frontendUrl", "http://localhost:3000");

        user = UserEntity.builder().userUid(USER_UID).username("jane").build();
        user.setUserId(BigInteger.ONE);

        when(pkceGenerator.generate()).thenReturn(new PkceChallenge("verifier", "challenge"));
        when(oauthStateStore.issue(any(), any())).thenReturn("state-token");
        when(authorizationUrlBuilder.build(anyString(), any(), any()))
            .thenReturn("https://accounts.google.com/o/oauth2/v2/auth?state=state-token");
        when(userRepository.findByUserUid(USER_UID)).thenReturn(Optional.of(user));
        when(tokenCipher.encrypt(anyString())).thenAnswer(i -> "enc(" + i.getArgument(0) + ")");
        when(tokenCipher.currentKeyVersion()).thenReturn((short) 1);
    }

    @Test
    void initiate_storesStateBoundToTheCallerAndAlwaysRequestsOpenid() {
        service.initiate(principal(), new GoogleConnectRequest(List.of(GoogleScope.CALENDAR), null));

        ArgumentCaptor<OAuthState> stateCaptor = ArgumentCaptor.forClass(OAuthState.class);
        verify(oauthStateStore).issue(stateCaptor.capture(), eq(Duration.ofMinutes(10)));

        OAuthState state = stateCaptor.getValue();
        assertThat(state.userUid()).isEqualTo(USER_UID);
        assertThat(state.codeVerifier()).isEqualTo("verifier");
        // openid is what makes Google return an id_token, which the callback needs.
        assertThat(state.requestedScopes()).contains("openid");
        assertThat(state.requestedScopes()).contains(GoogleScope.CALENDAR.uri());
        assertThat(state.requestedScopes()).doesNotContain(GoogleScope.SHEETS.uri());
    }

    @Test
    void initiate_withNoScopes_requestsEveryAllowListedScope() {
        service.initiate(principal(), new GoogleConnectRequest(null, null));

        ArgumentCaptor<OAuthState> stateCaptor = ArgumentCaptor.forClass(OAuthState.class);
        verify(oauthStateStore).issue(stateCaptor.capture(), any());

        assertThat(stateCaptor.getValue().requestedScopes())
            .contains(
                GoogleScope.CALENDAR.uri(),
                GoogleScope.SHEETS.uri(),
                GoogleScope.DRIVE_FILE.uri()
            );
    }

    @Test
    void initiate_absoluteRedirectAfter_isRejectedToPreventOpenRedirect() {
        service.initiate(
            principal(),
            new GoogleConnectRequest(null, "https://evil.example.com/steal")
        );

        ArgumentCaptor<OAuthState> stateCaptor = ArgumentCaptor.forClass(OAuthState.class);
        verify(oauthStateStore).issue(stateCaptor.capture(), any());

        assertThat(stateCaptor.getValue().redirectAfter()).isEqualTo("/settings/integrations");
    }

    @Test
    void initiate_protocolRelativeRedirectAfter_isAlsoRejected() {
        service.initiate(principal(), new GoogleConnectRequest(null, "//evil.example.com"));

        ArgumentCaptor<OAuthState> stateCaptor = ArgumentCaptor.forClass(OAuthState.class);
        verify(oauthStateStore).issue(stateCaptor.capture(), any());

        assertThat(stateCaptor.getValue().redirectAfter()).isEqualTo("/settings/integrations");
    }

    @Test
    void complete_userDeniedConsent_redirectsWithoutTouchingGoogleOrTheDatabase() {
        URI redirect = service.complete(null, null, "access_denied");

        assertThat(redirect.toString()).contains("google=denied");
        verify(googleTokenClient, never()).exchangeAuthorizationCode(anyString(), anyString());
        verify(oauthConnectionRepository, never()).save(any());
    }

    @Test
    void complete_unknownOrReplayedState_isRejected() {
        when(oauthStateStore.consume("bad")).thenReturn(Optional.empty());

        URI redirect = service.complete("code", "bad", null);

        assertThat(redirect.toString()).contains("google=invalid_state");
        verify(googleTokenClient, never()).exchangeAuthorizationCode(anyString(), anyString());
        verify(oauthConnectionRepository, never()).save(any());
    }

    @Test
    void complete_happyPath_persistsEncryptedTokensAndGrantedScopes() {
        when(oauthStateStore.consume("state-token")).thenReturn(Optional.of(new OAuthState(
            USER_UID, "verifier", List.of("openid"), "/settings/integrations"
        )));
        when(googleTokenClient.exchangeAuthorizationCode("auth-code", "verifier"))
            .thenReturn(tokens(
                "1//refresh",
                "openid https://www.googleapis.com/auth/calendar"
            ));
        when(googleTokenVerifier.verify("id.token.here"))
            .thenReturn(new GoogleIdentity("google-sub", "jane@gmail.com", true, "Jane", null));
        when(oauthConnectionRepository.findByUserUserIdAndProviderAndProviderAccountId(
            any(), eq(OAuthProvider.GOOGLE), eq("google-sub")
        )).thenReturn(Optional.empty());

        URI redirect = service.complete("auth-code", "state-token", null);

        assertThat(redirect.toString())
            .isEqualTo("http://localhost:3000/settings/integrations?google=connected");

        ArgumentCaptor<OAuthConnectionEntity> captor =
            ArgumentCaptor.forClass(OAuthConnectionEntity.class);
        verify(oauthConnectionRepository).save(captor.capture());

        OAuthConnectionEntity saved = captor.getValue();
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
        assertThat(saved.getTokenExpiresAt()).isAfter(Instant.now().plusSeconds(3500));
    }

    @Test
    void complete_reconsentWithoutRefreshToken_keepsTheStoredOne() {
        OAuthConnectionEntity existing = OAuthConnectionEntity.builder()
            .user(user)
            .provider(OAuthProvider.GOOGLE)
            .providerAccountId("google-sub")
            .accessToken("enc(old-access)")
            .refreshToken("enc(original-refresh)")
            .scopes(new String[]{"openid"})
            .build();

        when(oauthStateStore.consume("state-token")).thenReturn(Optional.of(new OAuthState(
            USER_UID, "verifier", List.of("openid"), "/settings/integrations"
        )));
        // Google omits refresh_token on some re-consents.
        when(googleTokenClient.exchangeAuthorizationCode(anyString(), anyString()))
            .thenReturn(tokens(null, "openid https://www.googleapis.com/auth/spreadsheets"));
        when(googleTokenVerifier.verify(anyString()))
            .thenReturn(new GoogleIdentity("google-sub", "jane@gmail.com", true, "Jane", null));
        when(oauthConnectionRepository.findByUserUserIdAndProviderAndProviderAccountId(
            any(), eq(OAuthProvider.GOOGLE), eq("google-sub")
        )).thenReturn(Optional.of(existing));

        service.complete("auth-code", "state-token", null);

        ArgumentCaptor<OAuthConnectionEntity> captor =
            ArgumentCaptor.forClass(OAuthConnectionEntity.class);
        verify(oauthConnectionRepository).save(captor.capture());

        assertThat(captor.getValue().getRefreshToken()).isEqualTo("enc(original-refresh)");
        assertThat(captor.getValue().getAccessToken()).isEqualTo("enc(ya29.access)");
        // Previously granted scopes survive a narrower response.
        assertThat(captor.getValue().getScopes())
            .contains("openid", "https://www.googleapis.com/auth/spreadsheets");
    }

    @Test
    void complete_exchangeFailure_redirectsWithErrorInsteadOfThrowing() {
        when(oauthStateStore.consume("state-token")).thenReturn(Optional.of(new OAuthState(
            USER_UID, "verifier", List.of("openid"), "/settings/integrations"
        )));
        when(googleTokenClient.exchangeAuthorizationCode(anyString(), anyString()))
            .thenThrow(new RuntimeException("boom"));

        URI redirect = service.complete("auth-code", "state-token", null);

        assertThat(redirect.toString()).contains("google=error");
        verify(oauthConnectionRepository, never()).save(any());
    }

    @Test
    void disconnect_revokesAtGoogleThenMarksRevoked() {
        UUID connectionUid = UUID.randomUUID();
        OAuthConnectionEntity connection = OAuthConnectionEntity.builder()
            .user(user)
            .refreshToken("enc(refresh)")
            .tokenKeyVersion((short) 1)
            .status(OAuthConnectionStatus.ACTIVE)
            .build();

        when(oauthConnectionRepository.findByOauthConnectionUid(connectionUid))
            .thenReturn(Optional.of(connection));
        when(tokenCipher.decrypt("enc(refresh)", (short) 1)).thenReturn("1//refresh");

        service.disconnect(principal(), connectionUid);

        verify(googleTokenClient).revoke("1//refresh");
        assertThat(connection.getStatus()).isEqualTo(OAuthConnectionStatus.REVOKED);
        assertThat(connection.getRevokedAt()).isNotNull();
    }

    @Test
    void disconnect_someoneElsesConnection_isDeniedAndNotRevoked() {
        UUID connectionUid = UUID.randomUUID();
        UserEntity otherUser = UserEntity.builder().userUid(UUID.randomUUID()).build();
        otherUser.setUserId(BigInteger.valueOf(999));

        OAuthConnectionEntity connection = OAuthConnectionEntity.builder()
            .user(otherUser)
            .refreshToken("enc(refresh)")
            .tokenKeyVersion((short) 1)
            .build();

        when(oauthConnectionRepository.findByOauthConnectionUid(connectionUid))
            .thenReturn(Optional.of(connection));

        assertThatThrownBy(() -> service.disconnect(principal(), connectionUid))
            .isInstanceOf(OAuthConnectionAccessDeniedException.class);

        verify(googleTokenClient, never()).revoke(anyString());
        verify(oauthConnectionRepository, never()).save(any());
    }
}
