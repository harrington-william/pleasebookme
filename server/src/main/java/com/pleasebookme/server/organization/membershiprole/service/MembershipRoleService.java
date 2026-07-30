package com.pleasebookme.server.organization.membershiprole.service;

import com.pleasebookme.server.organization.membershiprole.dto.MembershipRoleRequest;
import com.pleasebookme.server.organization.membershiprole.entity.MembershipRoleEntity;

import java.math.BigInteger;
import java.util.List;

public interface MembershipRoleService {
    MembershipRoleEntity createMembershipRole(MembershipRoleRequest request);

    MembershipRoleEntity getMembershipRoleById(
        BigInteger membershipId,
        BigInteger roleId
    );

    List<MembershipRoleEntity> getAllMembershipRoles();

    void deleteMembershipRole(
        BigInteger membershipId,
        BigInteger roleId
    );
}
