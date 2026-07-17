package com.pleasebookme.server.auth.userrole.dto;

import com.pleasebookme.server.auth.userrole.entity.UserRoleEntity;

import java.math.BigInteger;
import java.time.Instant;

public record UserRoleResponse(
    BigInteger userId,
    BigInteger roleId,
    Instant assignedAt
) {
    public static UserRoleResponse from(UserRoleEntity userRole) {
        return new UserRoleResponse(
            userRole.getUserRoleId().getUserId(),
            userRole.getUserRoleId().getRoleId(),
            userRole.getAssignedAt()
        );
    }
}
