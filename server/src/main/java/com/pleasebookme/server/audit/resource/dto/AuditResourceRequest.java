package com.pleasebookme.server.audit.resource.dto;

import com.pleasebookme.server.audit.enums.ResourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.JsonNode;

import java.math.BigInteger;

public record AuditResourceRequest(
    @NotNull
    BigInteger eventId,

    @NotNull
    ResourceType resourceType,

    @NotBlank
    @Size(max = 255)
    String resourceUid,

    @NotBlank
    @Size(max = 255)
    String resourceName,

    JsonNode beforeSnapshot,

    @NotNull
    JsonNode afterSnapshot
) {
}
