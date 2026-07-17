package com.pleasebookme.server.auth.userrole.service.impl;

import com.pleasebookme.server.auth.role.entity.RoleEntity;
import com.pleasebookme.server.auth.role.exception.RoleNotFoundException;
import com.pleasebookme.server.auth.role.repository.RoleRepository;
import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.user.exception.UserNotFoundException;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import com.pleasebookme.server.auth.userrole.dto.UserRoleRequest;
import com.pleasebookme.server.auth.userrole.entity.UserRoleEntity;
import com.pleasebookme.server.auth.userrole.exception.DuplicateUserRoleException;
import com.pleasebookme.server.auth.userrole.exception.UserRoleNotFoundException;
import com.pleasebookme.server.auth.userrole.id.UserRoleId;
import com.pleasebookme.server.auth.userrole.repository.UserRoleRepository;
import com.pleasebookme.server.auth.userrole.service.UserRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserRoleServiceImpl implements UserRoleService {
    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public UserRoleEntity createUserRole(UserRoleRequest request) {
        UserRoleId userRoleId = new UserRoleId(request.userId(), request.roleId());

        if (userRoleRepository.existsById(userRoleId)) {
            throw new DuplicateUserRoleException(
                "Role " + request.roleId() + " already assigned to user " + request.userId()
            );
        }

        UserEntity user = userRepository.findById(request.userId())
            .orElseThrow(() -> new UserNotFoundException("User not found: " + request.userId()));

        RoleEntity role = roleRepository.findById(request.roleId())
            .orElseThrow(() -> new RoleNotFoundException("Role not found: " + request.roleId()));

        UserRoleEntity userRole = UserRoleEntity.builder()
            .user(user)
            .role(role)
            .build();

        return userRoleRepository.save(userRole);
    }

    @Override
    public UserRoleEntity getUserRoleById(BigInteger userId, BigInteger roleId) {
        return userRoleRepository.findById(new UserRoleId(userId, roleId))
            .orElseThrow(() -> new UserRoleNotFoundException(
                "Role " + roleId + " is not assigned to user " + userId
            ));
    }

    @Override
    public List<UserRoleEntity> getAllUserRoles() {
        return userRoleRepository.findAll();
    }

    @Override
    public void deleteUserRole(BigInteger userId, BigInteger roleId) {
        userRoleRepository.delete(getUserRoleById(userId, roleId));
    }
}
