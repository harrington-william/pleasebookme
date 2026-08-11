package com.pleasebookme.server.service.integration.dto;

import com.pleasebookme.server.integration.enums.OAuthConnectionStatus;
import com.pleasebookme.server.integration.enums.OAuthProvider;
import com.pleasebookme.server.integration.oauthconnection.entity.OAuthConnectionEntity;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OAuthConnectionSummaryResponse(
    BigInteger oauthConnectionId,
    UUID oauthConnectionUid,
    OAuthProvider provider,
    String providerEmail,
    List<String> scopes,
    OAuthConnectionStatus status,
    Instant tokenExpiresAt,
    Instant connectedAt,
    Instant lastRefreshedAt,
    Instant lastUsedAt
) {
    public static OAuthConnectionSummaryResponse from(OAuthConnectionEntity connection) {
        return new OAuthConnectionSummaryResponse(
            connection.getOauthConnectionId(),
            connection.getOauthConnectionUid(),
            connection.getProvider(),
            connection.getProviderEmail(),
            connection.getScopes() != null ? List.of(connection.getScopes()) : List.of(),
            connection.getStatus(),
            connection.getTokenExpiresAt(),
            connection.getConnectedAt(),
            connection.getLastRefreshedAt(),
            connection.getLastUsedAt()
        );
    }
}
