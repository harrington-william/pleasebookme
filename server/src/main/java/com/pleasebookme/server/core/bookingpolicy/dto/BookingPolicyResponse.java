package com.pleasebookme.server.core.bookingpolicy.dto;

import com.pleasebookme.server.core.bookingpolicy.entity.BookingPolicyEntity;
import com.pleasebookme.server.core.enums.BookingMode;

import java.math.BigInteger;
import java.time.Instant;

public record BookingPolicyResponse(
    BigInteger bookingPolicyId,
    BigInteger serviceId,
    BookingMode bookingMode,
    String durationType,
    Integer defaultDuration,
    Integer minimumDuration,
    Integer maximumDuration,
    Integer minimumNotice,
    Integer maximumAdvanceBooking,
    Integer slotInterval,
    Integer beforeBuffer,
    Integer afterBuffer,
    Boolean allowOverlap,
    Boolean allowMultipleAttendee,
    Boolean requiresPayment,
    Boolean autoConfirm,
    String bookingWindowType,
    Integer capacity,
    Instant createdAt,
    Instant updatedAt
) {
    public static BookingPolicyResponse from(BookingPolicyEntity bookingPolicy) {
        return new BookingPolicyResponse(
            bookingPolicy.getBookingPolicyId(),
            bookingPolicy.getService().getServiceId(),
            bookingPolicy.getBookingMode(),
            bookingPolicy.getDurationType(),
            bookingPolicy.getDefaultDuration(),
            bookingPolicy.getMinimumDuration(),
            bookingPolicy.getMaximumDuration(),
            bookingPolicy.getMinimumNotice(),
            bookingPolicy.getMaximumAdvanceBooking(),
            bookingPolicy.getSlotInterval(),
            bookingPolicy.getBeforeBuffer(),
            bookingPolicy.getAfterBuffer(),
            bookingPolicy.getAllowOverlap(),
            bookingPolicy.getAllowMultipleAttendee(),
            bookingPolicy.getRequiresPayment(),
            bookingPolicy.getAutoConfirm(),
            bookingPolicy.getBookingWindowType(),
            bookingPolicy.getCapacity(),
            bookingPolicy.getCreatedAt(),
            bookingPolicy.getUpdatedAt()
        );
    }
}
