package com.pleasebookme.server.core.availability.repository;

import com.pleasebookme.server.core.availability.entity.AvailabilityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface AvailabilityRepository extends JpaRepository<AvailabilityEntity, BigInteger> {
}
