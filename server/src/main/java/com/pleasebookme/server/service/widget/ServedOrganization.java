package com.pleasebookme.server.service.widget;

import com.pleasebookme.server.organization.organizations.entity.OrganizationEntity;

public record ServedOrganization(
    OrganizationEntity organization,
    String ecosystemCode
) {}
