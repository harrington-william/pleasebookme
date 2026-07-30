package com.pleasebookme.server.auth.refreshtoken.dto;

import com.pleasebookme.server.auth.refreshtoken.entity.RefreshTokenEntity;
import com.pleasebookme.server.auth.refreshtoken.enums.RefreshOwner;

import java.math.BigInteger;
import java.time.Instant;

public record RefreshTokenResponse(
    BigInteger refreshTokenId,
    RefreshOwner owner,
    BigInteger userId,
    String deviceName,
    String oauthClientId,
    Instant createdAt,
    Instant expiresAt,
    Instant revokedAt
) {
    public static RefreshTokenResponse from(RefreshTokenEntity refreshToken) {
        return new RefreshTokenResponse(
            refreshToken.getRefreshTokenId(),
            refreshToken.getOwner(),
            refreshToken.getUser().getUserId(),
            refreshToken.getDeviceName(),
            refreshToken.getOauthClientId(),
            refreshToken.getCreatedAt(),
            refreshToken.getExpiresAt(),
            refreshToken.getRevokedAt()
        );
    }
}
