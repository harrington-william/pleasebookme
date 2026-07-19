package com.pleasebookme.server.auth.permission.controller;

import com.pleasebookme.server.auth.permission.dto.PermissionRequest;
import com.pleasebookme.server.auth.permission.dto.PermissionResponse;
import com.pleasebookme.server.auth.permission.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {
    private final PermissionService permissionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PermissionResponse createPermission(@Valid @RequestBody PermissionRequest request) {
        return PermissionResponse.from(permissionService.createPermission(request));
    }

    @GetMapping("/{permissionId}")
    public PermissionResponse getPermission(@PathVariable BigInteger permissionId) {
        return PermissionResponse.from(permissionService.getPermissionById(permissionId));
    }

    @GetMapping
    public List<PermissionResponse> getPermissions() {
        return permissionService.getAllPermissions().stream()
            .map(PermissionResponse::from)
            .toList();
    }

    @PutMapping("/{permissionId}")
    public PermissionResponse updatePermission(
        @PathVariable BigInteger permissionId,
        @Valid @RequestBody PermissionRequest request
    ) {
        return PermissionResponse.from(permissionService.updatePermission(permissionId, request));
    }

    @DeleteMapping("/{permissionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePermission(@PathVariable BigInteger permissionId) {
        permissionService.deletePermission(permissionId);
    }
}
