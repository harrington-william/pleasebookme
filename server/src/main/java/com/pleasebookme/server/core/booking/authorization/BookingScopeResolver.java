package com.pleasebookme.server.core.booking.authorization;

import com.pleasebookme.server.core.booking.entity.BookingEntity;
import com.pleasebookme.server.security.authorization.scope.ResourceScope;
import com.pleasebookme.server.security.authorization.scope.ScopeResolver;
import com.pleasebookme.server.tenant.tenants.entity.TenantEntity;
import com.pleasebookme.server.tenant.tenants.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

@Component
@RequiredArgsConstructor
public class BookingScopeResolver implements ScopeResolver<BookingEntity> {

    private final TenantRepository tenantRepository;

    @Override
    public Class<BookingEntity> resourceType() {
        return BookingEntity.class;
    }

    @Override
    public ResourceScope resolve(BookingEntity booking) {
        BigInteger organizationId = booking.getService().getOrganization().getOrganizationId();

        BigInteger tenantId = tenantRepository
            .findByOrganizationOrganizationId(organizationId)
            .map(TenantEntity::getTenantId)
            .orElse(null);

        return new ResourceScope(organizationId, tenantId, null);
    }
}
