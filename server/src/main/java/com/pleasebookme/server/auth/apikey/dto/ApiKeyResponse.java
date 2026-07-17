package com.pleasebookme.server.auth.apikey.dto;

import com.pleasebookme.server.auth.enums.ApiKeyStatus;
import com.pleasebookme.server.auth.apikey.entity.ApiKeyEntity;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

public record ApiKeyResponse(
    BigInteger apiKeyId,
    UUID apiKeyUid,
    BigInteger tenantId,
    BigInteger ownerUserId,
    String name,
    String description,
    String publicKey,
    ApiKeyStatus status,
    Instant lastUsedAt,
    Instant expiresAt,
    Instant revokedAt,
    Instant createdAt,
    Instant updatedAt
) {
    public static ApiKeyResponse from(ApiKeyEntity apiKey) {
        return new ApiKeyResponse(
            apiKey.getApiKeyId(),
            apiKey.getApiKeyUid(),
            apiKey.getTenant().getTenantId(),
            apiKey.getOwnerUser().getUserId(),
            apiKey.getName(),
            apiKey.getDescription(),
            apiKey.getPublicKey(),
            apiKey.getStatus(),
            apiKey.getLastUsedAt(),
            apiKey.getExpiresAt(),
            apiKey.getRevokedAt(),
            apiKey.getCreatedAt(),
            apiKey.getUpdatedAt()
        );
    }
}
