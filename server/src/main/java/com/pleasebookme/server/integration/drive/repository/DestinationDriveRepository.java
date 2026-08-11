package com.pleasebookme.server.integration.drive.repository;

import com.pleasebookme.server.integration.drive.entity.DestinationDriveEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface DestinationDriveRepository extends JpaRepository<DestinationDriveEntity, BigInteger> {
}
