package com.pleasebookme.server.security.oauth.google.authorization;

import com.pleasebookme.server.security.oauth.google.pkce.PkceChallenge;
import com.pleasebookme.server.security.oauth.google.pkce.PkceGenerator;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class PkceGeneratorTest {

    private final PkceGenerator generator = new PkceGenerator();

    @Test
    void generate_verifierIsWithinRfc7636LengthAndCharset() {
        PkceChallenge challenge = generator.generate();

        assertThat(challenge.verifier().length()).isBetween(43, 128);
        assertThat(challenge.verifier()).matches("[A-Za-z0-9\\-._~]+");
    }

    @Test
    void generate_challengeIsBase64UrlSha256OfVerifier() throws Exception {
        PkceChallenge challenge = generator.generate();

        byte[] digest = MessageDigest.getInstance("SHA-256")
            .digest(challenge.verifier().getBytes(StandardCharsets.US_ASCII));
        String expected = Base64.getUrlEncoder().withoutPadding().encodeToString(digest);

        assertThat(challenge.challenge()).isEqualTo(expected);
        assertThat(challenge.challenge()).doesNotContain("=", "+", "/");
    }

    @Test
    void generate_isNotDeterministic() {
        assertThat(generator.generate().verifier())
            .isNotEqualTo(generator.generate().verifier());
    }

    @Test
    void method_isS256() {
        assertThat(PkceChallenge.METHOD).isEqualTo("S256");
    }
}
