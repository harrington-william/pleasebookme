package com.pleasebookme.server.security.identity.principal;

import com.pleasebookme.server.security.identity.enums.AuthenticatedActorType;

import java.util.UUID;

public sealed interface AuthenticatedPrincipal
    permits UserPrincipal, WidgetPrincipal {

    AuthenticatedActorType actorType();

    UUID subject();

    UUID tenantUid();
}
