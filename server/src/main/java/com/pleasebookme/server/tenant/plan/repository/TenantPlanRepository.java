package com.pleasebookme.server.tenant.plan.repository;

import com.pleasebookme.server.tenant.plan.entity.TenantPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface TenantPlanRepository extends JpaRepository<TenantPlanEntity, BigInteger> {
    boolean existsByCode(String code);
}
