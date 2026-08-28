package com.pleasebookme.server.service.availabilityruleset.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AvailabilityRulesetRequest(
    @NotBlank
    @Size(max = 255)
    String title,

    @Size(max = 100)
    String timezone,

    @Valid
    @NotEmpty
    List<AvailabilityWindowRequest> windows
) {
}
