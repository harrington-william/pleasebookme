package com.pleasebookme.server.integration.oauthstate.model;

import java.util.List;
import java.util.UUID;

public record OAuthState(
    OAuthFlowMode mode,

    // Null when mode is SIGN_UP_AND_CONNECT
    UUID userUid,

    String codeVerifier,
    List<String> requestedScopes,
    String redirectAfter
) {}
