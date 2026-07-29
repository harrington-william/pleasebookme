package com.pleasebookme.server.security.token.jwt.crypto;

import javax.crypto.SecretKey;

public interface JwtKeyProvider {
    SecretKey signingKey();
}
