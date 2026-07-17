package com.pleasebookme.server.widget.widgetorigin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigInteger;

public record WidgetOriginRequest(
    @NotNull
    BigInteger widgetId,

    @NotBlank
    @Size(max = 255)
    String origin,

    Boolean verified,

    BigInteger createdById
) {
}
