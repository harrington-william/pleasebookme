package com.pleasebookme.server.auth.userrole.service;

import com.pleasebookme.server.auth.userrole.dto.UserRoleRequest;
import com.pleasebookme.server.auth.userrole.entity.UserRoleEntity;

import java.math.BigInteger;
import java.util.List;

public interface UserRoleService {
    UserRoleEntity createUserRole(UserRoleRequest request);

    UserRoleEntity getUserRoleById(BigInteger userId, BigInteger roleId);

    List<UserRoleEntity> getAllUserRoles();

    void deleteUserRole(BigInteger userId, BigInteger roleId);
}
