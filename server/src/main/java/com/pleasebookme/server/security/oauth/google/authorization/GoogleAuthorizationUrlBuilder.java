package com.pleasebookme.server.security.oauth.google.authorization;

import com.pleasebookme.server.security.oauth.google.pkce.PkceChallenge;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GoogleAuthorizationUrlBuilder {
    private final ClientRegistration googleClientRegistration;

    public String build(
        String state,
        PkceChallenge pkce,
        List<String> scopes
    ) {
        return UriComponentsBuilder
            .fromUriString(googleClientRegistration.getProviderDetails().getAuthorizationUri())
            .queryParam("client_id", googleClientRegistration.getClientId())
            .queryParam("redirect_uri", googleClientRegistration.getRedirectUri())
            .queryParam("response_type", "code")
            .queryParam("scope", String.join(" ", scopes))
            .queryParam("state", state)
            .queryParam("code_challenge", pkce.challenge())
            .queryParam("code_challenge_method", PkceChallenge.METHOD)
            .queryParam("access_type", "offline")
            .queryParam("prompt", "consent")
            .queryParam("include_granted_scopes", "true")
            .encode()
            .build()
            .toUriString();
    }
}
