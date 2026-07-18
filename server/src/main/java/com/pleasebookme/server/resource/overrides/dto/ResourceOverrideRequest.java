package com.pleasebookme.server.resource.overrides.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigInteger;
import java.time.Instant;

public record ResourceOverrideRequest(
    @NotNull
    BigInteger resourceId,

    @NotNull
    Instant startTime,

    @NotNull
    Instant endTime,

    @NotBlank
    String reason
) {
}
