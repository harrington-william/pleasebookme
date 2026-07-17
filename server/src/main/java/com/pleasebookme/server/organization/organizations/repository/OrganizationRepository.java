package com.pleasebookme.server.organization.organizations.repository;

import com.pleasebookme.server.organization.organizations.entity.OrganizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface OrganizationRepository extends JpaRepository<OrganizationEntity, BigInteger> {
    boolean existsBySlug(String slug);
}
