package com.pleasebookme.server.service.auth.service;

import com.pleasebookme.server.auth.handoff.store.SessionHandoffStore;
import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.user.exception.UserNotFoundException;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import com.pleasebookme.server.security.oauth.google.client.dto.GoogleTokenResponse;
import com.pleasebookme.server.security.oauth.google.identity.GoogleIdentity;
import com.pleasebookme.server.service.auth.dto.LoginResponse;
import com.pleasebookme.server.service.auth.exception.InvalidSessionHandoffException;
import com.pleasebookme.server.service.auth.service.impl.DefaultGoogleOnboardingService;
import com.pleasebookme.server.service.integration.service.GoogleConnectionWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultGoogleOnboardingServiceTest {

    private static final UUID USER_UID = UUID.randomUUID();

    private static final GoogleIdentity IDENTITY =
        new GoogleIdentity("google-sub", "jane@gmail.com", true, "Jane", null);

    private static final GoogleTokenResponse TOKENS = new GoogleTokenResponse(
        "ya29.access", "1//refresh", "id.token.here", "Bearer", 3599L, "openid"
    );

    @Mock private GoogleAccountResolver googleAccountResolver;
    @Mock private GoogleConnectionWriter googleConnectionWriter;
    @Mock private SessionHandoffStore sessionHandoffStore;
    @Mock private SessionIssuer sessionIssuer;
    @Mock private UserRepository userRepository;

    private DefaultGoogleOnboardingService service;
    private UserEntity user;

    @BeforeEach
    void setUp() {
        service = new DefaultGoogleOnboardingService(
            googleAccountResolver,
            googleConnectionWriter,
            sessionHandoffStore,
            sessionIssuer,
            userRepository
        );

        user = UserEntity.builder().userUid(USER_UID).username("jane").build();
        user.setUserId(BigInteger.ONE);
    }

    @Test
    void finalizeOnboarding_resolvesThenWritesThenIssuesAShortLivedHandoff() {
        when(googleAccountResolver.resolve(IDENTITY)).thenReturn(user);
        when(sessionHandoffStore.issue(eq(USER_UID), any())).thenReturn("handoff-code");

        assertThat(service.finalizeOnboarding(TOKENS, IDENTITY)).isEqualTo("handoff-code");

        InOrder order = inOrder(googleAccountResolver, googleConnectionWriter, sessionHandoffStore);
        order.verify(googleAccountResolver).resolve(IDENTITY);
        order.verify(googleConnectionWriter).persist(user, TOKENS, IDENTITY);
        // Issued last: a Redis key is not rolled back with the transaction, so it
        // must not exist before the database work that backs it has succeeded.
        order.verify(sessionHandoffStore).issue(USER_UID, Duration.ofSeconds(60));
    }

    @Test
    void finalizeOnboarding_mintsNoTokens() {
        // The JWT pair is minted at exchange time instead, so the code sitting in
        // the URL bar is a lookup key rather than a bearer credential - and the
        // 15-minute access token starts when the session actually begins.
        when(googleAccountResolver.resolve(IDENTITY)).thenReturn(user);
        when(sessionHandoffStore.issue(any(), any())).thenReturn("handoff-code");

        service.finalizeOnboarding(TOKENS, IDENTITY);

        verify(sessionIssuer, never()).issue(any());
    }

    @Test
    void finalizeOnboarding_provisioningFailure_issuesNoHandoff() {
        when(googleAccountResolver.resolve(IDENTITY))
            .thenThrow(new RuntimeException("tenant provisioning failed"));

        assertThatThrownBy(() -> service.finalizeOnboarding(TOKENS, IDENTITY))
            .isInstanceOf(RuntimeException.class);

        verify(googleConnectionWriter, never()).persist(any(), any(), any());
        verify(sessionHandoffStore, never()).issue(any(), any());
    }

    @Test
    void exchangeHandoff_consumesTheCodeAndIssuesASession() {
        when(sessionHandoffStore.consume("handoff-code")).thenReturn(Optional.of(USER_UID));
        when(userRepository.findByUserUid(USER_UID)).thenReturn(Optional.of(user));
        when(sessionIssuer.issue(user))
            .thenReturn(new LoginResponse("access-token", "refresh-token"));

        LoginResponse response = service.exchangeHandoff("handoff-code");

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        verify(sessionHandoffStore).consume("handoff-code");
    }

    @Test
    void exchangeHandoff_unknownExpiredOrReplayedCode_isRejectedIdentically() {
        // GETDEL cannot tell these three apart, and neither should the caller.
        when(sessionHandoffStore.consume("spent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.exchangeHandoff("spent"))
            .isInstanceOf(InvalidSessionHandoffException.class);

        verify(sessionIssuer, never()).issue(any());
    }

    @Test
    void exchangeHandoff_codePointingAtAMissingUser_issuesNoSession() {
        when(sessionHandoffStore.consume("handoff-code")).thenReturn(Optional.of(USER_UID));
        when(userRepository.findByUserUid(USER_UID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.exchangeHandoff("handoff-code"))
            .isInstanceOf(UserNotFoundException.class);

        verify(sessionIssuer, never()).issue(any());
    }
}
