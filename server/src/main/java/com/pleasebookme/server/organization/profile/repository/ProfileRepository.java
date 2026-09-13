package com.pleasebookme.server.organization.profile.repository;

import com.pleasebookme.server.organization.profile.entity.ProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfileRepository extends JpaRepository<ProfileEntity, BigInteger> {
    boolean existsByUserUserIdAndOrganizationOrganizationId(
        BigInteger userId,
        BigInteger organizationId
    );

    boolean existsByUsernameAndOrganizationOrganizationId(
        String username,
        BigInteger organizationId
    );

    List<ProfileEntity> findAllByUserUserId(BigInteger userId);

    Optional<ProfileEntity> findByUserUserIdAndOrganizationOrganizationId(
        BigInteger userId,
        BigInteger organizationId
    );
}
