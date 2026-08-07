package com.pleasebookme.server.service.integration.controller;

import com.pleasebookme.server.security.identity.context.CurrentPrincipalProvider;
import com.pleasebookme.server.service.integration.dto.GoogleConnectRequest;
import com.pleasebookme.server.service.integration.dto.GoogleConnectResponse;
import com.pleasebookme.server.service.integration.dto.OAuthConnectionSummaryResponse;
import com.pleasebookme.server.service.integration.service.GoogleConnectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/integrations/google")
@RequiredArgsConstructor
public class GoogleIntegrationController {
    private final GoogleConnectService googleConnectService;
    private final CurrentPrincipalProvider currentPrincipalProvider;

    @PostMapping("/connect")
    public GoogleConnectResponse connect(
        @Valid @RequestBody(required = false) GoogleConnectRequest request
    ) {
        return googleConnectService.initiate(
            currentPrincipalProvider.requireUser(),
            request
        );
    }

    // Unauthenticated by design: this is a top-level browser navigation from
    // Google carries no Authorization header. Identity comes from state.
    // Redirect user to dashboard instead of return a DTO
    @GetMapping("/callback")
    public ResponseEntity<Void> callback(
        @RequestParam(required = false) String code,
        @RequestParam(required = false) String state,
        @RequestParam(required = false) String error
    ) {
        return ResponseEntity
            .status(HttpStatus.FOUND)
            .location(googleConnectService.complete(code, state, error))
            .build();
    }

    @GetMapping("/connections")
    public List<OAuthConnectionSummaryResponse> connections() {
        return googleConnectService.list(currentPrincipalProvider.requireUser());
    }

    @DeleteMapping("/connections/{oauthConnectionUid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disconnect(@PathVariable UUID oauthConnectionUid) {
        googleConnectService.disconnect(
            currentPrincipalProvider.requireUser(),
            oauthConnectionUid
        );
    }
}
