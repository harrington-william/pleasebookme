package com.pleasebookme.server.security.oauth.google.client;

import com.pleasebookme.server.security.oauth.google.client.dto.GoogleTokenResponse;
import com.pleasebookme.server.security.oauth.google.exception.GoogleTokenExchangeException;
import com.pleasebookme.server.security.oauth.google.exception.GoogleTokenRefreshException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class DefaultGoogleTokenClient implements GoogleTokenClient {
    private static final String REVOKE_URI = "https://oauth2.googleapis.com/revoke";

    private final ClientRegistration registration;
    private final RestClient restClient;

    public DefaultGoogleTokenClient(ClientRegistration googleClientRegistration) {
        this.registration = googleClientRegistration;
        this.restClient = RestClient.builder()
            .defaultHeaders(headers -> headers.setBasicAuth(
                googleClientRegistration.getClientId(),
                googleClientRegistration.getClientSecret()
            ))
            .build();
    }

    @Override
    public GoogleTokenResponse exchangeAuthorizationCode(
        String code,
        String codeVerifier
    ) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("code_verifier", codeVerifier);
        form.add("redirect_uri", registration.getRedirectUri());

        try {
            GoogleTokenResponse response = post(form);

            if (response == null || response.accessToken() == null) {
                throw new GoogleTokenExchangeException(
                    "Google returned no access token for the authorization code"
                );
            }

            return response;
        } catch (GoogleTokenExchangeException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new GoogleTokenExchangeException(
                "Failed to exchange authorization code with Google: " + exception.getMessage(),
                exception
            );
        }
    }

    @Override
    public GoogleTokenResponse refreshAccessToken(String refreshToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("refresh_token", refreshToken);

        try {
            GoogleTokenResponse response = post(form);

            if (response == null || response.accessToken() == null) {
                throw new GoogleTokenRefreshException(
                    "Google returned no access token for the refresh token",
                    false
                );
            }

            return response;
        } catch (GoogleTokenRefreshException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            String message = String.valueOf(exception.getMessage());

            throw new GoogleTokenRefreshException(
                "Failed to refresh Google access token: " + message,
                message.contains("invalid_grant")
            );
        }
    }

    @Override
    public void revoke(String token) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("token", token);

        try {
            restClient
                .post()
                .uri(REVOKE_URI)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .toBodilessEntity();
        } catch (RuntimeException exception) {
            log.warn("Google token revocation did not succeed: {}", exception.getMessage());
        }
    }

    private GoogleTokenResponse post(MultiValueMap<String, String> form) {
        return restClient
            .post()
            .uri(registration.getProviderDetails().getTokenUri())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
            .retrieve()
            .body(GoogleTokenResponse.class);
    }
}
