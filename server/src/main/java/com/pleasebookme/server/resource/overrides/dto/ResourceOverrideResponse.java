package com.pleasebookme.server.resource.overrides.dto;

import com.pleasebookme.server.resource.overrides.entity.ResourceOverrideEntity;

import java.math.BigInteger;
import java.time.Instant;

public record ResourceOverrideResponse(
    BigInteger resourceOverrideId,
    BigInteger resourceId,
    Instant startTime,
    Instant endTime,
    String reason,
    Instant createdAt,
    Instant updatedAt
) {
    public static ResourceOverrideResponse from(ResourceOverrideEntity resourceOverride) {
        return new ResourceOverrideResponse(
            resourceOverride.getResourceOverrideId(),
            resourceOverride.getResource().getResourceId(),
            resourceOverride.getStartTime(),
            resourceOverride.getEndTime(),
            resourceOverride.getReason(),
            resourceOverride.getCreatedAt(),
            resourceOverride.getUpdatedAt()
        );
    }
}
