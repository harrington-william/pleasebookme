package com.pleasebookme.server.core.bookingpolicy.dto;

import com.pleasebookme.server.core.enums.BookingMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ServiceBookingPolicyRequest(
    BookingMode bookingMode,

    Integer defaultDuration,

    Integer minimumDuration,

    Integer maximumDuration,

    @NotNull
    Integer minimumNotice,

    @NotNull
    Integer maximumAdvanceBooking,

    Integer slotInterval,

    Integer beforeBuffer,

    Integer afterBuffer,

    Boolean allowOverlap,

    Boolean allowMultipleAttendee,

    Boolean requiresPayment,

    Boolean autoConfirm,

    @NotBlank
    @Size(max = 50)
    String bookingWindowType,

    @NotNull
    Integer capacity
) {
}
