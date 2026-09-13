package com.pleasebookme.server.service.availabilityruleset.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;
import java.util.List;

public record AvailabilityWindowRequest(
    @NotNull
    List<Integer> days,

    @NotNull
    LocalTime startTime,

    @NotNull
    LocalTime endTime
) {
}
