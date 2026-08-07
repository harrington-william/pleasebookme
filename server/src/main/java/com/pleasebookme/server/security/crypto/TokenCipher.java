package com.pleasebookme.server.security.crypto;

public interface TokenCipher {
    String encrypt(String plaintext);

    String decrypt(
        String ciphertext,
        short keyVersion
    );

    short currentKeyVersion();
}
