package com.pleasebookme.server.resource.maintenance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigInteger;
import java.time.Instant;

public record ResourceMaintenanceRequest(
    @NotNull
    BigInteger resourceId,

    @NotNull
    Instant startTime,

    @NotNull
    Instant endTime,

    @NotBlank
    String reason,

    @NotBlank
    @Size(max = 50)
    String status
) {
}
