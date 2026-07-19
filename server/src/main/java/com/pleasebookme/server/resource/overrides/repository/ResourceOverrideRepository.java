package com.pleasebookme.server.resource.overrides.repository;

import com.pleasebookme.server.resource.overrides.entity.ResourceOverrideEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface ResourceOverrideRepository extends JpaRepository<ResourceOverrideEntity, BigInteger> {
}
