package com.pleasebookme.server.security.token.jwt.verifier;

import com.pleasebookme.server.security.identity.enums.AuthenticatedActorType;
import com.pleasebookme.server.security.token.jwt.claims.JwtClaims;
import com.pleasebookme.server.security.token.jwt.config.JwtProperties;
import com.pleasebookme.server.security.token.jwt.crypto.JwtKeyProvider;
import com.pleasebookme.server.security.token.jwt.enums.JwtTokenType;
import com.pleasebookme.server.security.token.jwt.exception.TokenExpiredException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DefaultJwtVerifier implements JwtVerifier {
    private final JwtProperties jwtProperties;
    private final JwtKeyProvider jwtKeyProvider;

    @Override
    public JwtClaims verify(String token) {
        final Claims claims = Jwts.parser()
            .verifyWith(jwtKeyProvider.signingKey())
            .requireIssuer(jwtProperties.issuer())
            .requireAudience(jwtProperties.audience())
            .build()
            .parseSignedClaims(token)
            .getPayload();

        isTokenExpired(claims);
        validateTokenType(claims);

        return new JwtClaims(
            AuthenticatedActorType.valueOf(
                claims.get(
                    "actor_type",
                    String.class
                )
            ),

            UUID.fromString(claims.getSubject()),
            UUID.fromString(
                claims.get(
                    "tenant",
                    String.class
                )
            ),

            UUID.fromString(claims.getId()),
            JwtTokenType.valueOf(
                claims.get(
                    "token_type",
                    String.class
                )
            ),

            claims.getIssuedAt().toInstant(),
            claims.getExpiration().toInstant()
        );
    }

    private void validateTokenType(Claims claims) {
        String tokenType = claims.get("token_type", String.class);

        if (!"ACCESS".equals(tokenType)) {
            throw new JwtException("Invalid token type");
        }
    }

    private void isTokenExpired(Claims claims) {
        if (
            claims.getExpiration() == null ||
                claims.getExpiration().before(new Date())
        ) {
            throw new TokenExpiredException("Token has expired");
        }
    }
}
