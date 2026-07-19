package com.pleasebookme.server.auth.apikey.dto;

import com.pleasebookme.server.auth.enums.ApiKeyStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigInteger;
import java.time.Instant;

public record ApiKeyRequest(
    @NotNull
    BigInteger tenantId,

    @NotNull
    BigInteger ownerUserId,

    @NotBlank
    @Size(max = 255)
    String name,

    String description,

    @NotBlank
    String publicKey,

    @NotBlank
    String secretHash,

    ApiKeyStatus status,

    Instant lastUsedAt,

    Instant expiresAt,

    Instant revokedAt
) {
}
