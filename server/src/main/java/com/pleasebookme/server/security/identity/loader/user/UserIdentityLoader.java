package com.pleasebookme.server.security.identity.loader.user;

import com.pleasebookme.server.security.identity.aggregation.AuthenticationAggregation;

import java.util.UUID;

public interface UserIdentityLoader {
    AuthenticationAggregation loadByUsername(String username);

    AuthenticationAggregation loadByEmail(String email);

    AuthenticationAggregation loadByUid(UUID uuid);
}
