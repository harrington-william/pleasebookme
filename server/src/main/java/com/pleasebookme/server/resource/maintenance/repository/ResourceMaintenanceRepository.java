package com.pleasebookme.server.resource.maintenance.repository;

import com.pleasebookme.server.resource.maintenance.entity.ResourceMaintenanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface ResourceMaintenanceRepository extends JpaRepository<ResourceMaintenanceEntity, BigInteger> {
}
