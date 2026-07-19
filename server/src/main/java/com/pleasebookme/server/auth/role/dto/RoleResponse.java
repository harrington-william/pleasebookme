package com.pleasebookme.server.auth.role.dto;

import com.pleasebookme.server.auth.role.entity.RoleEntity;

import java.math.BigInteger;
import java.time.Instant;

public record RoleResponse(
    BigInteger roleId,
    String name,
    String description,
    Instant createdAt,
    Instant updatedAt
) {
    public static RoleResponse from(RoleEntity role) {
        return new RoleResponse(
            role.getRoleId(),
            role.getName(),
            role.getDescription(),
            role.getCreatedAt(),
            role.getUpdatedAt()
        );
    }
}
