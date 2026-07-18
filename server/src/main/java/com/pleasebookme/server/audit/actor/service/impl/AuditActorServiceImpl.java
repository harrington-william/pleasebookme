package com.pleasebookme.server.audit.actor.service.impl;

import com.pleasebookme.server.audit.actor.dto.AuditActorRequest;
import com.pleasebookme.server.audit.actor.entity.AuditActorEntity;
import com.pleasebookme.server.audit.actor.exception.AuditActorNotFoundException;
import com.pleasebookme.server.audit.actor.exception.DuplicateAuditActorException;
import com.pleasebookme.server.audit.actor.repository.AuditActorRepository;
import com.pleasebookme.server.audit.actor.service.AuditActorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditActorServiceImpl implements AuditActorService {
    private final AuditActorRepository auditActorRepository;

    @Override
    public AuditActorEntity createAuditActor(AuditActorRequest request) {
        if (request.userUid() != null && auditActorRepository.existsByUserUid(request.userUid())) {
            throw new DuplicateAuditActorException("User uid already exists: " + request.userUid());
        }

        if (request.attendeeId() != null && auditActorRepository.existsByAttendeeId(request.attendeeId())) {
            throw new DuplicateAuditActorException("Attendee id already exists: " + request.attendeeId());
        }

        if (request.email() != null && auditActorRepository.existsByEmail(request.email())) {
            throw new DuplicateAuditActorException("Email already exists: " + request.email());
        }

        AuditActorEntity auditActor = AuditActorEntity.builder()
            .actorType(request.actorType())
            .userUid(request.userUid())
            .membershipId(request.membershipId())
            .widgetUid(request.widgetUid())
            .apiKeyUid(request.apiKeyUid())
            .attendeeId(request.attendeeId())
            .systemName(request.systemName())
            .displayName(request.displayName())
            .email(request.email())
            .ipAddress(request.ipAddress())
            .userAgent(request.userAgent())
            .build();

        return auditActorRepository.save(auditActor);
    }

    @Override
    public AuditActorEntity getAuditActorById(BigInteger auditActorId) {
        return auditActorRepository.findById(auditActorId)
            .orElseThrow(() -> new AuditActorNotFoundException(
                "Audit actor not found: " + auditActorId
            ));
    }

    @Override
    public List<AuditActorEntity> getAllAuditActors() {
        return auditActorRepository.findAll();
    }
}
