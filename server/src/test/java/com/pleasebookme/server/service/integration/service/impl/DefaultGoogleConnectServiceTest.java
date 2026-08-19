package com.pleasebookme.server.service.integration.service.impl;

import com.pleasebookme.server.auth.enums.AccountStatus;
import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import com.pleasebookme.server.integration.enums.OAuthConnectionStatus;
import com.pleasebookme.server.integration.oauthconnection.entity.OAuthConnectionEntity;
import com.pleasebookme.server.integration.oauthconnection.repository.OAuthConnectionRepository;
import com.pleasebookme.server.integration.oauthstate.model.OAuthFlowMode;
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
import com.pleasebookme.server.service.auth.service.GoogleOnboardingService;
import com.pleasebookme.server.service.integration.dto.GoogleConnectRequest;
import com.pleasebookme.server.service.integration.exception.OAuthConnectionAccessDeniedException;
import com.pleasebookme.server.service.integration.service.GoogleConnectionWriter;
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
    private static final String INTEGRATIONS_PATH = "/dashboard/settings/integrations";

    @Mock private OAuthStateStore oauthStateStore;
    @Mock private PkceGenerator pkceGenerator;
    @Mock private GoogleAuthorizationUrlBuilder authorizationUrlBuilder;
    @Mock private GoogleTokenClient googleTokenClient;
    @Mock private GoogleTokenVerifier googleTokenVerifier;
    @Mock private TokenCipher tokenCipher;
    @Mock private UserRepository userRepository;
    @Mock private OAuthConnectionRepository oauthConnectionRepository;
    @Mock private GoogleConnectionWriter googleConnectionWriter;
    @Mock private GoogleOnboardingService googleOnboardingService;

    private DefaultGoogleConnectService service;
    private UserEntity user;

    private static UserPrincipal principal() {
        return new UserPrincipal(
            AuthenticatedActorType.USER,
            USER_UID, null,
            "jane", "jane@example.com", "Jane Doe", null,
            "Australia/Sydney", AccountStatus.ACTIVE, Set.of(), Set.of(), Map.of()
        );
    }

    private static GoogleTokenResponse tokens(String refreshToken, String scope) {
        return new GoogleTokenResponse(
            "ya29.access", refreshToken, "id.token.here", "Bearer", 3599L, scope
        );
    }

    private static OAuthState connectState(String redirectAfter) {
        return new OAuthState(
            OAuthFlowMode.CONNECT, USER_UID, "verifier", List.of("openid"), redirectAfter
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
            oauthConnectionRepository,
            googleConnectionWriter,
            googleOnboardingService
        );
        ReflectionTestUtils.setField(service, "frontendUrl", "http://localhost:3000");

        user = UserEntity.builder().userUid(USER_UID).username("jane").build();
        user.setUserId(BigInteger.ONE);

        when(pkceGenerator.generate()).thenReturn(new PkceChallenge("verifier", "challenge"));
        when(oauthStateStore.issue(any(), any())).thenReturn("state-token");
        when(authorizationUrlBuilder.build(anyString(), any(), any()))
            .thenReturn("https://accounts.google.com/o/oauth2/v2/auth?state=state-token");
        when(userRepository.findByUserUid(USER_UID)).thenReturn(Optional.of(user));
    }

    private OAuthState capturedState() {
        ArgumentCaptor<OAuthState> stateCaptor = ArgumentCaptor.forClass(OAuthState.class);
        verify(oauthStateStore).issue(stateCaptor.capture(), any());
        return stateCaptor.getValue();
    }

    @Test
    void initiate_storesStateBoundToTheCallerAndAlwaysRequestsOpenid() {
        service.initiate(principal(), new GoogleConnectRequest(List.of(GoogleScope.CALENDAR), null));

        ArgumentCaptor<OAuthState> stateCaptor = ArgumentCaptor.forClass(OAuthState.class);
        verify(oauthStateStore).issue(stateCaptor.capture(), eq(Duration.ofMinutes(10)));

        OAuthState state = stateCaptor.getValue();
        assertThat(state.mode()).isEqualTo(OAuthFlowMode.CONNECT);
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

        assertThat(capturedState().requestedScopes())
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

        assertThat(capturedState().redirectAfter()).isEqualTo(INTEGRATIONS_PATH);
    }

    @Test
    void initiate_protocolRelativeRedirectAfter_isAlsoRejected() {
        service.initiate(principal(), new GoogleConnectRequest(null, "//evil.example.com"));

        assertThat(capturedState().redirectAfter()).isEqualTo(INTEGRATIONS_PATH);
    }

    @Test
    void initiateOnboarding_bindsNoUserAndRequestsEveryScope() {
        service.initiateOnboarding("/google/complete");

        OAuthState state = capturedState();
        assertThat(state.mode()).isEqualTo(OAuthFlowMode.SIGN_UP_AND_CONNECT);
        // Nobody is logged in yet; the user is resolved from the Google identity.
        assertThat(state.userUid()).isNull();
        // This is the user's only consent screen, so ask for everything.
        assertThat(state.requestedScopes())
            .contains(
                "openid",
                GoogleScope.CALENDAR.uri(),
                GoogleScope.SHEETS.uri(),
                GoogleScope.DRIVE_FILE.uri()
            );
        assertThat(state.redirectAfter()).isEqualTo("/google/complete");
    }

    @Test
    void initiateOnboarding_withNoRedirect_fallsBackToAPublicRouteNotTheDashboard() {
        service.initiateOnboarding(null);

        // The callback returns before any session cookie exists, so a /dashboard
        // fallback would be bounced by the frontend's route guard and the handoff
        // code silently discarded.
        assertThat(capturedState().redirectAfter()).isEqualTo("/google/complete");
    }

    @Test
    void initiateOnboarding_absoluteRedirectAfter_isRejectedToPreventOpenRedirect() {
        service.initiateOnboarding("https://evil.example.com/steal");

        assertThat(capturedState().redirectAfter()).isEqualTo("/google/complete");
    }

    @Test
    void complete_userDeniedConsent_redirectsWithoutTouchingGoogleOrTheDatabase() {
        URI redirect = service.complete(null, null, "access_denied");

        assertThat(redirect.toString()).contains("google=denied");
        verify(googleTokenClient, never()).exchangeAuthorizationCode(anyString(), anyString());
        verify(googleConnectionWriter, never()).persist(any(), any(), any());
    }

    @Test
    void complete_deniedConsent_stillSpendsTheStateAndReturnsWhereTheUserCameFrom() {
        // Google sends state on error callbacks too. It has to be consumed so it
        // cannot be replayed, and it carries the only record of the origin page.
        when(oauthStateStore.consume("state-token"))
            .thenReturn(Optional.of(connectState("/google/complete")));

        URI redirect = service.complete(null, "state-token", "access_denied");

        verify(oauthStateStore).consume("state-token");
        assertThat(redirect.toString())
            .isEqualTo("http://localhost:3000/google/complete?google=denied");
    }

    @Test
    void complete_unknownOrReplayedState_isRejected() {
        when(oauthStateStore.consume("bad")).thenReturn(Optional.empty());

        URI redirect = service.complete("code", "bad", null);

        assertThat(redirect.toString()).contains("google=invalid_state");
        verify(googleTokenClient, never()).exchangeAuthorizationCode(anyString(), anyString());
        verify(googleConnectionWriter, never()).persist(any(), any(), any());
    }

    @Test
    void complete_connectMode_resolvesTheStatesUserAndWritesTheConnection() {
        when(oauthStateStore.consume("state-token"))
            .thenReturn(Optional.of(connectState(INTEGRATIONS_PATH)));
        GoogleTokenResponse tokens = tokens(
            "1//refresh", "openid https://www.googleapis.com/auth/calendar"
        );
        when(googleTokenClient.exchangeAuthorizationCode("auth-code", "verifier"))
            .thenReturn(tokens);
        GoogleIdentity identity =
            new GoogleIdentity("google-sub", "jane@gmail.com", true, "Jane", null);
        when(googleTokenVerifier.verify("id.token.here")).thenReturn(identity);

        URI redirect = service.complete("auth-code", "state-token", null);

        assertThat(redirect.toString())
            .isEqualTo("http://localhost:3000" + INTEGRATIONS_PATH + "?google=connected");
        verify(googleConnectionWriter).persist(user, tokens, identity);
        verify(googleOnboardingService, never()).finalizeOnboarding(any(), any());
    }

    @Test
    void complete_signUpMode_onboardsAndCarriesAHandoffCodeBack() {
        when(oauthStateStore.consume("state-token")).thenReturn(Optional.of(new OAuthState(
            OAuthFlowMode.SIGN_UP_AND_CONNECT,
            // No user yet - this is the whole point of the mode.
            null,
            "verifier",
            List.of("openid"),
            "/google/complete"
        )));
        GoogleTokenResponse tokens = tokens("1//refresh", "openid");
        when(googleTokenClient.exchangeAuthorizationCode("auth-code", "verifier"))
            .thenReturn(tokens);
        GoogleIdentity identity =
            new GoogleIdentity("google-sub", "jane@gmail.com", true, "Jane", null);
        when(googleTokenVerifier.verify("id.token.here")).thenReturn(identity);
        when(googleOnboardingService.finalizeOnboarding(tokens, identity))
            .thenReturn("handoff-code");

        URI redirect = service.complete("auth-code", "state-token", null);

        assertThat(redirect.toString())
            .isEqualTo("http://localhost:3000/google/complete?google=connected&handoff=handoff-code");
        // Onboarding owns the whole write, inside one transaction.
        verify(googleConnectionWriter, never()).persist(any(), any(), any());
        verify(userRepository, never()).findByUserUid(any());
    }

    @Test
    void complete_stateWrittenBeforeModeExisted_takesTheConnectPath() {
        // A null mode means the state predates the field. It still has a userUid,
        // so it must finish as a connect rather than trying to onboard.
        when(oauthStateStore.consume("state-token")).thenReturn(Optional.of(new OAuthState(
            null, USER_UID, "verifier", List.of("openid"), INTEGRATIONS_PATH
        )));
        when(googleTokenClient.exchangeAuthorizationCode(anyString(), anyString()))
            .thenReturn(tokens("1//refresh", "openid"));
        when(googleTokenVerifier.verify(anyString()))
            .thenReturn(new GoogleIdentity("google-sub", "jane@gmail.com", true, "Jane", null));

        URI redirect = service.complete("auth-code", "state-token", null);

        assertThat(redirect.toString()).contains("google=connected");
        verify(googleConnectionWriter).persist(eq(user), any(), any());
        verify(googleOnboardingService, never()).finalizeOnboarding(any(), any());
    }

    @Test
    void complete_exchangeFailure_redirectsWithErrorInsteadOfThrowing() {
        when(oauthStateStore.consume("state-token"))
            .thenReturn(Optional.of(connectState(INTEGRATIONS_PATH)));
        when(googleTokenClient.exchangeAuthorizationCode(anyString(), anyString()))
            .thenThrow(new RuntimeException("boom"));

        URI redirect = service.complete("auth-code", "state-token", null);

        assertThat(redirect.toString()).contains("google=error");
        verify(googleConnectionWriter, never()).persist(any(), any(), any());
    }

    @Test
    void complete_onboardingFailure_redirectsWithErrorAndNoHandoff() {
        when(oauthStateStore.consume("state-token")).thenReturn(Optional.of(new OAuthState(
            OAuthFlowMode.SIGN_UP_AND_CONNECT, null, "verifier", List.of("openid"), "/google/complete"
        )));
        when(googleTokenClient.exchangeAuthorizationCode(anyString(), anyString()))
            .thenReturn(tokens("1//refresh", "openid"));
        when(googleTokenVerifier.verify(anyString()))
            .thenReturn(new GoogleIdentity("google-sub", "jane@gmail.com", true, "Jane", null));
        // e.g. provisioning blew up on a NOT NULL column.
        when(googleOnboardingService.finalizeOnboarding(any(), any()))
            .thenThrow(new RuntimeException("provisioning failed"));

        URI redirect = service.complete("auth-code", "state-token", null);

        assertThat(redirect.toString())
            .isEqualTo("http://localhost:3000/google/complete?google=error");
        assertThat(redirect.toString()).doesNotContain("handoff");
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
