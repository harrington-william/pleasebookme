package com.pleasebookme.server.auth.role.service.impl;

import com.pleasebookme.server.auth.role.dto.RoleRequest;
import com.pleasebookme.server.auth.role.entity.RoleEntity;
import com.pleasebookme.server.auth.role.exception.DuplicateRoleException;
import com.pleasebookme.server.auth.role.exception.RoleNotFoundException;
import com.pleasebookme.server.auth.role.repository.RoleRepository;
import com.pleasebookme.server.auth.role.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;

    @Override
    public RoleEntity createRole(RoleRequest request) {
        if (roleRepository.existsByName(request.name())) {
            throw new DuplicateRoleException("Role name already exists: " + request.name());
        }

        RoleEntity role = RoleEntity.builder()
            .name(request.name())
            .description(request.description())
            .build();

        return roleRepository.save(role);
    }

    @Override
    public RoleEntity getRoleById(BigInteger roleId) {
        return roleRepository.findById(roleId)
            .orElseThrow(() -> new RoleNotFoundException(
                "Role not found: " + roleId
            ));
    }

    @Override
    public List<RoleEntity> getAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    public RoleEntity updateRole(
        BigInteger roleId,
        RoleRequest request
    ) {
        RoleEntity role = getRoleById(roleId);

        role.setName(request.name());
        role.setDescription(request.description());

        return roleRepository.save(role);
    }

    @Override
    public void deleteRole(BigInteger roleId) {
        roleRepository.delete(getRoleById(roleId));
    }
}
