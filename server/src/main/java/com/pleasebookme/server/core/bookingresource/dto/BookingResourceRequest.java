package com.pleasebookme.server.core.bookingresource.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigInteger;

public record BookingResourceRequest(
    @NotNull
    BigInteger bookingId,

    @NotNull
    BigInteger resourceId,

    Boolean isPrimary
) {
}
