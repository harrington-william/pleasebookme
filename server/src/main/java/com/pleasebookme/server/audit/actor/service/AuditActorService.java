package com.pleasebookme.server.audit.actor.service;

import com.pleasebookme.server.audit.actor.dto.AuditActorRequest;
import com.pleasebookme.server.audit.actor.entity.AuditActorEntity;

import java.math.BigInteger;
import java.util.List;

public interface AuditActorService {
    AuditActorEntity createAuditActor(AuditActorRequest request);

    AuditActorEntity getAuditActorById(BigInteger auditActorId);

    List<AuditActorEntity> getAllAuditActors();
}
