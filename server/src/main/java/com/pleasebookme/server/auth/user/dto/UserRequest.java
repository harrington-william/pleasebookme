package com.pleasebookme.server.auth.user.dto;

import com.pleasebookme.server.global.enums.Locale;
import com.pleasebookme.server.global.enums.Theme;
import com.pleasebookme.server.global.enums.WeekStart;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequest(
    @NotBlank
    @Size(max = 100)
    String username,

    @NotBlank
    @Size(max = 255)
    String name,

    @NotBlank
    @Size(max = 255)
    String email,

    @NotBlank
    @Size(max = 50)
    String phone,

    String bio,

    String avatarUrl,

    Locale locale,

    @Size(max = 100)
    String timezone,

    Theme theme,

    WeekStart weekStart
) {
}
