package com.pleasebookme.server.organization.membership.repository;

import com.pleasebookme.server.organization.membership.entity.MembershipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface MembershipRepository extends JpaRepository<MembershipEntity, BigInteger> {
    boolean existsByUserUserIdAndOrganizationOrganizationId(
        BigInteger userId,
        BigInteger organizationId
    );
}
