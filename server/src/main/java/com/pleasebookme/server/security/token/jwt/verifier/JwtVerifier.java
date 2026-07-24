package com.pleasebookme.server.security.token.jwt.verifier;

import com.pleasebookme.server.security.token.jwt.claims.JwtClaims;

public interface JwtVerifier {
    JwtClaims verify(String token);
}
