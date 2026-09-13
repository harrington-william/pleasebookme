package com.pleasebookme.server.resource.resourceservice.dto;

import com.pleasebookme.server.resource.resourceservice.entity.ResourceServiceEntity;

import java.math.BigInteger;
import java.time.Instant;

public record ResourceServiceResponse(
    BigInteger resourceId,
    BigInteger serviceId,
    Instant assignedAt
) {
    public static ResourceServiceResponse from(ResourceServiceEntity resourceService) {
        return new ResourceServiceResponse(
            resourceService.getResourceServiceId().getResourceId(),
            resourceService.getResourceServiceId().getServiceId(),
            resourceService.getAssignedAt()
        );
    }
}
