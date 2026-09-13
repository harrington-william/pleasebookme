package com.pleasebookme.server.widget.widgets.credential;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class WidgetCredentialGenerator {
    private static final int PUBLIC_KEY_BYTES = 16;
    private static final int SECRET_KEY_BYTES = 32;
    private static final String PUBLIC_KEY_PREFIX = "pbm_pk_";
    private static final String SECRET_KEY_PREFIX = "pbm_sk_";

    private final SecureRandom secureRandom = new SecureRandom();
    private final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

    public WidgetCredentialPair generate() {
        // Recognisable prefixes aid leak detection; fixed entropy sizes keep validation deterministic.
        return new WidgetCredentialPair(
            PUBLIC_KEY_PREFIX + randomValue(PUBLIC_KEY_BYTES),
            SECRET_KEY_PREFIX + randomValue(SECRET_KEY_BYTES)
        );
    }

    private String randomValue(int byteCount) {
        byte[] bytes = new byte[byteCount];
        secureRandom.nextBytes(bytes);
        return encoder.encodeToString(bytes);
    }
}
