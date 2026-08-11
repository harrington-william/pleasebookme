package com.pleasebookme.server.security.oauth.google.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GoogleTokenResponse(
    @JsonProperty("access_token")
    String accessToken,

    @JsonProperty("refresh_token")
    String refreshToken,

    @JsonProperty("id_token")
    String idToken,

    @JsonProperty("token_type")
    String tokenType,

    @JsonProperty("expires_in")
    Long expiresIn,

    @JsonProperty("scope")
    String scope
) {
    // Build scopes as array
    public List<String> grantedScopes() {
        if (scope == null || scope.isBlank()) {
            return List.of();
        }

        return List.of(scope.trim().split("\\s+"));
    }
}
