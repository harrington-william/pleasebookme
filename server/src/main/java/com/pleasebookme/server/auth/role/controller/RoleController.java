package com.pleasebookme.server.auth.role.controller;

import com.pleasebookme.server.auth.role.dto.RoleRequest;
import com.pleasebookme.server.auth.role.dto.RoleResponse;
import com.pleasebookme.server.auth.role.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoleResponse createRole(@Valid @RequestBody RoleRequest request) {
        return RoleResponse.from(roleService.createRole(request));
    }

    @GetMapping("/{roleId}")
    public RoleResponse getRole(@PathVariable BigInteger roleId) {
        return RoleResponse.from(roleService.getRoleById(roleId));
    }

    @GetMapping
    public List<RoleResponse> getRoles() {
        return roleService.getAllRoles().stream()
            .map(RoleResponse::from)
            .toList();
    }

    @PutMapping("/{roleId}")
    public RoleResponse updateRole(
        @PathVariable BigInteger roleId,
        @Valid @RequestBody RoleRequest request
    ) {
        return RoleResponse.from(roleService.updateRole(roleId, request));
    }

    @DeleteMapping("/{roleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRole(@PathVariable BigInteger roleId) {
        roleService.deleteRole(roleId);
    }
}
