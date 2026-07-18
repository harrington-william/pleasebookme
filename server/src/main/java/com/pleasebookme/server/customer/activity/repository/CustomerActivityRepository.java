package com.pleasebookme.server.customer.activity.repository;

import com.pleasebookme.server.customer.activity.entity.CustomerActivityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface CustomerActivityRepository extends JpaRepository<CustomerActivityEntity, BigInteger> {
}
