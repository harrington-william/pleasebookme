package com.pleasebookme.server.core.schedule.dto;

import com.pleasebookme.server.core.schedule.entity.ScheduleEntity;

import java.math.BigInteger;
import java.time.Instant;

public record ScheduleResponse(
    BigInteger scheduleId,
    BigInteger userId,
    String title,
    String timezone,
    Instant createdAt,
    Instant updatedAt
) {
    public static ScheduleResponse from(ScheduleEntity schedule) {
        return new ScheduleResponse(
            schedule.getScheduleId(),
            schedule.getUser().getUserId(),
            schedule.getTitle(),
            schedule.getTimezone(),
            schedule.getCreatedAt(),
            schedule.getUpdatedAt()
        );
    }
}
