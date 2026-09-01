package com.pleasebookme.server.resource.resources.dto;

import com.pleasebookme.server.resource.enums.ResourceStatus;

import java.math.BigInteger;

/**
 * Optional list filters. Every field is nullable and means "no constraint".
 */
public record ResourceFilter(
    BigInteger resourceTypeId,
    ResourceStatus status,
    String q
) {
    public static ResourceFilter none() {
        return new ResourceFilter(null, null, null);
    }
}
