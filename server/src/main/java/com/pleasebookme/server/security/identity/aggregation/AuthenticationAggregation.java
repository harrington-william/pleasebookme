package com.pleasebookme.server.security.identity.aggregation;

import com.pleasebookme.server.auth.permission.entity.PermissionEntity;
import com.pleasebookme.server.auth.role.entity.RoleEntity;
import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.organization.membership.entity.MembershipEntity;
import com.pleasebookme.server.organization.organizations.entity.OrganizationEntity;
import com.pleasebookme.server.organization.profile.entity.ProfileEntity;
import com.pleasebookme.server.tenant.tenants.entity.TenantEntity;

import java.util.Set;

public record AuthenticationAggregation(
    UserEntity user,

    MembershipEntity membership,

    OrganizationEntity organization,

    // Null until the user's organization has an active plan
    TenantEntity tenant,

    ProfileEntity profile,

    Set<RoleEntity> roles,

    Set<PermissionEntity> permissions
) {}
