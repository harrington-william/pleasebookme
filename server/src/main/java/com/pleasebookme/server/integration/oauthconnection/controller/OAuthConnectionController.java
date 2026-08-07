package com.pleasebookme.server.integration.oauthconnection.controller;

import com.pleasebookme.server.integration.oauthconnection.dto.OAuthConnectionResponse;
import com.pleasebookme.server.integration.oauthconnection.service.OAuthConnectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;

// Deliberately not a full CRUD surface. An oauth_connection row holds live
// Google credentials, so it may only be written by the consent flow in
// GoogleIntegrationController - never from a client-supplied request body.
// POST/PUT would let a caller inject forged tokens, and a list-all endpoint
// would expose every user's connections, so all three were removed.
@RestController
@RequestMapping("/api/v1/oauth-connections")
@RequiredArgsConstructor
public class OAuthConnectionController {
    private final OAuthConnectionService oauthConnectionService;

    @GetMapping("/{oauthConnectionId}")
    public OAuthConnectionResponse getOAuthConnection(@PathVariable BigInteger oauthConnectionId) {
        return OAuthConnectionResponse.from(oauthConnectionService.getOAuthConnectionById(oauthConnectionId));
    }

    @DeleteMapping("/{oauthConnectionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOAuthConnection(@PathVariable BigInteger oauthConnectionId) {
        oauthConnectionService.deleteOAuthConnection(oauthConnectionId);
    }
}
