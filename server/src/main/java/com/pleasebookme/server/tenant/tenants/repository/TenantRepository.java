package com.pleasebookme.server.tenant.tenants.repository;

import com.pleasebookme.server.tenant.tenants.entity.TenantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface TenantRepository extends JpaRepository<TenantEntity, BigInteger> {
    boolean existsBySlug(String slug);
}
