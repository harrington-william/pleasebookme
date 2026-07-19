package com.pleasebookme.server.auth.rolepermission.service;

import com.pleasebookme.server.auth.rolepermission.dto.RolePermissionRequest;
import com.pleasebookme.server.auth.rolepermission.entity.RolePermissionEntity;

import java.math.BigInteger;
import java.util.List;

public interface RolePermissionService {
    RolePermissionEntity createRolePermission(RolePermissionRequest request);

    RolePermissionEntity getRolePermissionById(BigInteger roleId, BigInteger permissionId);

    List<RolePermissionEntity> getAllRolePermissions();

    void deleteRolePermission(BigInteger roleId, BigInteger permissionId);
}
