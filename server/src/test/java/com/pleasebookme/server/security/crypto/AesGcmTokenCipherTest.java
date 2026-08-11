package com.pleasebookme.server.security.crypto;

import com.pleasebookme.server.security.crypto.config.TokenEncryptionProperties;
import com.pleasebookme.server.security.crypto.exception.TokenEncryptionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AesGcmTokenCipherTest {

    private static final String KEY_V1 = "z0+XUeKFj3ImuY58iihpIAZXA3kSfW4F3+i7MvN1K3s=";
    private static final String KEY_V2 = "8pQdJ2vN7xR4tL0mA6wZ3cB1nS5yH9kU2eG7fT4iO8w=";

    private AesGcmTokenCipher cipher;

    private static AesGcmTokenCipher cipherWith(
        short currentVersion,
        Map<Short, String> keys
    ) {
        AesGcmTokenCipher instance = new AesGcmTokenCipher(
            new TokenEncryptionProperties(currentVersion, keys)
        );
        instance.loadKeys();

        return instance;
    }

    @BeforeEach
    void setUp() {
        cipher = cipherWith((short) 1, Map.of((short) 1, KEY_V1));
    }

    @Test
    void encryptThenDecrypt_returnsOriginalPlaintext() {
        String plaintext = "1//0gLm3xExampleGoogleRefreshToken";

        String ciphertext = cipher.encrypt(plaintext);

        assertThat(cipher.decrypt(ciphertext, (short) 1)).isEqualTo(plaintext);
    }

    @Test
    void encrypt_doesNotLeakPlaintext() {
        String plaintext = "ya29.a0AfH6SensitiveAccessToken";

        String ciphertext = cipher.encrypt(plaintext);

        assertThat(ciphertext).doesNotContain(plaintext);
    }

    @Test
    void encrypt_sameInputTwice_producesDifferentCiphertext() {
        String plaintext = "same-token-value";

        String first = cipher.encrypt(plaintext);
        String second = cipher.encrypt(plaintext);

        // A fresh random IV per call is what makes GCM safe to reuse a key with.
        assertThat(first).isNotEqualTo(second);
        assertThat(cipher.decrypt(first, (short) 1)).isEqualTo(plaintext);
        assertThat(cipher.decrypt(second, (short) 1)).isEqualTo(plaintext);
    }

    @Test
    void encryptThenDecrypt_handlesUnicodeAndEmptyString() {
        assertThat(cipher.decrypt(cipher.encrypt(""), (short) 1)).isEmpty();
        assertThat(cipher.decrypt(cipher.encrypt("Nguyễn Trung — 東京"), (short) 1))
            .isEqualTo("Nguyễn Trung — 東京");
    }

    @Test
    void decrypt_tamperedCiphertext_throws() {
        byte[] payload = Base64.getDecoder().decode(cipher.encrypt("token"));
        payload[payload.length - 1] ^= 0x01;
        String tampered = Base64.getEncoder().encodeToString(payload);

        assertThatThrownBy(() -> cipher.decrypt(tampered, (short) 1))
            .isInstanceOf(TokenEncryptionException.class)
            .hasMessageContaining("Failed to decrypt");
    }

    @Test
    void decrypt_tamperedIv_throws() {
        byte[] payload = Base64.getDecoder().decode(cipher.encrypt("token"));
        payload[0] ^= 0x01;
        String tampered = Base64.getEncoder().encodeToString(payload);

        assertThatThrownBy(() -> cipher.decrypt(tampered, (short) 1))
            .isInstanceOf(TokenEncryptionException.class);
    }

    @Test
    void decrypt_wrongKeyVersion_throws() {
        AesGcmTokenCipher twoKeyCipher = cipherWith(
            (short) 1,
            Map.of((short) 1, KEY_V1, (short) 2, KEY_V2)
        );

        String ciphertext = twoKeyCipher.encrypt("token");

        assertThatThrownBy(() -> twoKeyCipher.decrypt(ciphertext, (short) 2))
            .isInstanceOf(TokenEncryptionException.class);
    }

    @Test
    void decrypt_unknownKeyVersion_throws() {
        String ciphertext = cipher.encrypt("token");

        assertThatThrownBy(() -> cipher.decrypt(ciphertext, (short) 9))
            .isInstanceOf(TokenEncryptionException.class)
            .hasMessageContaining("No token encryption key configured for version 9");
    }

    @Test
    void decrypt_malformedInput_throws() {
        assertThatThrownBy(() -> cipher.decrypt("not-base64!!!", (short) 1))
            .isInstanceOf(TokenEncryptionException.class)
            .hasMessageContaining("not valid Base64");

        assertThatThrownBy(() -> cipher.decrypt(
            Base64.getEncoder().encodeToString(new byte[8]), (short) 1
        ))
            .isInstanceOf(TokenEncryptionException.class)
            .hasMessageContaining("too short");
    }

    @Test
    void encryptOrDecrypt_null_throws() {
        assertThatThrownBy(() -> cipher.encrypt(null))
            .isInstanceOf(TokenEncryptionException.class);

        assertThatThrownBy(() -> cipher.decrypt(null, (short) 1))
            .isInstanceOf(TokenEncryptionException.class);
    }

    @Test
    void rotation_oldCiphertextStillDecryptsWithItsOwnKeyVersion() {
        // v1 is current: encrypt and keep the ciphertext.
        AesGcmTokenCipher before = cipherWith((short) 1, Map.of((short) 1, KEY_V1));
        String legacyCiphertext = before.encrypt("legacy-refresh-token");

        // Rotate: v2 becomes current, v1 is retained for reads.
        AesGcmTokenCipher after = cipherWith(
            (short) 2,
            Map.of((short) 1, KEY_V1, (short) 2, KEY_V2)
        );

        assertThat(after.currentKeyVersion()).isEqualTo((short) 2);
        assertThat(after.decrypt(legacyCiphertext, (short) 1)).isEqualTo("legacy-refresh-token");

        String rotated = after.encrypt("new-refresh-token");
        assertThat(after.decrypt(rotated, after.currentKeyVersion())).isEqualTo("new-refresh-token");
    }

    @Test
    void loadKeys_rejectsInvalidKeyMaterial() {
        assertThatThrownBy(() -> cipherWith((short) 1, Map.of((short) 1, "not-base64!!!")))
            .isInstanceOf(TokenEncryptionException.class)
            .hasMessageContaining("not valid Base64");

        String shortKey = Base64.getEncoder().encodeToString(new byte[16]);
        assertThatThrownBy(() -> cipherWith((short) 1, Map.of((short) 1, shortKey)))
            .isInstanceOf(TokenEncryptionException.class)
            .hasMessageContaining("must be 32 bytes");
    }

    @Test
    void loadKeys_rejectsMissingCurrentVersionKey() {
        assertThatThrownBy(() -> cipherWith((short) 3, Map.of((short) 1, KEY_V1)))
            .isInstanceOf(TokenEncryptionException.class)
            .hasMessageContaining("current version 3");
    }
}
