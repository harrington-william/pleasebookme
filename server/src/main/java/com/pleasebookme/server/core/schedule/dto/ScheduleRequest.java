package com.pleasebookme.server.core.schedule.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ScheduleRequest(
    @NotBlank
    @Size(max = 255)
    String title,

    @Size(max = 100)
    String timezone
) {
}
