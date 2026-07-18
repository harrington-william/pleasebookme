package com.pleasebookme.server.notification.template.service.impl;

import com.pleasebookme.server.notification.template.dto.NotificationTemplateRequest;
import com.pleasebookme.server.notification.template.entity.NotificationTemplateEntity;
import com.pleasebookme.server.notification.template.exception.DuplicateNotificationTemplateException;
import com.pleasebookme.server.notification.template.exception.NotificationTemplateNotFoundException;
import com.pleasebookme.server.notification.template.repository.NotificationTemplateRepository;
import com.pleasebookme.server.notification.template.service.NotificationTemplateService;
import com.pleasebookme.server.tenant.tenants.entity.TenantEntity;
import com.pleasebookme.server.tenant.tenants.exception.TenantNotFoundException;
import com.pleasebookme.server.tenant.tenants.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationTemplateServiceImpl implements NotificationTemplateService {
    private final NotificationTemplateRepository notificationTemplateRepository;
    private final TenantRepository tenantRepository;

    @Override
    public NotificationTemplateEntity createNotificationTemplate(NotificationTemplateRequest request) {
        if (notificationTemplateRepository.existsByTenantTenantIdAndCode(request.tenantId(), request.code())) {
            throw new DuplicateNotificationTemplateException("Code already exists for tenant: " + request.code());
        }

        TenantEntity tenant = resolveTenant(request.tenantId());

        NotificationTemplateEntity.NotificationTemplateEntityBuilder template = NotificationTemplateEntity.builder()
            .tenant(tenant)
            .code(request.code())
            .name(request.name())
            .channel(request.channel())
            .subjectTemplate(request.subjectTemplate())
            .bodyTemplate(request.bodyTemplate())
            .locale(request.locale());

        if (request.enabled() != null) template.enabled(request.enabled());
        if (request.version() != null) template.version(request.version());

        return notificationTemplateRepository.save(template.build());
    }

    @Override
    public NotificationTemplateEntity getNotificationTemplateById(BigInteger notificationTemplateId) {
        return notificationTemplateRepository.findById(notificationTemplateId)
            .orElseThrow(() -> new NotificationTemplateNotFoundException(
                "Notification template not found: " + notificationTemplateId
            ));
    }

    @Override
    public List<NotificationTemplateEntity> getAllNotificationTemplates() {
        return notificationTemplateRepository.findAll();
    }

    @Override
    public NotificationTemplateEntity updateNotificationTemplate(
        BigInteger notificationTemplateId,
        NotificationTemplateRequest request
    ) {
        NotificationTemplateEntity template = getNotificationTemplateById(notificationTemplateId);

        TenantEntity tenant = resolveTenant(request.tenantId());

        template.setTenant(tenant);
        template.setCode(request.code());
        template.setName(request.name());
        template.setChannel(request.channel());
        template.setSubjectTemplate(request.subjectTemplate());
        template.setBodyTemplate(request.bodyTemplate());
        template.setLocale(request.locale());

        if (request.enabled() != null) template.setEnabled(request.enabled());
        if (request.version() != null) template.setVersion(request.version());

        return notificationTemplateRepository.save(template);
    }

    @Override
    public void deleteNotificationTemplate(BigInteger notificationTemplateId) {
        notificationTemplateRepository.delete(getNotificationTemplateById(notificationTemplateId));
    }

    private TenantEntity resolveTenant(BigInteger tenantId) {
        if (tenantId == null) {
            return null;
        }

        return tenantRepository.findById(tenantId)
            .orElseThrow(() -> new TenantNotFoundException("Tenant not found: " + tenantId));
    }
}
