package com.pleasebookme.server.security.oauth.google.client;

import com.pleasebookme.server.security.oauth.google.client.dto.GoogleTokenResponse;

public interface GoogleTokenClient {
    GoogleTokenResponse exchangeAuthorizationCode(
        String code,
        String codeVerifier
    );

    GoogleTokenResponse refreshAccessToken(String refreshToken);

    void revoke(String token);
}
