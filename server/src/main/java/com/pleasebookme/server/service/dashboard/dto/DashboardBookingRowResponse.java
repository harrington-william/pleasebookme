package com.pleasebookme.server.service.dashboard.dto;

import com.pleasebookme.server.core.enums.BookingStatus;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

/**
 * One recent-booking row. Customer and resource names are nullable because a
 * booking can legitimately have neither an attendee nor a primary resource.
 */
public record DashboardBookingRowResponse(
    BigInteger bookingId,
    UUID bookingUid,
    String title,
    Instant startTime,
    Instant endTime,
    BookingStatus status,
    BigInteger serviceId,
    String serviceTitle,
    String customerName,
    String resourceName
) {}
