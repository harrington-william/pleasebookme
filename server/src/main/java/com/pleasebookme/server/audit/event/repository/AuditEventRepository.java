package com.pleasebookme.server.audit.event.repository;

import com.pleasebookme.server.audit.event.entity.AuditEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEventEntity, BigInteger> {
}
