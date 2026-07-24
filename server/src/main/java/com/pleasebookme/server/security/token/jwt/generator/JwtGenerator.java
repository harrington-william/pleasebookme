package com.pleasebookme.server.security.token.jwt.generator;

import com.pleasebookme.server.security.token.jwt.claims.JwtClaims;

public interface JwtGenerator {
    String generate(JwtClaims claims);
}
