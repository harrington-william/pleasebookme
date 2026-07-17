package com.pleasebookme.server.auth.role.service;

import com.pleasebookme.server.auth.role.dto.RoleRequest;
import com.pleasebookme.server.auth.role.entity.RoleEntity;

import java.math.BigInteger;
import java.util.List;

public interface RoleService {
    RoleEntity createRole(RoleRequest request);

    RoleEntity getRoleById(BigInteger roleId);

    List<RoleEntity> getAllRoles();

    RoleEntity updateRole(BigInteger roleId, RoleRequest request);

    void deleteRole(BigInteger roleId);
}
