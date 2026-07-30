package com.pleasebookme.server.widget.widgets.service.impl;

import com.pleasebookme.server.core.service.repository.ServiceRepository;
import com.pleasebookme.server.tenant.tenants.entity.TenantEntity;
import com.pleasebookme.server.tenant.tenants.exception.TenantNotFoundException;
import com.pleasebookme.server.tenant.tenants.repository.TenantRepository;
import com.pleasebookme.server.widget.widgets.dto.WidgetRequest;
import com.pleasebookme.server.widget.widgets.entity.WidgetEntity;
import com.pleasebookme.server.widget.widgets.exception.DuplicateWidgetException;
import com.pleasebookme.server.widget.widgets.exception.WidgetNotFoundException;
import com.pleasebookme.server.widget.widgets.repository.WidgetRepository;
import com.pleasebookme.server.widget.widgets.service.WidgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WidgetServiceImpl implements WidgetService {
    private final WidgetRepository widgetRepository;
    private final TenantRepository tenantRepository;

    @Override
    public WidgetEntity createWidget(WidgetRequest request) {
        if (widgetRepository.existsByPublicKey(request.publicKey())) {
            throw new DuplicateWidgetException("Public key already exists: " + request.publicKey());
        }

        TenantEntity tenant = tenantRepository.findById(request.tenantId())
            .orElseThrow(() -> new TenantNotFoundException(
                "Tenant not found: " + request.tenantId()
            ));

        WidgetEntity.WidgetEntityBuilder widget = WidgetEntity.builder()
            .tenant(tenant)
            .name(request.name())
            .publicKey(request.publicKey())
            .secretKey(request.secretKey())
            .expiresAt(request.expiresAt())
            .lastUsedAt(request.lastUsedAt());

        if (request.status() != null) widget.status(request.status());
        if (request.type() != null) widget.type(request.type());
        if (request.originValidation() != null) widget.originValidation(request.originValidation());

        return widgetRepository.save(widget.build());
    }

    @Override
    public WidgetEntity getWidgetById(BigInteger widgetId) {
        return widgetRepository.findById(widgetId)
            .orElseThrow(() -> new WidgetNotFoundException(
                "Widget not found: " + widgetId
            ));
    }

    @Override
    public List<WidgetEntity> getAllWidgets() {
        return widgetRepository.findAll();
    }

    @Override
    public WidgetEntity updateWidget(
        BigInteger widgetId,
        WidgetRequest request
    ) {
        WidgetEntity widget = getWidgetById(widgetId);

        TenantEntity tenant = tenantRepository.findById(request.tenantId())
            .orElseThrow(() -> new TenantNotFoundException(
                "Tenant not found: " + request.tenantId()
            ));

        widget.setTenant(tenant);
        widget.setName(request.name());
        widget.setPublicKey(request.publicKey());
        widget.setSecretKey(request.secretKey());
        widget.setExpiresAt(request.expiresAt());
        widget.setLastUsedAt(request.lastUsedAt());

        if (request.status() != null) widget.setStatus(request.status());
        if (request.type() != null) widget.setType(request.type());
        if (request.originValidation() != null) widget.setOriginValidation(request.originValidation());

        return widgetRepository.save(widget);
    }

    @Override
    public void deleteWidget(BigInteger widgetId) {
        widgetRepository.delete(getWidgetById(widgetId));
    }
}
