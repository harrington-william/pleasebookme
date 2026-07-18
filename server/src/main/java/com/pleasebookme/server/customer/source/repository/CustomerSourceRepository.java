package com.pleasebookme.server.customer.source.repository;

import com.pleasebookme.server.customer.source.entity.CustomerSourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface CustomerSourceRepository extends JpaRepository<CustomerSourceEntity, BigInteger> {
}
