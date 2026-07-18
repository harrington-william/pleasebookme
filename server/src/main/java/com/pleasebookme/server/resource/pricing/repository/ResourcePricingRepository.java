package com.pleasebookme.server.resource.pricing.repository;

import com.pleasebookme.server.resource.pricing.entity.ResourcePricingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface ResourcePricingRepository extends JpaRepository<ResourcePricingEntity, BigInteger> {
}
