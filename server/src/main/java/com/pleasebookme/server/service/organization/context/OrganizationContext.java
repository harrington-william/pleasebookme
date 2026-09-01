package com.pleasebookme.server.service.organization.context;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.organization.membership.entity.MembershipEntity;
import com.pleasebookme.server.organization.organizations.entity.OrganizationEntity;

import java.math.BigInteger;

public record OrganizationContext(
    UserEntity user,
    MembershipEntity membership,
    OrganizationEntity organization
) {
    public BigInteger userId() {
        return user.getUserId();
    }

    public BigInteger membershipId() {
        return membership.getMembershipId();
    }

    public BigInteger organizationId() {
        return organization.getOrganizationId();
    }
}
