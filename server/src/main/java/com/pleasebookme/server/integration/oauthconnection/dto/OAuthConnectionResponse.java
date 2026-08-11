package com.pleasebookme.server.integration.oauthconnection.dto;

import com.pleasebookme.server.integration.enums.OAuthConnectionStatus;
import com.pleasebookme.server.integration.enums.OAuthProvider;
import com.pleasebookme.server.integration.oauthconnection.entity.OAuthConnectionEntity;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

public record OAuthConnectionResponse(
    BigInteger oauthConnectionId,
    UUID oauthConnectionUid,
    BigInteger userId,
    OAuthProvider provider,
    String providerAccountId,
    String providerEmail,
    String[] scopes,
    Short tokenKeyVersion,
    Instant tokenExpiresAt,
    OAuthConnectionStatus status,
    Instant connectedAt,
    Instant lastRefreshedAt,
    Instant lastUsedAt,
    Instant revokedAt,
    Instant createdAt,
    Instant updatedAt
) {
    public static OAuthConnectionResponse from(OAuthConnectionEntity oauthConnection) {
        return new OAuthConnectionResponse(
            oauthConnection.getOauthConnectionId(),
            oauthConnection.getOauthConnectionUid(),
            oauthConnection.getUser().getUserId(),
            oauthConnection.getProvider(),
            oauthConnection.getProviderAccountId(),
            oauthConnection.getProviderEmail(),
            oauthConnection.getScopes(),
            oauthConnection.getTokenKeyVersion(),
            oauthConnection.getTokenExpiresAt(),
            oauthConnection.getStatus(),
            oauthConnection.getConnectedAt(),
            oauthConnection.getLastRefreshedAt(),
            oauthConnection.getLastUsedAt(),
            oauthConnection.getRevokedAt(),
            oauthConnection.getCreatedAt(),
            oauthConnection.getUpdatedAt()
        );
    }
}
