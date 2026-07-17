package com.pleasebookme.server.organization.profile.dto;

import com.pleasebookme.server.organization.profile.entity.ProfileEntity;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

public record ProfileResponse(
    BigInteger profileId,
    UUID profileUid,
    BigInteger userId,
    BigInteger organizationId,
    String username,
    Instant createdAt,
    Instant updatedAt
) {
    public static ProfileResponse from(ProfileEntity profile) {
        return new ProfileResponse(
            profile.getProfileId(),
            profile.getProfileUid(),
            profile.getUser().getUserId(),
            profile.getOrganization().getOrganizationId(),
            profile.getUsername(),
            profile.getCreatedAt(),
            profile.getUpdatedAt()
        );
    }
}
