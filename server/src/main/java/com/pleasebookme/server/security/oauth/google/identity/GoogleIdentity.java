package com.pleasebookme.server.security.oauth.google.identity;

public record GoogleIdentity(
    String sub,
    String email,
    boolean emailVerified,
    String name,
    String pictureUrl
) {}
