package com.pleasebookme.server.service.auth.dto;

public record GoogleAuthorizeRequest(
    String redirectAfter
) {}
