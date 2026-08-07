package com.pleasebookme.server.service.integration.service.impl;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.user.exception.UserNotFoundException;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import com.pleasebookme.server.integration.enums.OAuthConnectionStatus;
import com.pleasebookme.server.integration.enums.OAuthProvider;
import com.pleasebookme.server.integration.oauthconnection.entity.OAuthConnectionEntity;
import com.pleasebookme.server.integration.oauthconnection.repository.OAuthConnectionRepository;
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
import com.pleasebookme.server.service.integration.dto.GoogleConnectRequest;
import com.pleasebookme.server.service.integration.dto.GoogleConnectResponse;
import com.pleasebookme.server.service.integration.dto.OAuthConnectionSummaryResponse;
import com.pleasebookme.server.service.integration.exception.OAuthConnectionAccessDeniedException;
import com.pleasebookme.server.service.integration.service.GoogleConnectService;
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
    private static final String DEFAULT_REDIRECT_AFTER = "/settings/integrations";

    private final OAuthStateStore oauthStateStore;
    private final PkceGenerator pkceGenerator;
    private final GoogleAuthorizationUrlBuilder authorizationUrlBuilder;
    private final GoogleTokenClient googleTokenClient;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final TokenCipher tokenCipher;

    private final UserRepository userRepository;
    private final OAuthConnectionRepository oauthConnectionRepository;

    @Value("${app.client.frontend-url}")
    private String frontendUrl;

    @Override
    public GoogleConnectResponse initiate(
        UserPrincipal principal,
        GoogleConnectRequest request
    ) {
        List<GoogleScope> requested =
            request == null || request.scopes() == null || request.scopes().isEmpty()
                ? Arrays.asList(GoogleScope.values())
                : request.scopes();

        List<String> scopeUris = new ArrayList<>(GoogleScope.baseScopes());
        requested.stream().map(GoogleScope::uri).forEach(scopeUris::add);

        PkceChallenge pkce = pkceGenerator.generate();

        String state = oauthStateStore.issue(
            new OAuthState(
                principal.subject(),
                pkce.verifier(),
                scopeUris,
                safeRedirectAfter(request == null ? null : request.redirectAfter())
            ),
            STATE_TTL
        );

        return new GoogleConnectResponse(
            authorizationUrlBuilder.build(state, pkce, scopeUris)
        );
    }

    @Override
    public URI complete(
        String code,    // Authorization code
        String state,
        String error
    ) {
        if (error != null && !error.isBlank()) {
            log.info("Google consent was not granted: {}", error);
            return redirect(DEFAULT_REDIRECT_AFTER, "denied");
        }

        Optional<OAuthState> resolved = oauthStateStore.consume(state);

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

            persist(oauthState, tokens, identity);

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

    private void persist(
        OAuthState oauthState,
        GoogleTokenResponse tokens,
        GoogleIdentity identity
    ) {
        UserEntity user = userRepository
            .findByUserUid(oauthState.userUid())
            .orElseThrow(() -> new UserNotFoundException(
                "User not found: " + oauthState.userUid()
            ));

        OAuthConnectionEntity connection = oauthConnectionRepository
            .findByUserUserIdAndProviderAndProviderAccountId(
                user.getUserId(),
                OAuthProvider.GOOGLE,
                identity.sub()
            )
            .orElseGet(() -> OAuthConnectionEntity.builder()
                .user(user)
                .provider(OAuthProvider.GOOGLE)
                .providerAccountId(identity.sub())
                .build()
            );

        short keyVersion = tokenCipher.currentKeyVersion();

        connection.setProviderEmail(identity.email());
        connection.setAccessToken(tokenCipher.encrypt(tokens.accessToken()));

        // Google omits refresh_token on some re-consents. The column is NOT NULL
        // and the existing token stays valid, so only overwrite when one arrives.
        if (tokens.refreshToken() != null && !tokens.refreshToken().isBlank()) {
            connection.setRefreshToken(tokenCipher.encrypt(tokens.refreshToken()));
        }

        connection.setTokenKeyVersion(keyVersion);
        connection.setTokenExpiresAt(expiresAt(tokens));
        connection.setScopes(mergedScopes(connection, tokens));
        connection.setStatus(OAuthConnectionStatus.ACTIVE);
        connection.setRevokedAt(null);
        connection.setLastRefreshedAt(Instant.now());

        oauthConnectionRepository.save(connection);
    }

    private String[] mergedScopes(
        OAuthConnectionEntity connection,
        GoogleTokenResponse tokens
    ) {
        // include_granted_scopes=true means Google returns the union already, but
        // merging locally keeps previously granted scopes if a response is narrow.
        Set<String> scopes = new LinkedHashSet<>();

        if (connection.getScopes() != null) {
            scopes.addAll(Arrays.asList(connection.getScopes()));
        }

        scopes.addAll(tokens.grantedScopes());

        return scopes.toArray(String[]::new);
    }

    private Instant expiresAt(GoogleTokenResponse tokens) {
        long expiresIn = tokens.expiresIn() != null ? tokens.expiresIn() : 3600L;
        return Instant.now().plusSeconds(expiresIn);
    }

    private UserEntity extractUser(UserPrincipal principal) {
        return userRepository
            .findByUserUid(principal.subject())
            .orElseThrow(() -> new UserNotFoundException(
                "User not found: " + principal.subject()
            ));
    }

    // Only relative, non protocol-relative paths are honoured. Without this a
    // caller could pass an absolute URL and turn the callback into an open
    // redirect that carries the connection outcome to an attacker's site.
    private String safeRedirectAfter(String redirectAfter) {
        if (
            redirectAfter == null ||
            redirectAfter.isBlank() ||
            !redirectAfter.startsWith("/") ||
            redirectAfter.startsWith("//")
        ) {
            return DEFAULT_REDIRECT_AFTER;
        }

        return redirectAfter;
    }

    private URI redirect(
        String path,
        String outcome
    ) {
        return UriComponentsBuilder
            .fromUriString(frontendUrl)
            .path(safeRedirectAfter(path))
            .queryParam("google", outcome)
            .build()
            .toUri();
    }
}
