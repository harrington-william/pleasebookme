package com.pleasebookme.server.integration.syncjob.repository;

import com.pleasebookme.server.integration.syncjob.entity.SyncJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface SyncJobRepository extends JpaRepository<SyncJobEntity, BigInteger> {
}
