package com.pleasebookme.server.core.booking.dto;

import com.pleasebookme.server.core.enums.BookingStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigInteger;
import java.time.Instant;

public record BookingRequest(
    @Size(max = 255)
    String idempotencyKey,

    @NotNull
    BigInteger userId,

    @NotBlank
    @Size(max = 255)
    String title,

    String description,

    @NotNull
    Instant startTime,

    @NotNull
    Instant endTime,

    @NotNull
    BigInteger serviceId,

    String location,

    BookingStatus status,

    Boolean paid,

    BigInteger cancelledById,

    String cancellationReason,

    String rejectionReason,

    Boolean rescheduled,

    BigInteger rescheduledById,

    Boolean noShowHost,

    Instant deletedAt,

    BigInteger deletedById,

    BigInteger destinationCalendarId,

    BigInteger destinationSheetsId
) {
}
