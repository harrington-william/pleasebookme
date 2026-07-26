package com.pleasebookme.server.security.identity.principal;

import com.pleasebookme.server.auth.enums.AccountStatus;
import com.pleasebookme.server.global.enums.Locale;
import com.pleasebookme.server.security.identity.enums.AuthenticatedActorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record AuthenticatedPrincipal(

    @NotNull
    AuthenticatedActorType actorType,

    UUID userUid,
    UUID tenantUid,
    BigInteger organizationId,
    BigInteger membershipId,
    BigInteger profileId,

    @NotBlank(message = "Username is required")
    String username,
    String email,

    String displayName,
    Locale locale,
    @NotBlank(message = "Timezone is required")
    String timezone,

    AccountStatus accountStatus,
    Set<String> roles,
    Set<String> permissions,
    Map<String, Object> attributes

) implements Serializable {
    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    public boolean isSystem() {
        return actorType == AuthenticatedActorType.SYSTEM;
    }

    public boolean isActive() {
        return accountStatus == AccountStatus.ACTIVE;
    }
}
