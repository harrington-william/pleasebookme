package com.pleasebookme.server.security.identity.loader.widget;

import com.pleasebookme.server.security.identity.principal.WidgetPrincipal;

import java.util.UUID;

public interface WidgetIdentityLoader {
    WidgetPrincipal loadByPublicKey(String publicKey, String secretKey, String origin);

    WidgetPrincipal loadByUid(UUID uid);
}
