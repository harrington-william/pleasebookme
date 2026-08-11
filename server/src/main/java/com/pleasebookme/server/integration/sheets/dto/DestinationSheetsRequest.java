package com.pleasebookme.server.integration.sheets.dto;

import com.pleasebookme.server.integration.enums.IntegrationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigInteger;

public record DestinationSheetsRequest(
    @NotNull
    IntegrationType integrationType,

    @NotBlank
    @Size(max = 255)
    String externalId,

    @NotNull
    BigInteger userId,

    @NotNull
    BigInteger serviceId,

    @NotNull
    BigInteger oauthConnectionId
) {
}
