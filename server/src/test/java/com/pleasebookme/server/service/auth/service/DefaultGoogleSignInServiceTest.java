package com.pleasebookme.server.service.auth.service;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.security.oauth.google.exception.InvalidGoogleIdTokenException;
import com.pleasebookme.server.security.oauth.google.identity.GoogleIdentity;
import com.pleasebookme.server.security.oauth.google.verifier.GoogleTokenVerifier;
import com.pleasebookme.server.service.auth.dto.LoginResponse;
import com.pleasebookme.server.service.auth.service.impl.DefaultGoogleSignInService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Account resolution lives in DefaultGoogleAccountResolver and is tested there.
// What is left here is the ordering contract: verify, then resolve, then issue -
// and specifically that a bad token never reaches the other two.
@ExtendWith(MockitoExtension.class)
class DefaultGoogleSignInServiceTest {

    @Mock private GoogleTokenVerifier googleTokenVerifier;
    @Mock private GoogleAccountResolver googleAccountResolver;
    @Mock private SessionIssuer sessionIssuer;

    private DefaultGoogleSignInService googleSignInService;

    @BeforeEach
    void setUp() {
        googleSignInService = new DefaultGoogleSignInService(
            googleTokenVerifier,
            googleAccountResolver,
            sessionIssuer
        );
    }

    @Test
    void signIn_verifiesResolvesThenIssues() {
        GoogleIdentity identity =
            new GoogleIdentity("sub-1", "user@example.com", true, "Jane Doe", null);
        when(googleTokenVerifier.verify("raw-token")).thenReturn(identity);

        UserEntity user = UserEntity.builder().username("jane").build();
        when(googleAccountResolver.resolve(identity)).thenReturn(user);
        when(sessionIssuer.issue(user))
            .thenReturn(new LoginResponse("access-token", "refresh-token"));

        LoginResponse response = googleSignInService.signIn("raw-token");

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");

        InOrder order = inOrder(googleTokenVerifier, googleAccountResolver, sessionIssuer);
        order.verify(googleTokenVerifier).verify("raw-token");
        order.verify(googleAccountResolver).resolve(identity);
        order.verify(sessionIssuer).issue(user);
    }

    @Test
    void signIn_invalidIdToken_neverResolvesAnAccountOrIssuesASession() {
        when(googleTokenVerifier.verify("forged"))
            .thenThrow(new InvalidGoogleIdTokenException("bad signature"));

        assertThatThrownBy(() -> googleSignInService.signIn("forged"))
            .isInstanceOf(InvalidGoogleIdTokenException.class);

        verify(googleAccountResolver, never()).resolve(any());
        verify(sessionIssuer, never()).issue(any());
    }
}
