package com.pleasebookme.server.organization.membershiprole.service.impl;

import com.pleasebookme.server.auth.role.entity.RoleEntity;
import com.pleasebookme.server.auth.role.exception.RoleNotFoundException;
import com.pleasebookme.server.auth.role.repository.RoleRepository;
import com.pleasebookme.server.organization.membership.entity.MembershipEntity;
import com.pleasebookme.server.organization.membership.exception.MembershipNotFoundException;
import com.pleasebookme.server.organization.membership.repository.MembershipRepository;
import com.pleasebookme.server.organization.membershiprole.dto.MembershipRoleRequest;
import com.pleasebookme.server.organization.membershiprole.entity.MembershipRoleEntity;
import com.pleasebookme.server.organization.membershiprole.exception.DuplicateMembershipRoleException;
import com.pleasebookme.server.organization.membershiprole.exception.MembershipRoleNotFoundException;
import com.pleasebookme.server.organization.membershiprole.id.MembershipRoleId;
import com.pleasebookme.server.organization.membershiprole.repository.MembershipRoleRepository;
import com.pleasebookme.server.organization.membershiprole.service.MembershipRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MembershipRoleServiceImpl implements MembershipRoleService {
    private final MembershipRoleRepository membershipRoleRepository;
    private final MembershipRepository membershipRepository;
    private final RoleRepository roleRepository;

    @Override
    public MembershipRoleEntity createMembershipRole(MembershipRoleRequest request) {
        MembershipRoleId membershipRoleId = new MembershipRoleId(request.membershipId(), request.roleId());

        if (membershipRoleRepository.existsById(membershipRoleId)) {
            throw new DuplicateMembershipRoleException(
                "Role " + request.roleId() + " already assigned to membership " + request.membershipId()
            );
        }

        MembershipEntity membership = membershipRepository.findById(request.membershipId())
            .orElseThrow(() -> new MembershipNotFoundException("Membership not found: " + request.membershipId()));

        RoleEntity role = roleRepository.findById(request.roleId())
            .orElseThrow(() -> new RoleNotFoundException("Role not found: " + request.roleId()));

        MembershipRoleEntity membershipRole = MembershipRoleEntity.builder()
            .membership(membership)
            .role(role)
            .build();

        return membershipRoleRepository.save(membershipRole);
    }

    @Override
    public MembershipRoleEntity getMembershipRoleById(
        BigInteger membershipId,
        BigInteger roleId
    ) {
        return membershipRoleRepository.findById(new MembershipRoleId(membershipId, roleId))
            .orElseThrow(() -> new MembershipRoleNotFoundException(
                "Role " + roleId + " is not assigned to membership " + membershipId
            ));
    }

    @Override
    public List<MembershipRoleEntity> getAllMembershipRoles() {
        return membershipRoleRepository.findAll();
    }

    @Override
    public void deleteMembershipRole(
        BigInteger membershipId,
        BigInteger roleId
    ) {
        membershipRoleRepository.delete(getMembershipRoleById(membershipId, roleId));
    }
}
