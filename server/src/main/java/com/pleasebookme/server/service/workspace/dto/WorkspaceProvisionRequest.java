package com.pleasebookme.server.service.workspace.dto;

import com.pleasebookme.server.global.enums.Locale;

public record WorkspaceProvisionRequest(
    String username,
    String name,
    String email,
    String phone,
    Locale locale,
    String timezone
) {}
