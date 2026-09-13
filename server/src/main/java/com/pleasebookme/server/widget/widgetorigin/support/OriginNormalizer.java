package com.pleasebookme.server.widget.widgetorigin.support;

import com.pleasebookme.server.widget.widgetorigin.exception.InvalidWidgetOriginException;

import java.net.URI;
import java.util.Locale;

public final class OriginNormalizer {
    private OriginNormalizer() {}

    public static String normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String candidate = raw.trim();
        if (!candidate.contains("://")) {
            candidate = "https://" + candidate;
        }

        try {
            URI uri = URI.create(candidate);
            String scheme = uri.getScheme() != null
                ? uri.getScheme().toLowerCase(Locale.ROOT)
                : null;
            String host = uri.getHost() != null
                ? uri.getHost().toLowerCase(Locale.ROOT)
                : null;

            if (!("http".equals(scheme) || "https".equals(scheme)) || host == null || host.isBlank()) {
                throw invalid(raw);
            }

            if (uri.getUserInfo() != null) {
                throw invalid(raw);
            }

            int port = uri.getPort();
            boolean defaultPort = ("http".equals(scheme) && port == 80)
                || ("https".equals(scheme) && port == 443);

            // Browser Origin headers use only a lowercase scheme/host and a non-default port.
            return scheme + "://" + host + (port >= 0 && !defaultPort ? ":" + port : "");
        } catch (IllegalArgumentException exception) {
            throw invalid(raw);
        }
    }

    private static InvalidWidgetOriginException invalid(String raw) {
        return new InvalidWidgetOriginException("Invalid widget origin: " + raw);
    }
}
