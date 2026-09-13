package com.pleasebookme.server.organization.membership.repository;

import com.pleasebookme.server.organization.membership.entity.MembershipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MembershipRepository extends JpaRepository<MembershipEntity, BigInteger> {
    boolean existsByUserUserIdAndOrganizationOrganizationId(
        BigInteger userId,
        BigInteger organizationId
    );

    Optional<MembershipEntity> findByUserUserUidAndOrganizationOrganizationIdAndAccepted(
        UUID userUid,
        BigInteger organizationId,
        Boolean accepted
    );

    Optional<MembershipEntity> findByUserUserIdAndOrganizationOrganizationIdAndAccepted(
        BigInteger userId,
        BigInteger organizationId,
        Boolean accepted
    );

    List<MembershipEntity> findAllByUserUserIdAndAccepted(
        BigInteger userId,
        Boolean accepted
    );
}
