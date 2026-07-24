package com.pleasebookme.server.security.token.jwt.engine;

import com.pleasebookme.server.security.identity.principal.AuthenticatedPrincipal;
import com.pleasebookme.server.security.token.jwt.claims.JwtClaims;
import com.pleasebookme.server.security.token.jwt.claims.JwtClaimsFactory;
import com.pleasebookme.server.security.token.jwt.generator.JwtGenerator;
import com.pleasebookme.server.security.token.jwt.verifier.JwtVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultJwtEngine implements JwtEngine {
    private final JwtClaimsFactory jwtClaimsFactory;
    private final JwtGenerator jwtGenerator;
    private final JwtVerifier jwtVerifier;

    @Override
    public String issueAccessToken(AuthenticatedPrincipal principal) {
        JwtClaims claims = jwtClaimsFactory.accessClaims(principal);

        return jwtGenerator.generate(claims);
    }

    @Override
    public String issueRefreshToken(AuthenticatedPrincipal principal) {
        JwtClaims claims = jwtClaimsFactory.refreshClaims(principal);

        return jwtGenerator.generate(claims);
    }

    @Override
    public JwtClaims verify(String token) {
        return jwtVerifier.verify(token);
    }
}
