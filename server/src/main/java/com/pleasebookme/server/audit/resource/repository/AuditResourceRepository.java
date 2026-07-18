package com.pleasebookme.server.audit.resource.repository;

import com.pleasebookme.server.audit.resource.entity.AuditResourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface AuditResourceRepository extends JpaRepository<AuditResourceEntity, BigInteger> {
}
