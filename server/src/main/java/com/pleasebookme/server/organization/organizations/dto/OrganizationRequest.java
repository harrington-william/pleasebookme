package com.pleasebookme.server.organization.organizations.dto;

import com.pleasebookme.server.global.enums.WeekStart;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrganizationRequest(
    @NotBlank
    @Size(max = 255)
    String name,

    @NotBlank
    @Size(max = 255)
    String slug,

    String logoUrl,

    String bannerUrl,

    String bio,

    Boolean isPrivate,

    @Size(max = 100)
    String timezone,

    WeekStart weekStart
) {
}
