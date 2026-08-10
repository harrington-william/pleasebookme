package com.pleasebookme.server.service.integration.service.impl;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.user.exception.UserNotFoundException;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import com.pleasebookme.server.integration.enums.OAuthConnectionStatus;
import com.pleasebookme.server.integration.oauthconnection.entity.OAuthConnectionEntity;
import com.pleasebookme.server.integration.oauthconnection.repository.OAuthConnectionRepository;
import com.pleasebookme.server.integration.oauthstate.model.OAuthFlowMode;
import com.pleasebookme.server.integration.oauthstate.model.OAuthState;
import com.pleasebookme.server.integration.oauthstate.store.OAuthStateStore;
import com.pleasebookme.server.security.crypto.TokenCipher;
import com.pleasebookme.server.security.identity.principal.UserPrincipal;
import com.pleasebookme.server.security.oauth.google.authorization.GoogleAuthorizationUrlBuilder;
import com.pleasebookme.server.security.oauth.google.authorization.GoogleScope;
import com.pleasebookme.server.security.oauth.google.pkce.PkceChallenge;
import com.pleasebookme.server.security.oauth.google.pkce.PkceGenerator;
import com.pleasebookme.server.security.oauth.google.client.GoogleTokenClient;
import com.pleasebookme.server.security.oauth.google.client.dto.GoogleTokenResponse;
import com.pleasebookme.server.security.oauth.google.identity.GoogleIdentity;
import com.pleasebookme.server.security.oauth.google.verifier.GoogleTokenVerifier;
import com.pleasebookme.server.service.auth.service.GoogleOnboardingService;
import com.pleasebookme.server.service.integration.dto.GoogleConnectRequest;
import com.pleasebookme.server.service.integration.dto.GoogleConnectResponse;
import com.pleasebookme.server.service.integration.dto.OAuthConnectionSummaryResponse;
import com.pleasebookme.server.service.integration.exception.OAuthConnectionAccessDeniedException;
import com.pleasebookme.server.service.integration.service.GoogleConnectService;
import com.pleasebookme.server.service.integration.service.GoogleConnectionWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultGoogleConnectService implements GoogleConnectService {
    private static final Duration STATE_TTL = Duration.ofMinutes(10);
    private static final String DEFAULT_REDIRECT_AFTER = "/dashboard/settings/integrations";

    private static final String DEFAULT_ONBOARDING_REDIRECT_AFTER = "/google/complete";

    private final OAuthStateStore oauthStateStore;
    private final PkceGenerator pkceGenerator;
    private final GoogleAuthorizationUrlBuilder authorizationUrlBuilder;
    private final GoogleTokenClient googleTokenClient;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final TokenCipher tokenCipher;

    private final UserRepository userRepository;
    private final OAuthConnectionRepository oauthConnectionRepository;

    private final GoogleConnectionWriter googleConnectionWriter;
    private final GoogleOnboardingService googleOnboardingService;

    @Value("${app.client.frontend-url}")
    private String frontendUrl;

    @Override
    public GoogleConnectResponse initiate(
        UserPrincipal principal,
        GoogleConnectRequest request
    ) {
        return new GoogleConnectResponse(authorize(
            OAuthFlowMode.CONNECT,
            principal.subject(),
            request == null ? null : request.scopes(),
            safeRedirectAfter(
                request == null ? null : request.redirectAfter(),
                DEFAULT_REDIRECT_AFTER
            )
        ));
    }

    @Override
    public GoogleConnectResponse initiateOnboarding(String redirectAfter) {
        return new GoogleConnectResponse(authorize(
            // Nobody is logged in yet
            OAuthFlowMode.SIGN_UP_AND_CONNECT,

            null,
            null,
            safeRedirectAfter(redirectAfter, DEFAULT_ONBOARDING_REDIRECT_AFTER)
        ));
    }

    @Override
    public URI complete(
        String code,    // Authorization code
        String state,
        String error
    ) {
        Optional<OAuthState> resolved = oauthStateStore.consume(state);

        if (error != null && !error.isBlank()) {
            log.info("Google consent was not granted: {}", error);
            return redirect(
                resolved.map(OAuthState::redirectAfter).orElse(DEFAULT_REDIRECT_AFTER),
                "denied"
            );
        }

        // This could be expired, already-used, or revoked -> Indistinguishable
        if (resolved.isEmpty()) {
            log.warn("Rejected Google callback with an unknown or already-used state");
            return redirect(DEFAULT_REDIRECT_AFTER, "invalid_state");
        }

        OAuthState oauthState = resolved.get();

        if (code == null || code.isBlank()) {
            return redirect(oauthState.redirectAfter(), "missing_code");
        }

        try {
            // No @Transactional because this is a network round trip to Google
            // Should not hold a database connection open
            GoogleTokenResponse tokens = googleTokenClient.exchangeAuthorizationCode(
                code,
                oauthState.codeVerifier()
            );

            GoogleIdentity identity = googleTokenVerifier.verify(tokens.idToken());

            if (oauthState.mode() == OAuthFlowMode.SIGN_UP_AND_CONNECT) {
                String handoff = googleOnboardingService.finalizeOnboarding(tokens, identity);
                return redirect(oauthState.redirectAfter(), "connected", handoff);
            }

            UserEntity user = userRepository
                .findByUserUid(oauthState.userUid())
                .orElseThrow(() -> new UserNotFoundException(
                    "User not found: " + oauthState.userUid()
                ));

            googleConnectionWriter.persist(user, tokens, identity);

            return redirect(oauthState.redirectAfter(), "connected");
        } catch (RuntimeException exception) {
            log.error("Failed to complete Google OAuth connection", exception);
            return redirect(oauthState.redirectAfter(), "error");
        }
    }

    @Override
    public List<OAuthConnectionSummaryResponse> list(UserPrincipal principal) {
        UserEntity user = extractUser(principal);

        return oauthConnectionRepository
            .findByUserUserIdOrderByConnectedAtDesc(user.getUserId())
            .stream()
            .map(OAuthConnectionSummaryResponse::from)
            .toList();
    }

    @Override
    public void disconnect(
        UserPrincipal principal,
        UUID oauthConnectionUid
    ) {
        UserEntity user = extractUser(principal);

        OAuthConnectionEntity connection = oauthConnectionRepository
            .findByOauthConnectionUid(oauthConnectionUid)
            .orElseThrow(() -> new OAuthConnectionAccessDeniedException(
                "OAuth connection not found: " + oauthConnectionUid
            ));

        if (!connection.getUser().getUserId().equals(user.getUserId())) {
            throw new OAuthConnectionAccessDeniedException(
                "OAuth connection not found: " + oauthConnectionUid
            );
        }

        // Revoke at Google server
        googleTokenClient.revoke(
            tokenCipher.decrypt(connection.getRefreshToken(), connection.getTokenKeyVersion())
        );

        connection.setStatus(OAuthConnectionStatus.REVOKED);
        connection.setRevokedAt(Instant.now());
        oauthConnectionRepository.save(connection);
    }

    private String authorize(
        OAuthFlowMode mode,
        UUID userUid,
        List<GoogleScope> requestedScopes,
        String redirectAfter
    ) {
        List<GoogleScope> requested =
            requestedScopes == null || requestedScopes.isEmpty()
                ? Arrays.asList(GoogleScope.values())
                : requestedScopes;

        List<String> scopeUris = new ArrayList<>(GoogleScope.baseScopes());
        requested.stream().map(GoogleScope::uri).forEach(scopeUris::add);

        PkceChallenge pkce = pkceGenerator.generate();

        String state = oauthStateStore.issue(
            new OAuthState(
                mode,
                userUid,
                pkce.verifier(),
                scopeUris,
                redirectAfter
            ),
            STATE_TTL
        );

        return authorizationUrlBuilder.build(state, pkce, scopeUris);
    }

    private UserEntity extractUser(UserPrincipal principal) {
        return userRepository
            .findByUserUid(principal.subject())
            .orElseThrow(() -> new UserNotFoundException(
                "User not found: " + principal.subject()
            ));
    }

    private String safeRedirectAfter(
        String redirectAfter,
        String fallback
    ) {
        if (
            redirectAfter == null ||
            redirectAfter.isBlank() ||
            !redirectAfter.startsWith("/") ||
            redirectAfter.startsWith("//")
        ) {
            return fallback;
        }

        return redirectAfter;
    }

    private URI redirect(
        String path,
        String outcome
    ) {
        return redirect(path, outcome, null);
    }

    private URI redirect(
        String path,
        String outcome,
        String handoff
    ) {
        UriComponentsBuilder builder = UriComponentsBuilder
            .fromUriString(frontendUrl)
            .path(safeRedirectAfter(path, DEFAULT_REDIRECT_AFTER))
            .queryParam("google", outcome);

        if (handoff != null && !handoff.isBlank()) {
            builder.queryParam("handoff", handoff);
        }

        return builder.build().toUri();
    }
}
