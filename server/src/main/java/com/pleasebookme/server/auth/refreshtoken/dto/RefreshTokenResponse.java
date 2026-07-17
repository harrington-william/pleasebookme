package com.pleasebookme.server.auth.refreshtoken.dto;

import com.pleasebookme.server.auth.refreshtoken.entity.RefreshTokenEntity;

import java.math.BigInteger;
import java.time.Instant;

public record RefreshTokenResponse(
    BigInteger refreshTokenId,
    String owner,
    BigInteger userId,
    BigInteger widgetId,
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
            refreshToken.getWidget() != null ? refreshToken.getWidget().getWidgetId() : null,
            refreshToken.getDeviceName(),
            refreshToken.getOauthClientId(),
            refreshToken.getCreatedAt(),
            refreshToken.getExpiresAt(),
            refreshToken.getRevokedAt()
        );
    }
}
