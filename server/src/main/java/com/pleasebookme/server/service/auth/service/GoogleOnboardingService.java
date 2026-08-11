package com.pleasebookme.server.service.auth.service;

import com.pleasebookme.server.security.oauth.google.client.dto.GoogleTokenResponse;
import com.pleasebookme.server.security.oauth.google.identity.GoogleIdentity;
import com.pleasebookme.server.service.auth.dto.LoginResponse;

// The registration half of the one-shot Google flow: one consent screen yields
// both an account and delegated Calendar/Sheets/Drive access.
//
// Starting the flow is GoogleConnectService.initiateOnboarding - it is the same
// authorization request as a settings connect, only in a different mode, and
// keeping all authorization-URL construction in one place is worth more than
// symmetry here.
public interface GoogleOnboardingService {
    // Called from inside the OAuth callback. Returns a single-use handoff code
    // the browser carries to the frontend, which trades it for a real session.
    String finalizeOnboarding(
        GoogleTokenResponse tokens,
        GoogleIdentity identity
    );

    LoginResponse exchangeHandoff(String code);
}
