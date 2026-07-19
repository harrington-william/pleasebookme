package com.pleasebookme.server.resource.maintenance.dto;

import com.pleasebookme.server.resource.maintenance.entity.ResourceMaintenanceEntity;

import java.math.BigInteger;
import java.time.Instant;

public record ResourceMaintenanceResponse(
    BigInteger resourceMaintenanceId,
    BigInteger resourceId,
    Instant startTime,
    Instant endTime,
    String reason,
    String status,
    Instant createdAt,
    Instant updatedAt
) {
    public static ResourceMaintenanceResponse from(ResourceMaintenanceEntity resourceMaintenance) {
        return new ResourceMaintenanceResponse(
            resourceMaintenance.getResourceMaintenanceId(),
            resourceMaintenance.getResource().getResourceId(),
            resourceMaintenance.getStartTime(),
            resourceMaintenance.getEndTime(),
            resourceMaintenance.getReason(),
            resourceMaintenance.getStatus(),
            resourceMaintenance.getCreatedAt(),
            resourceMaintenance.getUpdatedAt()
        );
    }
}
