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

public record UserPrincipal(

    @NotNull
    AuthenticatedActorType actorType,

    UUID subject,

    @NotNull
    BigInteger userId,

    // Always null because a user can hold membership in more than 1 organization
    // Kept only to satisfy AuthenticatedPrincipal's sealed contract
    UUID tenantUid,

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

) implements AuthenticatedPrincipal, Serializable {
    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    public boolean isActive() {
        return accountStatus == AccountStatus.ACTIVE;
    }
}
