package com.pleasebookme.server.service.auth.dto;

public record LoginResponse(
    String accessToken,
    String refreshToken
) {}
