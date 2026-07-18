package com.pleasebookme.server.audit.actor.repository;

import com.pleasebookme.server.audit.actor.entity.AuditActorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface AuditActorRepository extends JpaRepository<AuditActorEntity, BigInteger> {
    boolean existsByUserUid(String userUid);

    boolean existsByAttendeeId(BigInteger attendeeId);

    boolean existsByEmail(String email);
}
