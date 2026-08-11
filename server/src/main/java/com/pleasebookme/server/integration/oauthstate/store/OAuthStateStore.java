package com.pleasebookme.server.integration.oauthstate.store;

import com.pleasebookme.server.integration.oauthstate.model.OAuthState;

import java.time.Duration;
import java.util.Optional;

public interface OAuthStateStore {
    String issue(
        OAuthState state,
        Duration ttl
    );

    Optional<OAuthState> consume(String state);
}
