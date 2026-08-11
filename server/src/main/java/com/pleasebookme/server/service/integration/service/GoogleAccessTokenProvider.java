package com.pleasebookme.server.service.integration.service;

import java.math.BigInteger;

// The only component permitted to hand out a decrypted Google access token.
// Everything downstream (calendar sync, sheets export) goes through here so
// expiry, refresh and revocation are handled in exactly one place.
public interface GoogleAccessTokenProvider {
    String accessTokenFor(BigInteger oauthConnectionId);
}
