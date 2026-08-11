package com.pleasebookme.server.security.crypto;

import com.pleasebookme.server.security.crypto.config.TokenEncryptionProperties;
import com.pleasebookme.server.security.crypto.exception.TokenEncryptionException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AesGcmTokenCipher implements TokenCipher {
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String ALGORITHM = "AES";

    private static final int KEY_LENGTH_BYTES = 32;
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private final TokenEncryptionProperties properties;

    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<Short, SecretKey> keys = new HashMap<>();

    @PostConstruct
    void loadKeys() {
        properties.keys().forEach((version, encodedKey) -> {
            byte[] keyBytes;

            try {
                keyBytes = Base64.getDecoder().decode(encodedKey);
            } catch (IllegalArgumentException exception) {
                throw new TokenEncryptionException(
                    "Token encryption key version " + version + " is not valid Base64"
                );
            }

            if (keyBytes.length != KEY_LENGTH_BYTES) {
                throw new TokenEncryptionException(
                    "Token encryption key version " + version + " must be "
                        + KEY_LENGTH_BYTES + " bytes (AES-256), got " + keyBytes.length
                );
            }

            keys.put(version, new SecretKeySpec(keyBytes, ALGORITHM));
        });

        if (!keys.containsKey(properties.currentVersion())) {
            throw new TokenEncryptionException(
                "No token encryption key configured for current version "
                    + properties.currentVersion()
            );
        }
    }

    @Override
    public String encrypt(String plaintext) {
        if (plaintext == null) {
            throw new TokenEncryptionException("Cannot encrypt a null value");
        }

        byte[] iv = new byte[IV_LENGTH_BYTES];
        secureRandom.nextBytes(iv);

        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(
                Cipher.ENCRYPT_MODE,
                key(properties.currentVersion()),
                new GCMParameterSpec(TAG_LENGTH_BITS, iv)
            );

            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            byte[] payload = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, payload, 0, iv.length);
            System.arraycopy(encrypted, 0, payload, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(payload);
        } catch (GeneralSecurityException exception) {
            throw new TokenEncryptionException("Failed to encrypt token", exception);
        }
    }

    @Override
    public String decrypt(
        String ciphertext,
        short keyVersion
    ) {
        if (ciphertext == null) {
            throw new TokenEncryptionException("Cannot decrypt a null value");
        }

        byte[] payload;

        try {
            payload = Base64.getDecoder().decode(ciphertext);
        } catch (IllegalArgumentException exception) {
            throw new TokenEncryptionException("Ciphertext is not valid Base64");
        }

        if (payload.length <= IV_LENGTH_BYTES) {
            throw new TokenEncryptionException("Ciphertext is too short to contain an IV");
        }

        byte[] iv = new byte[IV_LENGTH_BYTES];
        byte[] encrypted = new byte[payload.length - IV_LENGTH_BYTES];
        System.arraycopy(payload, 0, iv, 0, IV_LENGTH_BYTES);
        System.arraycopy(payload, IV_LENGTH_BYTES, encrypted, 0, encrypted.length);

        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(
                Cipher.DECRYPT_MODE,
                key(keyVersion),
                new GCMParameterSpec(TAG_LENGTH_BITS, iv)
            );

            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException exception) {
            throw new TokenEncryptionException(
                "Failed to decrypt token with key version " + keyVersion,
                exception
            );
        }
    }

    @Override
    public short currentKeyVersion() {
        return properties.currentVersion();
    }

    private SecretKey key(short version) {
        SecretKey key = keys.get(version);

        if (key == null) {
            throw new TokenEncryptionException(
                "No token encryption key configured for version " + version
            );
        }

        return key;
    }
}
