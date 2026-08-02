package com.pleasebookme.server.integration.sheets.dto;

import com.pleasebookme.server.integration.enums.IntegrationType;
import com.pleasebookme.server.integration.sheets.entity.DestinationSheetsEntity;

import java.math.BigInteger;
import java.time.Instant;

public record DestinationSheetsResponse(
    BigInteger destinationSheetsId,
    IntegrationType integrationType,
    String externalId,
    BigInteger userId,
    BigInteger serviceId,
    BigInteger oauthConnectionId,
    Instant createdAt,
    Instant updatedAt
) {
    public static DestinationSheetsResponse from(DestinationSheetsEntity destinationSheets) {
        return new DestinationSheetsResponse(
            destinationSheets.getDestinationSheetsId(),
            destinationSheets.getIntegrationType(),
            destinationSheets.getExternalId(),
            destinationSheets.getUser().getUserId(),
            destinationSheets.getService().getServiceId(),
            destinationSheets.getOauthConnection().getOauthConnectionId(),
            destinationSheets.getCreatedAt(),
            destinationSheets.getUpdatedAt()
        );
    }
}
