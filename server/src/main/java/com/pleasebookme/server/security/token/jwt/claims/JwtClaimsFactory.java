package com.pleasebookme.server.security.token.jwt.claims;

import com.pleasebookme.server.security.identity.principal.AuthenticatedPrincipal;

public interface JwtClaimsFactory {
    JwtClaims accessClaims(AuthenticatedPrincipal principal);

    JwtClaims refreshClaims(AuthenticatedPrincipal principal);
}
