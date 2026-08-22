package com.pleasebookme.server.service.auth.controller;

import com.pleasebookme.server.security.identity.context.CurrentPrincipalProvider;
import com.pleasebookme.server.security.identity.principal.UserPrincipal;
import com.pleasebookme.server.service.auth.dto.*;
import com.pleasebookme.server.service.auth.service.AuthService;
import com.pleasebookme.server.service.auth.service.GoogleOnboardingService;
import com.pleasebookme.server.service.auth.service.GoogleSignInService;
import com.pleasebookme.server.service.integration.service.GoogleConnectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final GoogleSignInService googleSignInService;
    private final GoogleOnboardingService googleOnboardingService;
    private final GoogleConnectService googleConnectService;
    private final CurrentPrincipalProvider currentPrincipalProvider;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public LoginResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/refresh")
    public RefreshResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refreshToken(request.refreshToken());
    }

    @GetMapping("/me")
    public UserPrincipal getCurrentUser() {
        return currentPrincipalProvider.requireUser();
    }

    @PostMapping("/widget/bootstrap")
    public WidgetBootstrapResponse bootstrapWidget(@Valid @RequestBody WidgetBootstrapRequest request) {
        return authService.bootstrapWidget(request);
    }

    @PostMapping("/google")
    public LoginResponse signInWithGoogle(@Valid @RequestBody GoogleSignInRequest request) {
        return googleSignInService.signIn(request.idToken());
    }

    @PostMapping("/google/authorize")
    public GoogleAuthorizeResponse authorizeGoogleOnboarding(
        @RequestBody(required = false) GoogleAuthorizeRequest request
    ) {
        return new GoogleAuthorizeResponse(
            googleConnectService
                .initiateOnboarding(request == null ? null : request.redirectAfter())
                .authorizationUrl()
        );
    }

    @PostMapping("/google/handoff")
    public LoginResponse exchangeGoogleHandoff(
        @Valid @RequestBody GoogleHandoffRequest request
    ) {
        return googleOnboardingService.exchangeHandoff(request.code());
    }
}
