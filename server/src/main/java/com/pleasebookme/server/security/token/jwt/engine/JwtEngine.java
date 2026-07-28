package com.pleasebookme.server.security.token.jwt.engine;

import com.pleasebookme.server.security.identity.principal.AuthenticatedPrincipal;
import com.pleasebookme.server.security.identity.principal.UserPrincipal;
import com.pleasebookme.server.security.token.jwt.claims.JwtClaims;

public interface JwtEngine {
    String issueAccessToken(UserPrincipal principal);

    String issueRefreshToken(UserPrincipal principal);

    JwtClaims verify(String token);
}
