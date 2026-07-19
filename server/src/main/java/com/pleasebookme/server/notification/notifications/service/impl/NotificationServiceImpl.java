package com.pleasebookme.server.notification.notifications.service.impl;

import com.pleasebookme.server.notification.channel.entity.NotificationChannelEntity;
import com.pleasebookme.server.notification.channel.exception.NotificationChannelNotFoundException;
import com.pleasebookme.server.notification.channel.repository.NotificationChannelRepository;
import com.pleasebookme.server.notification.notifications.dto.NotificationRequest;
import com.pleasebookme.server.notification.notifications.entity.NotificationEntity;
import com.pleasebookme.server.notification.notifications.exception.NotificationNotFoundException;
import com.pleasebookme.server.notification.notifications.repository.NotificationRepository;
import com.pleasebookme.server.notification.notifications.service.NotificationService;
import com.pleasebookme.server.notification.template.entity.NotificationTemplateEntity;
import com.pleasebookme.server.notification.template.exception.NotificationTemplateNotFoundException;
import com.pleasebookme.server.notification.template.repository.NotificationTemplateRepository;
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
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final TenantRepository tenantRepository;
    private final OrganizationRepository organizationRepository;
    private final NotificationTemplateRepository notificationTemplateRepository;
    private final NotificationChannelRepository notificationChannelRepository;

    @Override
    public NotificationEntity createNotification(NotificationRequest request) {
        TenantEntity tenant = tenantRepository.findById(request.tenantId())
            .orElseThrow(() -> new TenantNotFoundException("Tenant not found: " + request.tenantId()));

        OrganizationEntity organization = organizationRepository.findById(request.organizationId())
            .orElseThrow(() -> new OrganizationNotFoundException("Organization not found: " + request.organizationId()));

        NotificationTemplateEntity template = notificationTemplateRepository.findById(request.templateId())
            .orElseThrow(() -> new NotificationTemplateNotFoundException("Notification template not found: " + request.templateId()));

        NotificationChannelEntity channel = notificationChannelRepository.findById(request.channelId())
            .orElseThrow(() -> new NotificationChannelNotFoundException("Notification channel not found: " + request.channelId()));

        NotificationEntity.NotificationEntityBuilder notification = NotificationEntity.builder()
            .tenant(tenant)
            .organization(organization)
            .recipientType(request.recipientType())
            .recipientUid(request.recipientUid())
            .template(template)
            .channel(channel)
            .subject(request.subject())
            .content(request.content())
            .locale(request.locale())
            .scheduledAt(request.scheduledAt())
            .sentAt(request.sentAt());

        if (request.status() != null) notification.status(request.status());
        if (request.priority() != null) notification.priority(request.priority());

        return notificationRepository.save(notification.build());
    }

    @Override
    public NotificationEntity getNotificationById(BigInteger notificationId) {
        return notificationRepository.findById(notificationId)
            .orElseThrow(() -> new NotificationNotFoundException(
                "Notification not found: " + notificationId
            ));
    }

    @Override
    public List<NotificationEntity> getAllNotifications() {
        return notificationRepository.findAll();
    }

    @Override
    public NotificationEntity updateNotification(
        BigInteger notificationId,
        NotificationRequest request
    ) {
        NotificationEntity notification = getNotificationById(notificationId);

        TenantEntity tenant = tenantRepository.findById(request.tenantId())
            .orElseThrow(() -> new TenantNotFoundException("Tenant not found: " + request.tenantId()));

        OrganizationEntity organization = organizationRepository.findById(request.organizationId())
            .orElseThrow(() -> new OrganizationNotFoundException("Organization not found: " + request.organizationId()));

        NotificationTemplateEntity template = notificationTemplateRepository.findById(request.templateId())
            .orElseThrow(() -> new NotificationTemplateNotFoundException("Notification template not found: " + request.templateId()));

        NotificationChannelEntity channel = notificationChannelRepository.findById(request.channelId())
            .orElseThrow(() -> new NotificationChannelNotFoundException("Notification channel not found: " + request.channelId()));

        notification.setTenant(tenant);
        notification.setOrganization(organization);
        notification.setRecipientType(request.recipientType());
        notification.setRecipientUid(request.recipientUid());
        notification.setTemplate(template);
        notification.setChannel(channel);
        notification.setSubject(request.subject());
        notification.setContent(request.content());
        notification.setLocale(request.locale());
        notification.setScheduledAt(request.scheduledAt());
        notification.setSentAt(request.sentAt());

        if (request.status() != null) notification.setStatus(request.status());
        if (request.priority() != null) notification.setPriority(request.priority());

        return notificationRepository.save(notification);
    }

    @Override
    public void deleteNotification(BigInteger notificationId) {
        notificationRepository.delete(getNotificationById(notificationId));
    }
}
