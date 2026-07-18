package com.pleasebookme.server.notification.template.dto;

import com.pleasebookme.server.global.enums.Locale;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigInteger;

public record NotificationTemplateRequest(
    BigInteger tenantId,

    @NotBlank
    @Size(max = 100)
    String code,

    @NotBlank
    @Size(max = 255)
    String name,

    @NotBlank
    @Size(max = 50)
    String channel,

    String subjectTemplate,

    @NotBlank
    String bodyTemplate,

    @NotNull
    Locale locale,

    Boolean enabled,

    Integer version
) {
}
