package com.pleasebookme.server.service.widget;

import com.pleasebookme.server.tenant.enums.TenantStatus;
import java.util.Set;

public final class ServedTenantStatuses {
    public static final Set<TenantStatus> SERVED = Set.of(TenantStatus.ACTIVE, TenantStatus.TRIAL);

    private ServedTenantStatuses() {}
}
