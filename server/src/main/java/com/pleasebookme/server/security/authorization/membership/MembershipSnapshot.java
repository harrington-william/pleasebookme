package com.pleasebookme.server.security.authorization.membership;

import java.math.BigInteger;
import java.util.Set;

public record MembershipSnapshot(
    BigInteger membershipId,
    BigInteger organizationId,
    Set<String> roles,
    Set<String> permissions
) {
    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }
}
