package com.pleasebookme.server.security.token.refresh.verifier;

import com.pleasebookme.server.auth.refreshtoken.entity.RefreshTokenEntity;
import com.pleasebookme.server.auth.refreshtoken.exception.RefreshTokenExpiredException;
import com.pleasebookme.server.auth.refreshtoken.exception.RefreshTokenNotFoundException;
import com.pleasebookme.server.auth.refreshtoken.exception.RefreshTokenRevokedException;
import com.pleasebookme.server.auth.refreshtoken.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class DefaultRefreshTokenVerifier implements RefreshTokenVerifier {
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public RefreshTokenEntity verify(String secret) {
        RefreshTokenEntity token =
            refreshTokenRepository.findBySecret(secret)
                .orElseThrow(() ->
                    new RefreshTokenNotFoundException(
                        "Invalid refresh token"
                    )
                );

        if (token.getRevokedAt() != null) {
            throw new RefreshTokenRevokedException("Token has been revoked");
        }

        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new RefreshTokenExpiredException("Token has expired");
        }

        return token;
    }
}
