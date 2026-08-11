package com.pleasebookme.server.security.oauth.google.pkce;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class PkceGenerator {
    private static final int VERIFIER_BYTES = 64;

    private final SecureRandom secureRandom = new SecureRandom();
    private final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

    public PkceChallenge generate() {
        byte[] bytes = new byte[VERIFIER_BYTES];
        secureRandom.nextBytes(bytes);

        String verifier = encoder.encodeToString(bytes);

        return new PkceChallenge(verifier, challengeFor(verifier));
    }

    private String challengeFor(String verifier) {
        try {
            byte[] digest = MessageDigest
                .getInstance("SHA-256")
                .digest(verifier.getBytes(StandardCharsets.US_ASCII));

            return encoder.encodeToString(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
