package com.pleasebookme.server.tenant.plan.repository;

import com.pleasebookme.server.tenant.plan.entity.TenantPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.Optional;

@Repository
public interface TenantPlanRepository extends JpaRepository<TenantPlanEntity, BigInteger> {
    boolean existsByCode(String code);

    Optional<TenantPlanEntity> findByCode(String code);
}
