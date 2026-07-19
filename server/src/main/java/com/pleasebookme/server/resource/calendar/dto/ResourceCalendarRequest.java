package com.pleasebookme.server.resource.calendar.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigInteger;

public record ResourceCalendarRequest(
    @NotNull
    BigInteger resourceId,

    @NotNull
    BigInteger scheduleId
) {
}
