package com.pleasebookme.server.security.oauth.google.authorization;

import java.util.List;

public enum GoogleScope {
    CALENDAR("https://www.googleapis.com/auth/calendar"),
    SHEETS("https://www.googleapis.com/auth/spreadsheets"),
    DRIVE_FILE("https://www.googleapis.com/auth/drive.file");

    private static final List<String> BASE_SCOPES = List.of("openid", "email", "profile");

    private final String uri;

    GoogleScope(String uri) {
        this.uri = uri;
    }

    public static List<String> baseScopes() {
        return BASE_SCOPES;
    }

    public String uri() {
        return uri;
    }
}
