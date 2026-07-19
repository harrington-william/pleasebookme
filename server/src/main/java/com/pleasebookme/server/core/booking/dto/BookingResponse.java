package com.pleasebookme.server.core.booking.dto;

import com.pleasebookme.server.core.booking.entity.BookingEntity;
import com.pleasebookme.server.core.enums.BookingStatus;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

public record BookingResponse(
    BigInteger bookingId,
    UUID bookingUid,
    String idempotencyKey,
    BigInteger userId,
    String title,
    String description,
    Instant startTime,
    Instant endTime,
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
    BigInteger destinationSheetsId,
    Instant createdAt,
    Instant updatedAt
) {
    public static BookingResponse from(BookingEntity booking) {
        return new BookingResponse(
            booking.getBookingId(),
            booking.getBookingUid(),
            booking.getIdempotencyKey(),
            booking.getUser().getUserId(),
            booking.getTitle(),
            booking.getDescription(),
            booking.getStartTime(),
            booking.getEndTime(),
            booking.getService().getServiceId(),
            booking.getLocation(),
            booking.getStatus(),
            booking.getPaid(),
            booking.getCancelledBy() != null ? booking.getCancelledBy().getUserId() : null,
            booking.getCancellationReason(),
            booking.getRejectionReason(),
            booking.getRescheduled(),
            booking.getRescheduledBy() != null ? booking.getRescheduledBy().getUserId() : null,
            booking.getNoShowHost(),
            booking.getDeletedAt(),
            booking.getDeletedBy() != null ? booking.getDeletedBy().getUserId() : null,
            booking.getDestinationCalendar() != null ? booking.getDestinationCalendar().getDestinationCalendarId() : null,
            booking.getDestinationSheets() != null ? booking.getDestinationSheets().getDestinationSheetsId() : null,
            booking.getCreatedAt(),
            booking.getUpdatedAt()
        );
    }
}
