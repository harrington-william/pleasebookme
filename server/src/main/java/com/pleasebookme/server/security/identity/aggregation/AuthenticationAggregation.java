package com.pleasebookme.server.security.identity.aggregation;

import com.pleasebookme.server.auth.permission.entity.PermissionEntity;
import com.pleasebookme.server.auth.role.entity.RoleEntity;
import com.pleasebookme.server.auth.user.entity.UserEntity;

import java.util.Set;

public record AuthenticationAggregation(
    UserEntity user,

    Set<RoleEntity> roles,

    Set<PermissionEntity> permissions
) {}
