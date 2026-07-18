package com.pleasebookme.server.audit.change.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigInteger;

public record AuditChangeRequest(
    @NotNull
    BigInteger resourceId,

    @NotBlank
    @Size(max = 100)
    String fieldName,

    String oldValue,

    String newValue
) {
}
