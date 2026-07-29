package com.pleasebookme.server.security.token.refresh;

import com.pleasebookme.server.auth.refreshtoken.entity.RefreshTokenEntity;
import com.pleasebookme.server.auth.refreshtoken.enums.RefreshOwner;
import com.pleasebookme.server.auth.refreshtoken.repository.RefreshTokenRepository;
import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.security.identity.aggregation.AuthenticationAggregation;
import com.pleasebookme.server.security.identity.loader.user.UserIdentityLoader;
import com.pleasebookme.server.security.identity.mapper.UserPrincipalMapper;
import com.pleasebookme.server.security.identity.principal.UserPrincipal;
import com.pleasebookme.server.security.token.jwt.config.JwtProperties;
import com.pleasebookme.server.security.token.jwt.engine.JwtEngine;
import com.pleasebookme.server.security.token.refresh.verifier.RefreshTokenVerifier;
import com.pleasebookme.server.service.auth.dto.RefreshResponse;
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
    private final UserIdentityLoader userIdentityLoader;
    private final RefreshTokenVerifier refreshTokenVerifier;
    private final UserPrincipalMapper principalMapper;

    @Transactional
    public RefreshResponse refresh(String refreshToken) {
        RefreshTokenEntity token = refreshTokenVerifier.verify(refreshToken);

        // Temporary reject requests not coming from a USER
        // Other type of origin such as widget will be supported later
        if (
            token.getOwner() == null ||
            token.getOwner() != RefreshOwner.USER
        ) {
            throw new RuntimeException("Unsupported owner type");
        }

        // Handle user request
        UserEntity user = token.getUser();
        String deviceName = token.getDeviceName();

        token.setRevokedAt(Instant.now());

        UserPrincipal principal = loadPrincipal(user.getUsername());

        // Issue tokens
        String newAccessToken = jwtEngine.issueAccessToken(principal);
        String newRefreshToken = jwtEngine.issueRefreshToken(principal);

        RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.builder()
            .secret(newRefreshToken)
            .owner(RefreshOwner.USER)
            .user(user)
            .deviceName(deviceName)
            .createdAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(
                jwtProperties.refreshTokenLifeTime().toSeconds()
            ))
            .build();

        refreshTokenRepository.save(refreshTokenEntity);

        return new RefreshResponse(newAccessToken, newRefreshToken);
    }

    private UserPrincipal loadPrincipal(String username) {
        AuthenticationAggregation aggregation = userIdentityLoader.loadByUsername(username);

        return principalMapper.map(aggregation);
    }
}
