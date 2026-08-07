package com.pleasebookme.server.integration.oauthstate.model;

import java.util.List;
import java.util.UUID;

// Everything the callback needs to finish a consent it did not start. The
// callback is an unauthenticated top-level browser navigation, so the user's
// identity has to travel here rather than in an Authorization header.
public record OAuthState(
    UUID userUid,
    String codeVerifier,
    List<String> requestedScopes,
    String redirectAfter
) {}
