package com.pleasebookme.server.auth.permission.service;

import com.pleasebookme.server.auth.permission.dto.PermissionRequest;
import com.pleasebookme.server.auth.permission.entity.PermissionEntity;

import java.math.BigInteger;
import java.util.List;

public interface PermissionService {
    PermissionEntity createPermission(PermissionRequest request);

    PermissionEntity getPermissionById(BigInteger permissionId);

    List<PermissionEntity> getAllPermissions();

    PermissionEntity updatePermission(BigInteger permissionId, PermissionRequest request);

    void deletePermission(BigInteger permissionId);
}
