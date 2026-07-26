package com.pleasebookme.server.security.token.refresh;

import com.pleasebookme.server.auth.refreshtoken.entity.RefreshTokenEntity;
import com.pleasebookme.server.auth.refreshtoken.exception.RefreshTokenExpiredException;
import com.pleasebookme.server.auth.refreshtoken.exception.RefreshTokenNotFoundException;
import com.pleasebookme.server.auth.refreshtoken.exception.RefreshTokenRevokedException;
import com.pleasebookme.server.auth.refreshtoken.repository.RefreshTokenRepository;
import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.security.identity.aggregation.AuthenticationAggregation;
import com.pleasebookme.server.security.identity.loader.IdentityLoader;
import com.pleasebookme.server.security.identity.mapper.PrincipalMapper;
import com.pleasebookme.server.security.identity.principal.AuthenticatedPrincipal;
import com.pleasebookme.server.security.token.jwt.config.JwtProperties;
import com.pleasebookme.server.security.token.jwt.engine.JwtEngine;
import com.pleasebookme.server.service.auth.dto.LoginResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenRefresher {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;
    private final JwtEngine jwtEngine;
    private final IdentityLoader identityLoader;
    private final PrincipalMapper<AuthenticationAggregation> principalMapper;

    @Transactional
    public LoginResponse refresh(String refreshToken) {
        RefreshTokenEntity token =
            refreshTokenRepository.findBySecret(refreshToken)
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

        // Temporary reject requests not coming from a USER
        // Other type of origin such as widget will be supported later
        if (
            token.getOwner() == null ||
            !token.getOwner().equals("USER")
        ) {

            throw new RuntimeException("Unsupported owner type");
        }

        // Handle user request
        UserEntity user = token.getUser();
        String deviceName = token.getDeviceName();

        token.setRevokedAt(Instant.now());

        AuthenticatedPrincipal principal = loadPrincipal(user.getUsername());

        // Issue tokens
        String newAccessToken = jwtEngine.issueAccessToken(principal);
        String newRefreshToken = jwtEngine.issueRefreshToken(principal);

        RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.builder()
            .secret(newRefreshToken)
            .owner("USER")
            .user(user)
            .deviceName(deviceName)
            .createdAt(Instant.now())
            // Check expires
            .expiresAt(Instant.now().plus(jwtProperties.refreshTokenLifeTime()))
            .build();

        refreshTokenRepository.save(refreshTokenEntity);

        return new LoginResponse(newAccessToken, newRefreshToken);
    }

    private AuthenticatedPrincipal loadPrincipal(String username) {
        AuthenticationAggregation aggregation = identityLoader.loadByUsername(username);

        return principalMapper.map(aggregation);
    }
}
