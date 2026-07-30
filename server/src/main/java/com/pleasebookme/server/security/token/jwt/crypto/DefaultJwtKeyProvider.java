package com.pleasebookme.server.security.token.jwt.crypto;

import com.pleasebookme.server.security.token.jwt.config.JwtProperties;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
@RequiredArgsConstructor
public class DefaultJwtKeyProvider implements JwtKeyProvider {
    private final JwtProperties jwtProperties;

    @Override
    public SecretKey signingKey() {
        byte[] bytes = Decoders.BASE64.decode(
            jwtProperties.secret()
        );

        return Keys.hmacShaKeyFor(bytes);
    }
}
