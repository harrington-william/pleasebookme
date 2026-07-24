package com.pleasebookme.server.security.identity.loader;

import com.pleasebookme.server.security.identity.aggregation.AuthenticationAggregation;

import java.util.UUID;

public interface IdentityLoader {
    AuthenticationAggregation loadByUsername(String username);

    AuthenticationAggregation loadByEmail(String email);

    AuthenticationAggregation loadByUid(UUID uuid);
}
