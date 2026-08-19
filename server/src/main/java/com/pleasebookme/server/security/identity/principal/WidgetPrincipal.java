package com.pleasebookme.server.security.identity.principal;

import com.pleasebookme.server.security.identity.enums.AuthenticatedActorType;
import com.pleasebookme.server.widget.enums.WidgetStatus;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.UUID;

public record WidgetPrincipal(

    @NotNull
    AuthenticatedActorType actorType,

    UUID widgetUid,
    UUID tenantUid,
    BigInteger tenantId,

    WidgetStatus status

) implements AuthenticatedPrincipal, Serializable {
    @Override
    public UUID subject() {
        return widgetUid;
    }

    public boolean isActive() {
        return status == WidgetStatus.ACTIVE;
    }
}
