package com.pleasebookme.server.organization.organizations;

import java.util.Locale;
import java.util.Set;

public final class ReservedOrganizationSlugs {
    // Brand and system names an organization must not take. These were originally
    // reserved because the hosted booking page sat at the client's root (/{slug});
    // it now lives under /booking/{slug}, so nothing here protects routing any more
    // and this list is purely about not handing out names the platform owns.
    private static final Set<String> RESERVED = Set.of(
        "api", "dashboard", "login", "register", "google", "me",
        "widget", "widgets", "admin", "settings", "public", "book",
        "_next", "favicon.ico", "robots.txt", "sitemap.xml"
    );

    private ReservedOrganizationSlugs() {}

    public static boolean isReserved(String slug) {
        return slug != null && RESERVED.contains(slug.toLowerCase(Locale.ROOT));
    }
}
