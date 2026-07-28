package com.pleasebookme.server.security.token.jwt.factory;

import com.pleasebookme.server.security.identity.principal.AuthenticatedPrincipal;
import com.pleasebookme.server.security.token.jwt.claims.JwtClaims;
import com.pleasebookme.server.security.token.jwt.config.JwtProperties;
import com.pleasebookme.server.security.token.jwt.enums.JwtTokenType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DefaultJwtClaimsFactory implements JwtClaimsFactory {
    private final JwtProperties jwtProperties;

    @Override
    public JwtClaims accessClaims(AuthenticatedPrincipal principal) {
        Instant now = Instant.now();

        return new JwtClaims(
            principal.actorType(),

            // Subject
            principal.subject(),
            principal.tenantUid(),
            UUID.randomUUID(),

            JwtTokenType.ACCESS,

            now,
            now.plus(jwtProperties.accessTokenLifeTime())
        );
    }

    @Override
    public JwtClaims refreshClaims(AuthenticatedPrincipal principal) {
        Instant now = Instant.now();

        return new JwtClaims(
            principal.actorType(),

            // Subject
            principal.subject(),
            principal.tenantUid(),
            UUID.randomUUID(),

            JwtTokenType.REFRESH,

            now,
            now.plus(jwtProperties.refreshTokenLifeTime())
        );
    }
}
