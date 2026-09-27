package com.pleasebookme.server.service.widget.fullpage;

import com.pleasebookme.server.organization.organizations.exception.OrganizationNotFoundException;
import com.pleasebookme.server.organization.organizations.repository.OrganizationRepository;
import com.pleasebookme.server.service.widget.ServedOrganization;
import com.pleasebookme.server.service.widget.ServedTenantStatuses;
import com.pleasebookme.server.tenant.tenants.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SlugOrganizationResolver {
    private final OrganizationRepository organizationRepository;
    private final TenantRepository tenantRepository;

    // Only used by full-page widgets
    public ServedOrganization resolve(String organizationSlug) {
        var organization = organizationRepository.findBySlug(organizationSlug)
            .orElseThrow(() -> organizationNotFound(organizationSlug));

        var tenant = tenantRepository.findByOrganizationOrganizationId(organization.getOrganizationId())
            .orElseThrow(() -> organizationNotFound(organizationSlug));

        // Missing and unserved tenants intentionally share the same response.
        if (!ServedTenantStatuses.SERVED.contains(tenant.getStatus())) {
            throw organizationNotFound(organizationSlug);
        }

        return new ServedOrganization(organization, tenant.getEcosystem().getCode());
    }

    private OrganizationNotFoundException organizationNotFound(String slug) {
        return new OrganizationNotFoundException("Organization not found: " + slug);
    }
}
