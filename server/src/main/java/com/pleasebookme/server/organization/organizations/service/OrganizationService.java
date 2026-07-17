package com.pleasebookme.server.organization.organizations.service;

import com.pleasebookme.server.organization.organizations.dto.OrganizationRequest;
import com.pleasebookme.server.organization.organizations.entity.OrganizationEntity;

import java.math.BigInteger;
import java.util.List;

public interface OrganizationService {
    OrganizationEntity createOrganization(OrganizationRequest request);

    OrganizationEntity getOrganizationById(BigInteger organizationId);

    List<OrganizationEntity> getAllOrganizations();

    OrganizationEntity updateOrganization(
        BigInteger organizationId,
        OrganizationRequest request
    );

    void deleteOrganization(BigInteger organizationId);
}
