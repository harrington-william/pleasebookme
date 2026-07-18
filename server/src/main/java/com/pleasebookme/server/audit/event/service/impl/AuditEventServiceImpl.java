package com.pleasebookme.server.audit.event.service.impl;

import com.pleasebookme.server.audit.actor.entity.AuditActorEntity;
import com.pleasebookme.server.audit.actor.exception.AuditActorNotFoundException;
import com.pleasebookme.server.audit.actor.repository.AuditActorRepository;
import com.pleasebookme.server.audit.event.dto.AuditEventRequest;
import com.pleasebookme.server.audit.event.entity.AuditEventEntity;
import com.pleasebookme.server.audit.event.exception.AuditEventNotFoundException;
import com.pleasebookme.server.audit.event.repository.AuditEventRepository;
import com.pleasebookme.server.audit.event.service.AuditEventService;
import com.pleasebookme.server.organization.organizations.entity.OrganizationEntity;
import com.pleasebookme.server.organization.organizations.exception.OrganizationNotFoundException;
import com.pleasebookme.server.organization.organizations.repository.OrganizationRepository;
import com.pleasebookme.server.tenant.tenants.entity.TenantEntity;
import com.pleasebookme.server.tenant.tenants.exception.TenantNotFoundException;
import com.pleasebookme.server.tenant.tenants.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditEventServiceImpl implements AuditEventService {
    private final AuditEventRepository auditEventRepository;
    private final TenantRepository tenantRepository;
    private final OrganizationRepository organizationRepository;
    private final AuditActorRepository auditActorRepository;

    @Override
    public AuditEventEntity createAuditEvent(AuditEventRequest request) {
        TenantEntity tenant = resolveTenant(request.tenantId());
        OrganizationEntity organization = resolveOrganization(request.organizationId());
        AuditActorEntity actor = auditActorRepository.findById(request.actorId())
            .orElseThrow(() -> new AuditActorNotFoundException("Audit actor not found: " + request.actorId()));

        AuditEventEntity.AuditEventEntityBuilder auditEvent = AuditEventEntity.builder()
            .correlationId(request.correlationId())
            .requestId(request.requestId())
            .traceId(request.traceId())
            .tenant(tenant)
            .organization(organization)
            .actor(actor)
            .eventDomain(request.eventDomain())
            .eventType(request.eventType())
            .action(request.action())
            .severity(request.severity())
            .status(request.status());

        if (request.eventVersion() != null) auditEvent.eventVersion(request.eventVersion());

        return auditEventRepository.save(auditEvent.build());
    }

    @Override
    public AuditEventEntity getAuditEventById(BigInteger auditEventId) {
        return auditEventRepository.findById(auditEventId)
            .orElseThrow(() -> new AuditEventNotFoundException(
                "Audit event not found: " + auditEventId
            ));
    }

    @Override
    public List<AuditEventEntity> getAllAuditEvents() {
        return auditEventRepository.findAll();
    }

    private TenantEntity resolveTenant(BigInteger tenantId) {
        if (tenantId == null) {
            return null;
        }

        return tenantRepository.findById(tenantId)
            .orElseThrow(() -> new TenantNotFoundException("Tenant not found: " + tenantId));
    }

    private OrganizationEntity resolveOrganization(BigInteger organizationId) {
        if (organizationId == null) {
            return null;
        }

        return organizationRepository.findById(organizationId)
            .orElseThrow(() -> new OrganizationNotFoundException("Organization not found: " + organizationId));
    }
}
