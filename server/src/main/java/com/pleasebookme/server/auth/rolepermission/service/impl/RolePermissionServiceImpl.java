package com.pleasebookme.server.auth.rolepermission.service.impl;

import com.pleasebookme.server.auth.permission.entity.PermissionEntity;
import com.pleasebookme.server.auth.permission.exception.PermissionNotFoundException;
import com.pleasebookme.server.auth.permission.repository.PermissionRepository;
import com.pleasebookme.server.auth.role.entity.RoleEntity;
import com.pleasebookme.server.auth.role.exception.RoleNotFoundException;
import com.pleasebookme.server.auth.role.repository.RoleRepository;
import com.pleasebookme.server.auth.rolepermission.dto.RolePermissionRequest;
import com.pleasebookme.server.auth.rolepermission.entity.RolePermissionEntity;
import com.pleasebookme.server.auth.rolepermission.exception.DuplicateRolePermissionException;
import com.pleasebookme.server.auth.rolepermission.exception.RolePermissionNotFoundException;
import com.pleasebookme.server.auth.rolepermission.id.RolePermissionId;
import com.pleasebookme.server.auth.rolepermission.repository.RolePermissionRepository;
import com.pleasebookme.server.auth.rolepermission.service.RolePermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RolePermissionServiceImpl implements RolePermissionService {
    private final RolePermissionRepository rolePermissionRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public RolePermissionEntity createRolePermission(RolePermissionRequest request) {
        RolePermissionId rolePermissionId = new RolePermissionId(request.roleId(), request.permissionId());

        if (rolePermissionRepository.existsById(rolePermissionId)) {
            throw new DuplicateRolePermissionException(
                "Permission " + request.permissionId() + " already assigned to role " + request.roleId()
            );
        }

        RoleEntity role = roleRepository.findById(request.roleId())
            .orElseThrow(() -> new RoleNotFoundException("Role not found: " + request.roleId()));

        PermissionEntity permission = permissionRepository.findById(request.permissionId())
            .orElseThrow(() -> new PermissionNotFoundException("Permission not found: " + request.permissionId()));

        RolePermissionEntity rolePermission = RolePermissionEntity.builder()
            .role(role)
            .permission(permission)
            .build();

        return rolePermissionRepository.save(rolePermission);
    }

    @Override
    public RolePermissionEntity getRolePermissionById(BigInteger roleId, BigInteger permissionId) {
        return rolePermissionRepository.findById(new RolePermissionId(roleId, permissionId))
            .orElseThrow(() -> new RolePermissionNotFoundException(
                "Permission " + permissionId + " is not assigned to role " + roleId
            ));
    }

    @Override
    public List<RolePermissionEntity> getAllRolePermissions() {
        return rolePermissionRepository.findAll();
    }

    @Override
    public void deleteRolePermission(BigInteger roleId, BigInteger permissionId) {
        rolePermissionRepository.delete(getRolePermissionById(roleId, permissionId));
    }
}
