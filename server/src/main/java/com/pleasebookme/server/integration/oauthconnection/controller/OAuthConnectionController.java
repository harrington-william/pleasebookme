package com.pleasebookme.server.integration.oauthconnection.controller;

import com.pleasebookme.server.integration.oauthconnection.dto.OAuthConnectionRequest;
import com.pleasebookme.server.integration.oauthconnection.dto.OAuthConnectionResponse;
import com.pleasebookme.server.integration.oauthconnection.service.OAuthConnectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/oauth-connections")
@RequiredArgsConstructor
public class OAuthConnectionController {
    private final OAuthConnectionService oauthConnectionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OAuthConnectionResponse createOAuthConnection(@Valid @RequestBody OAuthConnectionRequest request) {
        return OAuthConnectionResponse.from(oauthConnectionService.createOAuthConnection(request));
    }

    @GetMapping("/{oauthConnectionId}")
    public OAuthConnectionResponse getOAuthConnection(@PathVariable BigInteger oauthConnectionId) {
        return OAuthConnectionResponse.from(oauthConnectionService.getOAuthConnectionById(oauthConnectionId));
    }

    @GetMapping
    public List<OAuthConnectionResponse> getOAuthConnections() {
        return oauthConnectionService.getAllOAuthConnections().stream()
            .map(OAuthConnectionResponse::from)
            .toList();
    }

    @PutMapping("/{oauthConnectionId}")
    public OAuthConnectionResponse updateOAuthConnection(
        @PathVariable BigInteger oauthConnectionId,
        @Valid @RequestBody OAuthConnectionRequest request
    ) {
        return OAuthConnectionResponse.from(oauthConnectionService.updateOAuthConnection(oauthConnectionId, request));
    }

    @DeleteMapping("/{oauthConnectionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOAuthConnection(@PathVariable BigInteger oauthConnectionId) {
        oauthConnectionService.deleteOAuthConnection(oauthConnectionId);
    }
}
