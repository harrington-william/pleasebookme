package com.pleasebookme.server.core.outofoffice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigInteger;
import java.time.Instant;

public record OutOfOfficeRequest(
    @NotNull
    Instant startTime,

    @NotNull
    Instant endTime,

    String notes,

    Boolean showNotePublicly,

    @NotNull
    BigInteger userId,

    @NotNull
    BigInteger toUserId,

    @NotBlank
    String reason
) {
}
