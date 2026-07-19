package com.pleasebookme.server.auth.permission.service.impl;

import com.pleasebookme.server.auth.permission.dto.PermissionRequest;
import com.pleasebookme.server.auth.permission.entity.PermissionEntity;
import com.pleasebookme.server.auth.permission.exception.DuplicatePermissionException;
import com.pleasebookme.server.auth.permission.exception.PermissionNotFoundException;
import com.pleasebookme.server.auth.permission.repository.PermissionRepository;
import com.pleasebookme.server.auth.permission.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {
    private final PermissionRepository permissionRepository;

    @Override
    public PermissionEntity createPermission(PermissionRequest request) {
        if (permissionRepository.existsBySlug(request.slug())) {
            throw new DuplicatePermissionException("Permission slug already exists: " + request.slug());
        }

        PermissionEntity permission = PermissionEntity.builder()
            .name(request.name())
            .description(request.description())
            .resource(request.resource())
            .action(request.action())
            .slug(request.slug())
            .build();

        return permissionRepository.save(permission);
    }

    @Override
    public PermissionEntity getPermissionById(BigInteger permissionId) {
        return permissionRepository.findById(permissionId)
            .orElseThrow(() -> new PermissionNotFoundException(
                "Permission not found: " + permissionId
            ));
    }

    @Override
    public List<PermissionEntity> getAllPermissions() {
        return permissionRepository.findAll();
    }

    @Override
    public PermissionEntity updatePermission(
        BigInteger permissionId,
        PermissionRequest request
    ) {
        PermissionEntity permission = getPermissionById(permissionId);

        permission.setName(request.name());
        permission.setDescription(request.description());
        permission.setResource(request.resource());
        permission.setAction(request.action());
        permission.setSlug(request.slug());

        return permissionRepository.save(permission);
    }

    @Override
    public void deletePermission(BigInteger permissionId) {
        permissionRepository.delete(getPermissionById(permissionId));
    }
}
