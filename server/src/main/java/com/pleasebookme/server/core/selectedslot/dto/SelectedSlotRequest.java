package com.pleasebookme.server.core.selectedslot.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigInteger;
import java.time.Instant;

public record SelectedSlotRequest(
    @NotNull
    BigInteger serviceId,

    @NotNull
    BigInteger userId,

    @NotNull
    Instant slotStart,

    @NotNull
    Instant slotEnd,

    @NotNull
    Instant releaseAt,

    Boolean isSeat
) {
}
