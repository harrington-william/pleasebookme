package com.pleasebookme.server.service.integration.service;

import com.pleasebookme.server.security.identity.principal.UserPrincipal;
import com.pleasebookme.server.service.integration.dto.GoogleConnectRequest;
import com.pleasebookme.server.service.integration.dto.GoogleConnectResponse;
import com.pleasebookme.server.service.integration.dto.OAuthConnectionSummaryResponse;

import java.net.URI;
import java.util.List;
import java.util.UUID;

public interface GoogleConnectService {
    GoogleConnectResponse initiate(
        UserPrincipal principal,
        GoogleConnectRequest request
    );

    // Returns the dashboard URI to redirect the browser to, for both success
    // and every failure mode. A browser stranded on a JSON error body is a
    // dead end for the user, so this never throws to the controller.
    URI complete(
        String code,
        String state,
        String error
    );

    List<OAuthConnectionSummaryResponse> list(UserPrincipal principal);

    void disconnect(
        UserPrincipal principal,
        UUID oauthConnectionUid
    );
}
