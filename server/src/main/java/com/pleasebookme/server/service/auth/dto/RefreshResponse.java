package com.pleasebookme.server.service.auth.dto;

public record RefreshResponse(
    String accessToken,
    String refreshToken
) {}
