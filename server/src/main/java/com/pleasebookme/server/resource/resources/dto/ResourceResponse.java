package com.pleasebookme.server.resource.resources.dto;

import com.pleasebookme.server.resource.resources.entity.ResourceEntity;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

public record ResourceResponse(
    BigInteger resourceId,
    UUID resourceUid,
    BigInteger organizationId,
    BigInteger serviceId,
    BigInteger resourceTypeId,
    String name,
    String slug,
    String description,
    Integer capacity,
    String status,
    Boolean isBookable,
    Boolean isVirtual,
    Instant createdAt,
    Instant updatedAt
) {
    public static ResourceResponse from(ResourceEntity resource) {
        return new ResourceResponse(
            resource.getResourceId(),
            resource.getResourceUid(),
            resource.getOrganization().getOrganizationId(),
            resource.getService().getServiceId(),
            resource.getResourceType().getResourceTypeId(),
            resource.getName(),
            resource.getSlug(),
            resource.getDescription(),
            resource.getCapacity(),
            resource.getStatus(),
            resource.getIsBookable(),
            resource.getIsVirtual(),
            resource.getCreatedAt(),
            resource.getUpdatedAt()
        );
    }
}
