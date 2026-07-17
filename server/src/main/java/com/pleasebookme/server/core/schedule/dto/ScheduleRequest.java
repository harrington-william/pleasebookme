package com.pleasebookme.server.core.schedule.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigInteger;

public record ScheduleRequest(
    @NotNull
    BigInteger userId,

    @NotBlank
    @Size(max = 255)
    String title,

    @Size(max = 100)
    String timezone
) {
}
