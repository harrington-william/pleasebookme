package com.pleasebookme.server.auth.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigInteger;
import java.time.Instant;

public record AccountRequest(
    @NotNull
    BigInteger userId,

    @Size(max = 50)
    String type,

    @NotBlank
    @Size(max = 100)
    String provider,

    @NotBlank
    @Size(max = 255)
    String providerAccountId,

    @Size(max = 255)
    String providerEmail,

    String accessToken,

    String refreshToken,

    Instant expiresAt,

    String tokenType,

    String scope,

    String idToken
) {
}
