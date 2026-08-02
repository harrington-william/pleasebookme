package com.pleasebookme.server.integration.oauthconnection.dto;

import com.pleasebookme.server.integration.enums.OAuthConnectionStatus;
import com.pleasebookme.server.integration.enums.OAuthProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigInteger;
import java.time.Instant;

public record OAuthConnectionRequest(
    @NotNull
    BigInteger userId,

    @NotNull
    OAuthProvider provider,

    @NotBlank
    @Size(max = 255)
    String providerAccountId,

    @Size(max = 255)
    String providerEmail,

    @NotNull
    String[] scopes,

    @NotBlank
    String accessToken,

    @NotBlank
    String refreshToken,

    Short tokenKeyVersion,

    @NotNull
    Instant tokenExpiresAt,

    OAuthConnectionStatus status,

    Instant lastRefreshedAt,

    Instant lastUsedAt,

    Instant revokedAt
) {
}
