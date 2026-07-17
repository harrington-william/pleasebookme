package com.pleasebookme.server.auth.userrole.controller;

import com.pleasebookme.server.auth.userrole.dto.UserRoleRequest;
import com.pleasebookme.server.auth.userrole.dto.UserRoleResponse;
import com.pleasebookme.server.auth.userrole.service.UserRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/user-roles")
@RequiredArgsConstructor
public class UserRoleController {
    private final UserRoleService userRoleService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserRoleResponse createUserRole(@Valid @RequestBody UserRoleRequest request) {
        return UserRoleResponse.from(userRoleService.createUserRole(request));
    }

    @GetMapping("/{userId}/{roleId}")
    public UserRoleResponse getUserRole(
        @PathVariable BigInteger userId,
        @PathVariable BigInteger roleId
    ) {
        return UserRoleResponse.from(userRoleService.getUserRoleById(userId, roleId));
    }

    @GetMapping
    public List<UserRoleResponse> getUserRoles() {
        return userRoleService.getAllUserRoles().stream()
            .map(UserRoleResponse::from)
            .toList();
    }

    @DeleteMapping("/{userId}/{roleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserRole(
        @PathVariable BigInteger userId,
        @PathVariable BigInteger roleId
    ) {
        userRoleService.deleteUserRole(userId, roleId);
    }
}
