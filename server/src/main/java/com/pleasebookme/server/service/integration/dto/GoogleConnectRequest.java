package com.pleasebookme.server.service.integration.dto;

import com.pleasebookme.server.security.oauth.google.authorization.GoogleScope;

import java.util.List;

public record GoogleConnectRequest(
    List<GoogleScope> scopes,

    // Redirect in the service, anything suspicious falls back to the default
    String redirectAfter
) {}
