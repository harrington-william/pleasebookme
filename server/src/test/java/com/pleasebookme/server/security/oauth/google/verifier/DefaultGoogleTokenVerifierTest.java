package com.pleasebookme.server.security.oauth.google.verifier;

import com.pleasebookme.server.security.oauth.google.exception.InvalidGoogleIdTokenException;
import com.pleasebookme.server.security.oauth.google.identity.GoogleIdentity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultGoogleTokenVerifierTest {

    @Mock
    private JwtDecoder googleIdTokenDecoder;

    private DefaultGoogleTokenVerifier googleTokenVerifier;

    private static Jwt.Builder validJwtBuilder() {
        return Jwt.withTokenValue("raw-token")
            .header("alg", "RS256")
            .claim("sub", "google-sub-123")
            .claim("email", "user@example.com")
            .claim("email_verified", true)
            .claim("name", "Jane Doe")
            .claim("picture", "https://example.com/pic.jpg");
    }

    @BeforeEach
    void setUp() {
        googleTokenVerifier = new DefaultGoogleTokenVerifier(googleIdTokenDecoder);
    }

    @Test
    void verify_validToken_returnsGoogleIdentity() {
        when(googleIdTokenDecoder.decode("raw-token")).thenReturn(validJwtBuilder().build());

        GoogleIdentity identity = googleTokenVerifier.verify("raw-token");

        assertThat(identity.sub()).isEqualTo("google-sub-123");
        assertThat(identity.email()).isEqualTo("user@example.com");
        assertThat(identity.emailVerified()).isTrue();
        assertThat(identity.name()).isEqualTo("Jane Doe");
        assertThat(identity.pictureUrl()).isEqualTo("https://example.com/pic.jpg");
    }

    @Test
    void verify_decoderRejectsToken_throwsInvalidGoogleIdTokenException() {
        when(googleIdTokenDecoder.decode("bad-token"))
            .thenThrow(new BadJwtException("signature invalid"));

        assertThatThrownBy(() -> googleTokenVerifier.verify("bad-token"))
            .isInstanceOf(InvalidGoogleIdTokenException.class);
    }

    @Test
    void verify_missingSubClaim_throwsInvalidGoogleIdTokenException() {
        Jwt jwt = Jwt.withTokenValue("raw-token")
            .header("alg", "RS256")
            .claim("email", "user@example.com")
            .build();
        when(googleIdTokenDecoder.decode("raw-token")).thenReturn(jwt);

        assertThatThrownBy(() -> googleTokenVerifier.verify("raw-token"))
            .isInstanceOf(InvalidGoogleIdTokenException.class);
    }

    @Test
    void verify_missingEmailVerifiedClaim_defaultsToFalse() {
        Jwt jwt = Jwt.withTokenValue("raw-token")
            .header("alg", "RS256")
            .claim("sub", "google-sub-123")
            .claim("email", "user@example.com")
            .build();
        when(googleIdTokenDecoder.decode("raw-token")).thenReturn(jwt);

        GoogleIdentity identity = googleTokenVerifier.verify("raw-token");

        assertThat(identity.emailVerified()).isFalse();
    }
}
