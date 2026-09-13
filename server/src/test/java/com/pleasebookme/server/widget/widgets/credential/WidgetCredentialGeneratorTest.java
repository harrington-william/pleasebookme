package com.pleasebookme.server.widget.widgets.credential;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class WidgetCredentialGeneratorTest {
    private static final String PUBLIC_KEY_PATTERN = "^pbm_pk_[A-Za-z0-9_-]{22}$";
    private static final String SECRET_KEY_PATTERN = "^pbm_sk_[A-Za-z0-9_-]{43}$";

    private final WidgetCredentialGenerator generator = new WidgetCredentialGenerator();

    @Test
    void generate_returnsFixedFormatCredentialPair() {
        WidgetCredentialPair pair = generator.generate();

        assertThat(pair.publicKey()).matches(PUBLIC_KEY_PATTERN);
        assertThat(pair.secretKey()).matches(SECRET_KEY_PATTERN);
    }

    @Test
    void generate_producesOneThousandDistinctPublicKeys() {
        Set<String> publicKeys = new HashSet<>();

        for (int i = 0; i < 1_000; i++) {
            publicKeys.add(generator.generate().publicKey());
        }

        assertThat(publicKeys).hasSize(1_000);
    }

    @Test
    void generatedSecret_isSafeForBcryptRoundTrip() {
        String secret = generator.generate().secretKey();
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(11);

        assertThat(encoder.matches(secret, encoder.encode(secret))).isTrue();
    }
}
