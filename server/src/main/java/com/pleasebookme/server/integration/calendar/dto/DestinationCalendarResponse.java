package com.pleasebookme.server.integration.calendar.dto;

import com.pleasebookme.server.integration.calendar.entity.DestinationCalendarEntity;
import com.pleasebookme.server.integration.enums.IntegrationType;

import java.math.BigInteger;
import java.time.Instant;

public record DestinationCalendarResponse(
    BigInteger destinationCalendarId,
    IntegrationType integrationType,
    String externalId,
    BigInteger userId,
    BigInteger serviceId,
    Instant createdAt,
    Instant updatedAt
) {
    public static DestinationCalendarResponse from(DestinationCalendarEntity destinationCalendar) {
        return new DestinationCalendarResponse(
            destinationCalendar.getDestinationCalendarId(),
            destinationCalendar.getIntegrationType(),
            destinationCalendar.getExternalId(),
            destinationCalendar.getUser().getUserId(),
            destinationCalendar.getService().getServiceId(),
            destinationCalendar.getCreatedAt(),
            destinationCalendar.getUpdatedAt()
        );
    }
}
