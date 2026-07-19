package com.pleasebookme.server.audit.change.repository;

import com.pleasebookme.server.audit.change.entity.AuditChangeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface AuditChangeRepository extends JpaRepository<AuditChangeEntity, BigInteger> {
}
