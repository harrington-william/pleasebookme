package com.pleasebookme.server.widget.widgets.service.impl;

import com.pleasebookme.server.service.organization.context.OrganizationContext;
import com.pleasebookme.server.service.organization.service.CurrentOrganizationProvider;
import com.pleasebookme.server.service.tenant.service.CurrentTenantProvider;
import com.pleasebookme.server.tenant.tenants.entity.TenantEntity;
import com.pleasebookme.server.widget.enums.WidgetStatus;
import com.pleasebookme.server.widget.widgetorigin.entity.WidgetOriginEntity;
import com.pleasebookme.server.widget.widgetorigin.repository.WidgetOriginRepository;
import com.pleasebookme.server.widget.widgetorigin.support.OriginNormalizer;
import com.pleasebookme.server.widget.widgets.credential.WidgetCredentialGenerator;
import com.pleasebookme.server.widget.widgets.credential.WidgetCredentialPair;
import com.pleasebookme.server.widget.widgets.dto.WidgetCreateRequest;
import com.pleasebookme.server.widget.widgets.dto.WidgetCredentialRequest;
import com.pleasebookme.server.widget.widgets.dto.WidgetDetail;
import com.pleasebookme.server.widget.widgets.dto.WidgetFilter;
import com.pleasebookme.server.widget.widgets.dto.WidgetStatsResponse;
import com.pleasebookme.server.widget.widgets.dto.WidgetUpdateRequest;
import com.pleasebookme.server.widget.widgets.entity.WidgetEntity;
import com.pleasebookme.server.widget.widgets.exception.DuplicateWidgetException;
import com.pleasebookme.server.widget.widgets.exception.WidgetNotFoundException;
import com.pleasebookme.server.widget.widgets.exception.WidgetRevokedException;
import com.pleasebookme.server.widget.widgets.repository.WidgetRepository;
import com.pleasebookme.server.widget.widgets.service.WidgetService;
import com.pleasebookme.server.widget.widgets.specification.WidgetSort;
import com.pleasebookme.server.widget.widgets.specification.WidgetSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WidgetServiceImpl implements WidgetService {
    private final WidgetRepository widgetRepository;
    private final WidgetOriginRepository widgetOriginRepository;
    private final CurrentOrganizationProvider currentOrganizationProvider;
    private final CurrentTenantProvider currentTenantProvider;
    private final WidgetCredentialGenerator widgetCredentialGenerator;
    private final PasswordEncoder passwordEncoder;

    @Override
    public WidgetCredentialPair generateCredentials() {
        return widgetCredentialGenerator.generate();
    }

    @Override
    @Transactional
    public WidgetDetail createWidget(WidgetCreateRequest request) {
        currentOrganizationProvider.requireCurrent();
        TenantEntity tenant = currentTenantProvider.requireCurrent();
        WidgetCredentialRequest credentials = request.credentials();

        ensurePublicKeyAvailable(credentials.publicKey());
        String normalizedOrigin = OriginNormalizer.normalize(request.origin());

        WidgetEntity.WidgetEntityBuilder widget = WidgetEntity.builder()
            .tenant(tenant)
            .name(request.name())
            .status(WidgetStatus.ACTIVE)
            .originValidation(normalizedOrigin != null)
            .publicKey(credentials.publicKey())
            .secretKey(passwordEncoder.encode(credentials.secretKey()))
            .issuedAt(Instant.now());

        if (request.type() != null) widget.type(request.type());

        WidgetEntity createdWidget = widgetRepository.save(widget.build());
        WidgetOriginEntity origin = upsertOrigin(createdWidget, normalizedOrigin);

        return new WidgetDetail(createdWidget, origin);
    }

    @Override
    @Transactional(readOnly = true)
    public WidgetDetail getWidgetById(BigInteger widgetId) {
        WidgetEntity widget = findWidgetOrThrow(widgetId);
        return new WidgetDetail(widget, findPrimaryOrigin(widgetId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WidgetDetail> getWidgetsByOrganizationId(
        BigInteger organizationId,
        WidgetFilter filter,
        Pageable pageable
    ) {
        OrganizationContext organizationContext = currentOrganizationProvider.requireCurrent();
        Pageable safePageable = WidgetSort.sanitize(pageable);

        if (!organizationContext.organizationId().equals(organizationId)) {
            return Page.empty(safePageable);
        }

        TenantEntity tenant = currentTenantProvider.requireByOrganizationId(organizationId);
        WidgetFilter appliedFilter = filter != null ? filter : WidgetFilter.none();

        Specification<WidgetEntity> specification = Specification.allOf(
            WidgetSpecifications.hasTenant(tenant.getTenantId()),
            // Soft-deleted widgets stay excluded even when status=REVOKED is requested.
            WidgetSpecifications.isNotRevoked(),
            WidgetSpecifications.hasType(appliedFilter.type()),
            WidgetSpecifications.hasStatus(appliedFilter.status())
        );

        Page<WidgetEntity> widgets = widgetRepository.findAll(specification, safePageable);
        List<BigInteger> widgetIds = widgets.getContent().stream()
            .map(WidgetEntity::getWidgetId)
            .toList();

        Map<BigInteger, WidgetOriginEntity> originsByWidgetId = widgetOriginRepository
            .findAllByWidgetWidgetIdIn(widgetIds)
            .stream()
            .collect(Collectors.toMap(
                origin -> origin.getWidget().getWidgetId(),
                Function.identity(),
                WidgetServiceImpl::lowestId
            ));

        return widgets.map(widget -> new WidgetDetail(
            widget,
            originsByWidgetId.get(widget.getWidgetId())
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public WidgetStatsResponse getWidgetStatsByOrganizationId(BigInteger organizationId) {
        OrganizationContext organizationContext = currentOrganizationProvider.requireCurrent();

        if (!organizationContext.organizationId().equals(organizationId)) {
            return new WidgetStatsResponse(0, 0, 0, 0);
        }

        BigInteger tenantId = currentTenantProvider
            .requireByOrganizationId(organizationId)
            .getTenantId();

        return new WidgetStatsResponse(
            widgetRepository.countByTenantTenantIdAndStatusNot(tenantId, WidgetStatus.REVOKED),
            countByStatus(tenantId, WidgetStatus.ACTIVE),
            countByStatus(tenantId, WidgetStatus.DISABLED),
            countByStatus(tenantId, WidgetStatus.REVOKED)
        );
    }

    @Override
    @Transactional
    public WidgetDetail updateWidget(
        BigInteger widgetId,
        WidgetUpdateRequest request
    ) {
        WidgetEntity widget = findWidgetOrThrow(widgetId);

        if (widget.getStatus() == WidgetStatus.REVOKED) {
            throw new WidgetRevokedException("Widget is revoked: " + widgetId);
        }

        widget.setName(request.name());
        if (request.type() != null) widget.setType(request.type());

        // REGISTERING belongs to an issuance flow and REVOKED is reserved for DELETE.
        if (request.status() == WidgetStatus.ACTIVE || request.status() == WidgetStatus.DISABLED) {
            widget.setStatus(request.status());
        }

        if (request.credentials() != null) {
            rotateCredentials(widget, request.credentials());
        }

        String normalizedOrigin = OriginNormalizer.normalize(request.origin());
        WidgetOriginEntity origin = upsertOrigin(widget, normalizedOrigin);
        WidgetEntity updatedWidget = widgetRepository.save(widget);

        return new WidgetDetail(updatedWidget, origin);
    }

    @Override
    @Transactional
    public void deleteWidget(BigInteger widgetId) {
        WidgetEntity widget = findWidgetOrThrow(widgetId);

        if (widget.getStatus() == WidgetStatus.REVOKED) {
            return;
        }

        widget.setStatus(WidgetStatus.REVOKED);
        widgetRepository.save(widget);
    }

    private void ensurePublicKeyAvailable(String publicKey) {
        if (widgetRepository.existsByPublicKey(publicKey)) {
            throw new DuplicateWidgetException("Public key already exists: " + publicKey);
        }
    }

    private WidgetEntity findWidgetOrThrow(BigInteger widgetId) {
        return widgetRepository.findById(widgetId)
            .orElseThrow(() -> new WidgetNotFoundException("Widget not found: " + widgetId));
    }

    private long countByStatus(
        BigInteger tenantId,
        WidgetStatus status
    ) {
        return widgetRepository.countByTenantTenantIdAndStatus(tenantId, status);
    }

    private void rotateCredentials(
        WidgetEntity widget,
        WidgetCredentialRequest credentials
    ) {
        if (!widget.getPublicKey().equals(credentials.publicKey())) {
            ensurePublicKeyAvailable(credentials.publicKey());
        }

        widget.setPublicKey(credentials.publicKey());
        widget.setSecretKey(passwordEncoder.encode(credentials.secretKey()));
        widget.setIssuedAt(Instant.now());
    }

    /**
     * Widget configuration owns one effective allow-list origin. Keeping its child
     * write here makes the parent flag and origin row commit atomically.
     */
    private WidgetOriginEntity upsertOrigin(
        WidgetEntity widget,
        String normalizedOrigin
    ) {
        List<WidgetOriginEntity> existing = widgetOriginRepository
            .findAllByWidgetWidgetIdIn(List.of(widget.getWidgetId()));

        if (normalizedOrigin == null) {
            if (!existing.isEmpty()) widgetOriginRepository.deleteAll(existing);
            widget.setOriginValidation(false);
            return null;
        }

        widget.setOriginValidation(true);
        WidgetOriginEntity matching = existing.stream()
            .filter(origin -> normalizedOrigin.equals(origin.getOrigin()))
            .min(Comparator.comparing(WidgetOriginEntity::getWidgetOriginId))
            .orElse(null);
        List<WidgetOriginEntity> obsolete = existing.stream()
            .filter(origin -> !normalizedOrigin.equals(origin.getOrigin()))
            .toList();

        if (!obsolete.isEmpty()) widgetOriginRepository.deleteAll(obsolete);
        if (matching != null) return matching;

        return widgetOriginRepository.save(WidgetOriginEntity.builder()
            .widget(widget)
            .origin(normalizedOrigin)
            .verified(false)
            .createdBy(currentOrganizationProvider.requireCurrent().user())
            .build());
    }

    private WidgetOriginEntity findPrimaryOrigin(BigInteger widgetId) {
        return widgetOriginRepository.findAllByWidgetWidgetIdIn(List.of(widgetId)).stream()
            .min(Comparator.comparing(WidgetOriginEntity::getWidgetOriginId))
            .orElse(null);
    }

    private static WidgetOriginEntity lowestId(
        WidgetOriginEntity left,
        WidgetOriginEntity right
    ) {
        return left.getWidgetOriginId().compareTo(right.getWidgetOriginId()) <= 0 ? left : right;
    }
}
