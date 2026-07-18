package com.pleasebookme.server.resource.type.dto;

import com.pleasebookme.server.resource.type.entity.ResourceTypeEntity;

import java.math.BigInteger;
import java.time.Instant;

public record ResourceTypeResponse(
    BigInteger resourceTypeId,
    BigInteger organizationId,
    String name,
    String description,
    String icon,
    Instant createdAt,
    Instant updatedAt
) {
    public static ResourceTypeResponse from(ResourceTypeEntity resourceType) {
        return new ResourceTypeResponse(
            resourceType.getResourceTypeId(),
            resourceType.getOrganization().getOrganizationId(),
            resourceType.getName(),
            resourceType.getDescription(),
            resourceType.getIcon(),
            resourceType.getCreatedAt(),
            resourceType.getUpdatedAt()
        );
    }
}
