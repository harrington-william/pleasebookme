package com.pleasebookme.server.integration.drive.dto;

import com.pleasebookme.server.integration.drive.entity.DestinationDriveEntity;
import com.pleasebookme.server.integration.enums.IntegrationType;

import java.math.BigInteger;
import java.time.Instant;

public record DestinationDriveResponse(
    BigInteger destinationDriveId,
    IntegrationType integrationType,
    String externalId,
    BigInteger userId,
    BigInteger serviceId,
    BigInteger oauthConnectionId,
    Instant createdAt,
    Instant updatedAt
) {
    public static DestinationDriveResponse from(DestinationDriveEntity destinationDrive) {
        return new DestinationDriveResponse(
            destinationDrive.getDestinationDriveId(),
            destinationDrive.getIntegrationType(),
            destinationDrive.getExternalId(),
            destinationDrive.getUser().getUserId(),
            destinationDrive.getService().getServiceId(),
            destinationDrive.getOauthConnection().getOauthConnectionId(),
            destinationDrive.getCreatedAt(),
            destinationDrive.getUpdatedAt()
        );
    }
}
