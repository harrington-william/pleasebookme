package com.pleasebookme.server.security.oauth.google.pkce;

public record PkceChallenge(
    String verifier,
    String challenge
) {
    public static final String METHOD = "S256";
}
