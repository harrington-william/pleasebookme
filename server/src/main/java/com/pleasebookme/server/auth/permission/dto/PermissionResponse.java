package com.pleasebookme.server.auth.permission.dto;

import com.pleasebookme.server.auth.permission.entity.PermissionEntity;

import java.math.BigInteger;
import java.time.Instant;

public record PermissionResponse(
    BigInteger permissionId,
    String name,
    String description,
    String resource,
    String action,
    String slug,
    Instant createdAt,
    Instant updatedAt
) {
    public static PermissionResponse from(PermissionEntity permission) {
        return new PermissionResponse(
            permission.getPermissionId(),
            permission.getName(),
            permission.getDescription(),
            permission.getResource(),
            permission.getAction(),
            permission.getSlug(),
            permission.getCreatedAt(),
            permission.getUpdatedAt()
        );
    }
}
