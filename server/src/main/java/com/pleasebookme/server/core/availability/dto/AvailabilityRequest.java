package com.pleasebookme.server.core.availability.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigInteger;
import java.time.LocalTime;

public record AvailabilityRequest(
    @NotNull
    BigInteger userId,

    @NotNull
    BigInteger scheduleId,

    @NotNull
    Integer[] days,

    @NotNull
    LocalTime startTime,

    @NotNull
    LocalTime endTime
) {
}
