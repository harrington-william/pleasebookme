package com.pleasebookme.server.notification.channel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NotificationChannelRequest(
    @NotBlank
    @Size(max = 50)
    String code,

    @NotBlank
    @Size(max = 100)
    String name,

    Boolean enabled
) {
}
