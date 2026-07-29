package com.pleasebookme.server.security.token.jwt.engine;

import com.pleasebookme.server.security.identity.principal.AuthenticatedPrincipal;
import com.pleasebookme.server.security.token.jwt.claims.JwtClaims;

public interface JwtEngine {
    String issueAccessToken(AuthenticatedPrincipal principal);

    String issueRefreshToken(AuthenticatedPrincipal principal);

    JwtClaims verify(String token);
}
