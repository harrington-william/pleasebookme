package com.pleasebookme.server.resource.attribute.dto;

import com.pleasebookme.server.resource.attribute.entity.ResourceAttributesEntity;

import java.math.BigInteger;
import java.time.Instant;

public record ResourceAttributeResponse(
    BigInteger resourceId,
    String key,
    String value,
    Instant createdAt,
    Instant updatedAt
) {
    public static ResourceAttributeResponse from(ResourceAttributesEntity resourceAttribute) {
        return new ResourceAttributeResponse(
            resourceAttribute.getResourceAttributesId().getResourceId(),
            resourceAttribute.getResourceAttributesId().getKey(),
            resourceAttribute.getValue(),
            resourceAttribute.getCreatedAt(),
            resourceAttribute.getUpdatedAt()
        );
    }
}
