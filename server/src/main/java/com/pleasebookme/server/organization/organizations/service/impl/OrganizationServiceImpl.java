package com.pleasebookme.server.organization.organizations.service.impl;

import com.pleasebookme.server.organization.organizations.dto.OrganizationRequest;
import com.pleasebookme.server.organization.organizations.entity.OrganizationEntity;
import com.pleasebookme.server.organization.organizations.exception.DuplicateOrganizationException;
import com.pleasebookme.server.organization.organizations.exception.OrganizationNotFoundException;
import com.pleasebookme.server.organization.organizations.repository.OrganizationRepository;
import com.pleasebookme.server.organization.organizations.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {
    private final OrganizationRepository organizationRepository;

    @Override
    public OrganizationEntity createOrganization(OrganizationRequest request) {
        if (organizationRepository.existsBySlug(request.slug())) {
            throw new DuplicateOrganizationException("Slug already exists: " + request.slug());
        }

        OrganizationEntity.OrganizationEntityBuilder organization = OrganizationEntity.builder()
            .name(request.name())
            .slug(request.slug())
            .logoUrl(request.logoUrl())
            .bannerUrl(request.bannerUrl())
            .bio(request.bio());

        if (request.isPrivate() != null) organization.isPrivate(request.isPrivate());
        if (request.timezone() != null) organization.timezone(request.timezone());
        if (request.weekStart() != null) organization.weekStart(request.weekStart());

        return organizationRepository.save(organization.build());
    }

    @Override
    public OrganizationEntity getOrganizationById(BigInteger organizationId) {
        return organizationRepository.findById(organizationId)
            .orElseThrow(() -> new OrganizationNotFoundException(
                "Organization not found: " + organizationId
            ));
    }

    @Override
    public List<OrganizationEntity> getAllOrganizations() {
        return organizationRepository.findAll();
    }

    @Override
    public OrganizationEntity updateOrganization(
        BigInteger organizationId,
        OrganizationRequest request
    ) {
        OrganizationEntity organization = getOrganizationById(organizationId);

        organization.setName(request.name());
        organization.setSlug(request.slug());
        organization.setLogoUrl(request.logoUrl());
        organization.setBannerUrl(request.bannerUrl());
        organization.setBio(request.bio());

        if (request.isPrivate() != null) organization.setIsPrivate(request.isPrivate());
        if (request.timezone() != null) organization.setTimezone(request.timezone());
        if (request.weekStart() != null) organization.setWeekStart(request.weekStart());

        return organizationRepository.save(organization);
    }

    @Override
    public void deleteOrganization(BigInteger organizationId) {
        organizationRepository.delete(getOrganizationById(organizationId));
    }
}
