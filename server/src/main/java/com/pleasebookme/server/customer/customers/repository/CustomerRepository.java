package com.pleasebookme.server.customer.customers.repository;

import com.pleasebookme.server.customer.customers.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerEntity, BigInteger> {
    boolean existsByTenantTenantIdAndEmail(
        BigInteger tenantId,
        String email
    );

    boolean existsByTenantTenantIdAndPhone(
        BigInteger tenantId,
        String phone
    );
}
