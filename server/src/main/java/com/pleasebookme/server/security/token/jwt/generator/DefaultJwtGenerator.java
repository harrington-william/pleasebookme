package com.pleasebookme.server.security.token.jwt.generator;

import com.pleasebookme.server.security.token.jwt.claims.JwtClaims;
import com.pleasebookme.server.security.token.jwt.config.JwtProperties;
import com.pleasebookme.server.security.token.jwt.crypto.JwtKeyProvider;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class DefaultJwtGenerator implements JwtGenerator {
    private final JwtProperties jwtProperties;
    private final JwtKeyProvider jwtKeyProvider;

    @Override
    public String generate(JwtClaims claims) {
        return Jwts.builder()
            .issuer(jwtProperties.issuer())
            .audience()
                .add(jwtProperties.audience())
                .and()

            .subject(claims.subject().toString())
            .id(claims.tokenId().toString())

            .issuedAt(Date.from(claims.issuedAt()))
            .expiration(Date.from(claims.expiresAt()))

            .claim(
                "tenant",
                claims.tenant().toString()
            )
            .claim(
                "actor_type",
                claims.actorType().name()
            )
            .claim(
                "token_type",
                claims.tokenType().name()
            )

            .signWith(
                jwtKeyProvider.signingKey(),
                Jwts.SIG.HS256
            )
            .compact();
    }
}
