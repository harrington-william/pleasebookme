package com.pleasebookme.server.core.availability.dto;

import com.pleasebookme.server.core.availability.entity.AvailabilityEntity;

import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalTime;

public record AvailabilityResponse(
    BigInteger availabilityId,
    BigInteger userId,
    BigInteger scheduleId,
    Integer[] days,
    LocalTime startTime,
    LocalTime endTime,
    Instant createdAt,
    Instant updatedAt
) {
    public static AvailabilityResponse from(AvailabilityEntity availability) {
        return new AvailabilityResponse(
            availability.getAvailabilityId(),
            availability.getUser().getUserId(),
            availability.getSchedule().getScheduleId(),
            availability.getDays(),
            availability.getStartTime(),
            availability.getEndTime(),
            availability.getCreatedAt(),
            availability.getUpdatedAt()
        );
    }
}
