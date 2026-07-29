package com.pleasebookme.server.auth.refreshtoken.dto;

import com.pleasebookme.server.auth.refreshtoken.enums.RefreshOwner;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigInteger;
import java.time.Instant;

public record RefreshTokenRequest(
    @NotNull
    BigInteger userId,

    @NotBlank
    String secret,

    @Size(max = 255)
    RefreshOwner owner,

    @Size(max = 255)
    String deviceName,

    @Size(max = 255)
    String oauthClientId,

    @NotNull
    Instant expiresAt,

    Instant revokedAt
) {
}
