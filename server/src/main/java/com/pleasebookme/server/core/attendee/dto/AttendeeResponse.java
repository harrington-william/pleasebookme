package com.pleasebookme.server.core.attendee.dto;

import com.pleasebookme.server.core.attendee.entity.AttendeeEntity;
import com.pleasebookme.server.global.enums.Locale;

import java.math.BigInteger;
import java.time.Instant;

public record AttendeeResponse(
    BigInteger attendeeId,
    BigInteger bookingId,
    String email,
    String phone,
    String name,
    Locale locale,
    String timezone,
    Boolean noShow,
    Instant createdAt
) {
    public static AttendeeResponse from(AttendeeEntity attendee) {
        return new AttendeeResponse(
            attendee.getAttendeeId(),
            attendee.getBooking().getBookingId(),
            attendee.getEmail(),
            attendee.getPhone(),
            attendee.getName(),
            attendee.getLocale(),
            attendee.getTimezone(),
            attendee.getNoShow(),
            attendee.getCreatedAt()
        );
    }
}
