package com.pleasebookme.server.service.auth.service.impl;

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
import com.pleasebookme.server.service.auth.dto.LoginResponse;
import com.pleasebookme.server.service.auth.service.SessionIssuer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class DefaultSessionIssuer implements SessionIssuer {
    private final UserIdentityLoader userIdentityLoader;
    private final UserPrincipalMapper userPrincipalMapper;

    private final JwtEngine jwtEngine;
    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public LoginResponse issue(UserEntity user) {
        AuthenticationAggregation aggregation = userIdentityLoader.loadByUsername(user.getUsername());
        UserPrincipal principal = userPrincipalMapper.map(aggregation);

        String accessToken = jwtEngine.issueAccessToken(principal);
        String refreshToken = jwtEngine.issueRefreshToken(principal);

        Instant now = Instant.now();
        RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.builder()
            .secret(refreshToken)
            .owner(RefreshOwner.USER)
            .user(user)
            .deviceName(null)
            .createdAt(now)
            .expiresAt(now.plusSeconds(
                jwtProperties.refreshTokenLifeTime().toSeconds()
            ))
            .build();
        refreshTokenRepository.save(refreshTokenEntity);

        return new LoginResponse(accessToken, refreshToken);
    }
}
