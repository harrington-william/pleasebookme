package com.pleasebookme.server.resource.resources.dto;

import com.pleasebookme.server.resource.resources.entity.ResourceEntity;

import java.math.BigInteger;

public record ResourceLookupResponse(
    BigInteger resourceId,
    String name
) {
    public static ResourceLookupResponse from(ResourceEntity resource) {
        return new ResourceLookupResponse(
            resource.getResourceId(),
            resource.getName()
        );
    }
}
