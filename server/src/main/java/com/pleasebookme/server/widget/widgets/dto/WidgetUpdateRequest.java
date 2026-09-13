package com.pleasebookme.server.widget.widgets.dto;

import com.pleasebookme.server.widget.enums.WidgetStatus;
import com.pleasebookme.server.widget.enums.WidgetType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Update keeps credentials optional so ordinary edits do not rotate the key pair. */
public record WidgetUpdateRequest(
    @NotBlank
    @Size(max = 255)
    String name,

    WidgetType type,

    WidgetStatus status,

    @Size(max = 255)
    String origin,

    @Valid
    WidgetCredentialRequest credentials
) {
}
