package com.pleasebookme.server.organization.membershiprole.dto;

import com.pleasebookme.server.organization.membershiprole.entity.MembershipRoleEntity;

import java.math.BigInteger;
import java.time.Instant;

public record MembershipRoleResponse(
    BigInteger membershipId,
    BigInteger roleId,
    Instant assignedAt
) {
    public static MembershipRoleResponse from(MembershipRoleEntity membershipRole) {
        return new MembershipRoleResponse(
            membershipRole.getMembershipRoleId().getMembershipId(),
            membershipRole.getMembershipRoleId().getRoleId(),
            membershipRole.getAssignedAt()
        );
    }
}
