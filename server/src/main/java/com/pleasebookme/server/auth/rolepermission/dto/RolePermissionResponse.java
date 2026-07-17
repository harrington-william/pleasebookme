package com.pleasebookme.server.auth.rolepermission.dto;

import com.pleasebookme.server.auth.rolepermission.entity.RolePermissionEntity;

import java.math.BigInteger;

public record RolePermissionResponse(
    BigInteger roleId,
    BigInteger permissionId
) {
    public static RolePermissionResponse from(RolePermissionEntity rolePermission) {
        return new RolePermissionResponse(
            rolePermission.getRolePermissionId().getRoleId(),
            rolePermission.getRolePermissionId().getPermissionId()
        );
    }
}
