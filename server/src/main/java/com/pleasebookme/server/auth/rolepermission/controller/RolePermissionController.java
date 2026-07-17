package com.pleasebookme.server.auth.rolepermission.controller;

import com.pleasebookme.server.auth.rolepermission.dto.RolePermissionRequest;
import com.pleasebookme.server.auth.rolepermission.dto.RolePermissionResponse;
import com.pleasebookme.server.auth.rolepermission.service.RolePermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/role-permissions")
@RequiredArgsConstructor
public class RolePermissionController {
    private final RolePermissionService rolePermissionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RolePermissionResponse createRolePermission(@Valid @RequestBody RolePermissionRequest request) {
        return RolePermissionResponse.from(rolePermissionService.createRolePermission(request));
    }

    @GetMapping("/{roleId}/{permissionId}")
    public RolePermissionResponse getRolePermission(
        @PathVariable BigInteger roleId,
        @PathVariable BigInteger permissionId
    ) {
        return RolePermissionResponse.from(rolePermissionService.getRolePermissionById(roleId, permissionId));
    }

    @GetMapping
    public List<RolePermissionResponse> getRolePermissions() {
        return rolePermissionService.getAllRolePermissions().stream()
            .map(RolePermissionResponse::from)
            .toList();
    }

    @DeleteMapping("/{roleId}/{permissionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRolePermission(
        @PathVariable BigInteger roleId,
        @PathVariable BigInteger permissionId
    ) {
        rolePermissionService.deleteRolePermission(roleId, permissionId);
    }
}
