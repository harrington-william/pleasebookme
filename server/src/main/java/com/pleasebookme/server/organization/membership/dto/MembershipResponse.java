package com.pleasebookme.server.organization.membership.dto;

import com.pleasebookme.server.organization.membership.entity.MembershipEntity;

import java.math.BigInteger;
import java.time.Instant;

public record MembershipResponse(
    BigInteger membershipId,
    BigInteger organizationId,
    BigInteger userId,
    Boolean accepted,
    Instant createdAt,
    Instant updatedAt
) {
    public static MembershipResponse from(MembershipEntity membership) {
        return new MembershipResponse(
            membership.getMembershipId(),
            membership.getOrganization().getOrganizationId(),
            membership.getUser().getUserId(),
            membership.getAccepted(),
            membership.getCreatedAt(),
            membership.getUpdatedAt()
        );
    }
}
