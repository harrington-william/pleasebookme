package com.pleasebookme.server.core.bookingresource.dto;

import com.pleasebookme.server.core.bookingresource.entity.BookingResourceEntity;

import java.math.BigInteger;
import java.time.Instant;

public record BookingResourceResponse(
    BigInteger bookingId,
    BigInteger resourceId,
    Boolean isPrimary,
    Instant assignedAt
) {
    public static BookingResourceResponse from(BookingResourceEntity bookingResource) {
        return new BookingResourceResponse(
            bookingResource.getBookingResourceId().getBookingId(),
            bookingResource.getBookingResourceId().getResourceId(),
            bookingResource.getIsPrimary(),
            bookingResource.getAssignedAt()
        );
    }
}
