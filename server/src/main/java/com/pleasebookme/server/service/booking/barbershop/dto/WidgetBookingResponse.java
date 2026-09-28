package com.pleasebookme.server.service.booking.barbershop.dto;

import com.pleasebookme.server.core.enums.BookingStatus;

import java.time.Instant;
import java.util.UUID;

public record WidgetBookingResponse(
    UUID bookingUid,
    BookingStatus status,
    Instant startTime,
    Instant endTime,
    String timezone,
    String serviceTitle,
    String organizationName
) {}
