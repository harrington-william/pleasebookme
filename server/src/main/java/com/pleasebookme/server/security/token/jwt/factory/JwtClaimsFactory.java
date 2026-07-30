package com.pleasebookme.server.security.token.jwt.factory;

import com.pleasebookme.server.security.identity.principal.AuthenticatedPrincipal;
import com.pleasebookme.server.security.token.jwt.claims.JwtClaims;

public interface JwtClaimsFactory {
    JwtClaims accessClaims(AuthenticatedPrincipal principal);

    JwtClaims refreshClaims(AuthenticatedPrincipal principal);
}
