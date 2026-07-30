package com.pleasebookme.server.organization.membership.service;

import com.pleasebookme.server.organization.membership.dto.MembershipRequest;
import com.pleasebookme.server.organization.membership.entity.MembershipEntity;

import java.math.BigInteger;
import java.util.List;

public interface MembershipService {
    MembershipEntity createMembership(MembershipRequest request);

    MembershipEntity getMembershipById(BigInteger membershipId);

    List<MembershipEntity> getAllMemberships();

    MembershipEntity updateMembership(
        BigInteger membershipId,
        MembershipRequest request
    );

    void deleteMembership(BigInteger membershipId);
}
