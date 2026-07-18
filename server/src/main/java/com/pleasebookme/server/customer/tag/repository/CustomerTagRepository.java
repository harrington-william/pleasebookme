package com.pleasebookme.server.customer.tag.repository;

import com.pleasebookme.server.customer.tag.entity.CustomerTagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface CustomerTagRepository extends JpaRepository<CustomerTagEntity, BigInteger> {
    boolean existsByCustomerCustomerIdAndTag(
        BigInteger customerId,
        String tag
    );
}
