package com.pleasebookme.server.organization.organizations.dto;

import com.pleasebookme.server.global.enums.WeekStart;
import com.pleasebookme.server.organization.organizations.entity.OrganizationEntity;

import java.math.BigInteger;
import java.time.Instant;

public record OrganizationResponse(
    BigInteger organizationId,
    String name,
    String slug,
    String logoUrl,
    String bannerUrl,
    String bio,
    Boolean isPrivate,
    String timezone,
    WeekStart weekStart,
    Instant createdAt,
    Instant updatedAt
) {
    public static OrganizationResponse from(OrganizationEntity organization) {
        return new OrganizationResponse(
            organization.getOrganizationId(),
            organization.getName(),
            organization.getSlug(),
            organization.getLogoUrl(),
            organization.getBannerUrl(),
            organization.getBio(),
            organization.getIsPrivate(),
            organization.getTimezone(),
            organization.getWeekStart(),
            organization.getCreatedAt(),
            organization.getUpdatedAt()
        );
    }
}
