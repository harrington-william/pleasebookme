package com.pleasebookme.server.service.widget.embedded;

import com.pleasebookme.server.organization.organizations.exception.OrganizationNotFoundException;
import com.pleasebookme.server.security.identity.context.CurrentPrincipalProvider;
import com.pleasebookme.server.service.widget.ServedOrganization;
import com.pleasebookme.server.service.widget.ServedTenantStatuses;
import com.pleasebookme.server.tenant.tenants.repository.TenantRepository;
import com.pleasebookme.server.widget.widgets.exception.WidgetNotActiveException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmbeddedWidgetOrganizationResolver {
    private final CurrentPrincipalProvider currentPrincipalProvider;
    private final TenantRepository tenantRepository;

    public ServedOrganization resolve() {
        var principal = currentPrincipalProvider.requireWidget();

        if (!principal.isActive()) {
            throw new WidgetNotActiveException("Widget is not active");
        }

        var tenant = tenantRepository.findById(principal.tenantId())
            .orElseThrow(() -> new OrganizationNotFoundException("Organization not found for widget"));

        if (!ServedTenantStatuses.SERVED.contains(tenant.getStatus())) {
            throw new OrganizationNotFoundException("Organization not found for widget");
        }

        return new ServedOrganization(tenant.getOrganization(), tenant.getEcosystem().getCode());
    }
}
