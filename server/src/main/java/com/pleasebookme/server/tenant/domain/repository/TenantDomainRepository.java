package com.pleasebookme.server.tenant.domain.repository;

import com.pleasebookme.server.tenant.domain.entity.TenantDomainEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface TenantDomainRepository extends JpaRepository<TenantDomainEntity, BigInteger> {
    boolean existsByDomain(String domain);
}
