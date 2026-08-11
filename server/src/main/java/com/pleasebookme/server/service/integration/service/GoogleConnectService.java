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

    // The one-shot registration entry point: same authorization request as
    // initiate, but with no principal to bind it to and always asking for every
    // scope, since this is the user's only consent screen.
    //
    // Lives here rather than in service/auth so that scope assembly, PKCE, state
    // TTL and redirect sanitisation stay in a single file. Note the dependency
    // must not be inverted - DefaultGoogleConnectService already calls into
    // GoogleOnboardingService from the callback, and a delegation back the other
    // way would be a constructor-injection cycle.
    GoogleConnectResponse initiateOnboarding(String redirectAfter);

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
