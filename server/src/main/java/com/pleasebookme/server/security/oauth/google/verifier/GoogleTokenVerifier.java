package com.pleasebookme.server.security.oauth.google.verifier;

import com.pleasebookme.server.security.oauth.google.identity.GoogleIdentity;

public interface GoogleTokenVerifier {
    GoogleIdentity verify(String idToken);
}
