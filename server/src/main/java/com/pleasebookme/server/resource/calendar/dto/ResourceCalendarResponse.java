package com.pleasebookme.server.resource.calendar.dto;

import com.pleasebookme.server.resource.calendar.entity.ResourceCalendarEntity;

import java.math.BigInteger;
import java.time.Instant;

public record ResourceCalendarResponse(
    BigInteger resourceCalendarId,
    BigInteger resourceId,
    BigInteger scheduleId,
    Instant createdAt,
    Instant updatedAt
) {
    public static ResourceCalendarResponse from(ResourceCalendarEntity resourceCalendar) {
        return new ResourceCalendarResponse(
            resourceCalendar.getResourceCalendarId(),
            resourceCalendar.getResource().getResourceId(),
            resourceCalendar.getSchedule().getScheduleId(),
            resourceCalendar.getCreatedAt(),
            resourceCalendar.getUpdatedAt()
        );
    }
}
