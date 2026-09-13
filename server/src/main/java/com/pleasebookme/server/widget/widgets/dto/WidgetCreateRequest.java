package com.pleasebookme.server.widget.widgets.dto;

import com.pleasebookme.server.widget.enums.WidgetType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Create keeps credentials mandatory because a widget cannot be persisted without them. */
public record WidgetCreateRequest(
    @NotBlank
    @Size(max = 255)
    String name,

    WidgetType type,

    @Size(max = 255)
    String origin,

    @NotNull
    @Valid
    WidgetCredentialRequest credentials
) {
}
