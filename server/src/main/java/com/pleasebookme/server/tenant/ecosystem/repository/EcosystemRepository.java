package com.pleasebookme.server.tenant.ecosystem.repository;

import com.pleasebookme.server.tenant.ecosystem.entity.EcosystemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface EcosystemRepository extends JpaRepository<EcosystemEntity, BigInteger> {
    boolean existsByCode(String code);
}
